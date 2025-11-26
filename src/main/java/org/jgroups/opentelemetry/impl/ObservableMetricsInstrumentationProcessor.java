package org.jgroups.opentelemetry.impl;

import io.opentelemetry.api.metrics.Meter;
import org.jgroups.logging.Log;
import org.jgroups.annotations.observability.Observable;
import org.jgroups.annotations.observability.ObservableScope;
import org.jgroups.logging.LogFactory;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.stack.Protocol;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Generic instrumentation processor that scans @Observable annotations on protocol classes
 * and automatically registers corresponding OpenTelemetry metrics.
 *
 * <p>This processor acts as a catch-all fallback for any protocol that doesn't have
 * a specific MetricsInstrumentation implementation. It scans protocol instances for
 * fields and methods annotated with @Observable and creates appropriate metric
 * instruments based on the annotation parameters.</p>
 *
 * <p>This class is stateless and automatically discovered via ServiceLoader.</p>
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class ObservableMetricsInstrumentationProcessor implements MetricsInstrumentation<Protocol> {

    private static final Log log = LogFactory.getLog(ObservableMetricsInstrumentationProcessor.class);

    @Override
    public Class<Protocol> getProtocolClass() {
        return Protocol.class;
    }

    @Override
    public void registerMetrics(InstrumentationContext context) {
        Protocol protocol = context.protocol();
        Meter meter = context.meter();

        int count = 0;

        // Process annotated fields
        count += processFields(protocol, meter, context);

        // Process annotated methods
        count += processMethods(protocol, meter, context);

        if (count > 0) {
            log.debug("registered %d @Observable metrics for %s", count, protocol.getClass().getSimpleName());
        }
    }

    /**
     * Processes fields annotated with @Observable.
     */
    private int processFields(Protocol protocol, Meter meter, InstrumentationContext context) {
        int count = 0;
        Class<?> clazz = protocol.getClass();

        for (Field field : getAllFields(clazz)) {
            Observable annotation = field.getAnnotation(Observable.class);
            if (annotation != null) {
                // Skip configuration metrics if exposeConfigurationMetrics is false
                if (annotation.scope() == ObservableScope.CONFIGURATION && !context.exposeConfigurationMetrics()) {
                    log.trace("skipping configuration metric for field %s.%s (exposeConfigurationMetrics=false)",
                             clazz.getSimpleName(), field.getName());
                    continue;
                }

                try {
                    registerFieldMetric(protocol, meter, field, annotation);
                    count++;
                } catch (Exception e) {
                    log.warn("failed to register metric for field %s.%s: %s",
                             clazz.getSimpleName(), field.getName(), e.getMessage());
                }
            }
        }

        return count;
    }

    /**
     * Processes methods annotated with @Observable.
     */
    private int processMethods(Protocol protocol, Meter meter, InstrumentationContext context) {
        int count = 0;
        Class<?> clazz = protocol.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            Observable annotation = method.getAnnotation(Observable.class);
            if (annotation != null) {
                // Skip configuration metrics if exposeConfigurationMetrics is false
                if (annotation.scope() == ObservableScope.CONFIGURATION && !context.exposeConfigurationMetrics()) {
                    log.trace("skipping configuration metric for method %s.%s (exposeConfigurationMetrics=false)",
                             clazz.getSimpleName(), method.getName());
                    continue;
                }

                try {
                    registerMethodMetric(protocol, meter, method, annotation);
                    count++;
                } catch (Exception e) {
                    log.warn("failed to register metric for method %s.%s: %s",
                            clazz.getSimpleName(), method.getName(), e.getMessage());
                }
            }
        }

        return count;
    }

    /**
     * Registers a metric for an annotated field.
     */
    private static void registerFieldMetric(Protocol protocol, Meter meter, Field field, Observable annotation) throws IllegalAccessException {
        field.setAccessible(true);
        String metricName = getMetricName(protocol, annotation, field.getName());
        String description = annotation.description();
        String unit = annotation.unit().toString();

        switch (annotation.type()) {
            case GAUGE:
                registerGaugeForField(protocol, meter, field, metricName, description, unit);
                break;
            case COUNTER:
            case UP_DOWN_COUNTER:
                registerCounterForField(protocol, meter, field, metricName, description, unit);
                break;
            case HISTOGRAM:
                log.warn("HISTOGRAM type not yet supported for field %s",  field.getName());
                break;
        }
    }

    /**
     * Registers a metric for an annotated method.
     */
    private static void registerMethodMetric(Protocol protocol, Meter meter, Method method, Observable annotation) {
        String metricName = getMetricName(protocol, annotation, method.getName());
        String description = annotation.description();
        String unit = annotation.unit().toString();

        switch (annotation.type()) {
            case GAUGE:
                registerGaugeForMethod(protocol, meter, method, metricName, description, unit);
                break;
            case COUNTER:
            case UP_DOWN_COUNTER:
                registerCounterForMethod(protocol, meter, method, metricName, description, unit);
                break;
            case HISTOGRAM:
                log.warn("HISTOGRAM type not yet supported for method %s", method.getName());
                break;
        }
    }

    /**
     * Registers a gauge metric for a field.
     */
    private static void registerGaugeForField(Protocol protocol, Meter meter, Field field, String name, String description, String unit) {
        meter.gaugeBuilder(name)
            .setDescription(description)
            .setUnit(unit)
            .ofLongs()
            .buildWithCallback(measurement -> {
                try {
                    long value = extractLongValue(field.get(protocol));
                    measurement.record(value);
                } catch (Exception e) {
                    log.trace("failed to read field %s: %s", field.getName(), e.getMessage());
                }
            });
    }

    /**
     * Registers a gauge metric for a method.
     */
    private static void registerGaugeForMethod(Protocol protocol, Meter meter, Method method, String name, String description, String unit) {
        meter.gaugeBuilder(name)
            .setDescription(description)
            .setUnit(unit)
            .ofLongs()
            .buildWithCallback(measurement -> {
                try {
                    method.setAccessible(true);
                    Object result = method.invoke(protocol);
                    long value = extractLongValue(result);
                    measurement.record(value);
                } catch (Exception e) {
                    log.trace("failed to invoke method %s: %s", method.getName(), e.getMessage());
                }
            });
    }

    /**
     * Registers a counter metric for a field (using gauge with callback).
     */
    private static void registerCounterForField(Protocol protocol, Meter meter, Field field, String name, String description, String unit) {
        // OpenTelemetry Java SDK doesn't support async counters, so we use gauges
        registerGaugeForField(protocol, meter, field, name, description, unit);
    }

    /**
     * Registers a counter metric for a method (using gauge with callback).
     */
    private static void registerCounterForMethod(Protocol protocol, Meter meter, Method method, String name, String description, String unit) {
        // OpenTelemetry Java SDK doesn't support async counters, so we use gauges
        registerGaugeForMethod(protocol, meter, method, name, description, unit);
    }

    /**
     * Extracts a long value from various numeric types.
     */
    private static long extractLongValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof LongAdder) {
            return ((LongAdder) value).sum();
        }
        if (value instanceof AtomicLong) {
            return ((AtomicLong) value).get();
        }
        if (value instanceof AtomicInteger) {
            return ((AtomicInteger) value).get();
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }

    /**
     * Gets the metric name from the annotation, or derives it from the field/method name.
     * Automatically adds the jgroups.{protocol}. prefix.
     */
    private static String getMetricName(Protocol protocol, Observable annotation, String defaultName) {
        String protocolName = protocol.getClass().getSimpleName().toLowerCase();
        String prefix = "org.jgroups." + protocolName + ".";

        String suffix;
        if (annotation.name() != null && !annotation.name().isEmpty()) {
            suffix = annotation.name();
        } else {
            // Convert field/method name to metric name (e.g., num_msgs_sent -> num.msgs.sent)
            suffix = defaultName.replace('_', '.');
        }

        return prefix + suffix;
    }

    /**
     * Gets all fields from a class and its superclasses.
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
