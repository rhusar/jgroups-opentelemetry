package org.jgroups.opentelemetry.spi;

import org.jgroups.stack.Protocol;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Interface for instrumenting JGroups protocols with OpenTelemetry metrics.
 * Implementations register metric instruments that expose protocol internal state
 * to OpenTelemetry with standardized names and units.
 *
 * <p>Implementations are stateless and discovered via ServiceLoader. They must provide
 * a public no-arg constructor and be annotated with {@code @MetaInfServices(MetricsInstrumentation.class)}.</p>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * @MetaInfServices(MetricsInstrumentation.class)
 * public class UNICAST3Instrumentation implements MetricsInstrumentation<UNICAST3> {
 *
 *     @Override
 *     public void registerMetrics(InstrumentationContext context) {
 *         UNICAST3 unicast3 = (UNICAST3) context.getProtocol();
 *         Meter meter = context.getMeter();
 *         Log log = context.getLog();
 *
 *         // Register metrics using asynchronous callbacks
 *         context.registerLongGauge("messages.sent",
 *             "Total number of unicast messages sent",
 *             "{messages}",
 *             measurement -> {
 *                 measurement.record(ReflectionHelper.getLongAdderValue(unicast3, "num_msgs_sent", log));
 *             });
 *     }
 * }
 * }</pre>
 *
 * @author Radoslav Husar
 * @param <T> The specific protocol type this instrumentation monitors
 */
public interface MetricsInstrumentation<T extends Protocol> {

    /**
     * Returns the protocol class that this instrumentation supports.
     * This is used by the ServiceLoader mechanism to match instrumentation to protocols.
     *
     * <p>The default implementation uses reflection to extract the type parameter from the
     * implementing class. Implementations can override this method if needed.</p>
     *
     * @return The protocol class (e.g., UNICAST3.class)
     */
    @SuppressWarnings("unchecked")
    default Class<T> getProtocolClass() {
        // Walk up the class hierarchy to find MetricsInstrumentation<T>
        Class<?> currentClass = getClass();
        while (currentClass != null) {
            // Check interfaces at this level
            Type[] interfaces = currentClass.getGenericInterfaces();
            for (Type iface : interfaces) {
                if (iface instanceof ParameterizedType paramType) {
                    if (paramType.getRawType() == MetricsInstrumentation.class) {
                        Type typeArg = paramType.getActualTypeArguments()[0];
                        if (typeArg instanceof Class) {
                            return (Class<T>) typeArg;
                        }
                    }
                }
            }

            // Check the generic superclass
            Type superType = currentClass.getGenericSuperclass();
            if (superType instanceof ParameterizedType paramType) {
                Type rawType = paramType.getRawType();
                if (rawType instanceof Class<?> superClass) {
                    // Check if the superclass (or its ancestors) implements MetricsInstrumentation
                    if (implementsMetricsInstrumentation(superClass)) {
                        // Found it in superclass hierarchy, get the type argument from current level
                        Type typeArg = paramType.getActualTypeArguments()[0];
                        if (typeArg instanceof Class) {
                            return (Class<T>) typeArg;
                        }
                    }
                }
            }

            // Move up the hierarchy
            currentClass = currentClass.getSuperclass();
        }
        throw new IllegalStateException("Could not determine protocol class for " + getClass().getName());
    }

    /**
     * Helper method to check if a class (or its ancestors) implements MetricsInstrumentation.
     */
    private boolean implementsMetricsInstrumentation(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return false;
        }

        // Check direct interfaces
        for (Class<?> iface : clazz.getInterfaces()) {
            if (iface == MetricsInstrumentation.class) {
                return true;
            }
        }

        // Recursively check superclass
        return implementsMetricsInstrumentation(clazz.getSuperclass());
    }

    /**
     * Registers all metrics for the protocol with OpenTelemetry.
     * This method sets up asynchronous callbacks to read metric values from the protocol.
     *
     * @param context The instrumentation context containing protocol, meter, and log
     */
    void registerMetrics(InstrumentationContext context);
}
