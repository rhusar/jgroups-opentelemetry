package org.jgroups.opentelemetry.impl;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Handles discovery and registration of metrics instrumentation for JGroups protocols.
 * This class encapsulates the logic for loading MetricsInstrumentation providers via ServiceLoader
 * and registering metrics for protocols in a stack.
 *
 * @author Radoslav Husar
 */
public class MetricsRegistrar {

    private static final Log log = LogFactory.getLog(MetricsRegistrar.class);

    /**
     * Registers metrics for all protocols in the given protocol stack.
     * Discovers instrumentation via ServiceLoader and matches them to protocols.
     *
     * @param openTelemetry The OpenTelemetry instance to create the meter from
     * @param protocolStack The protocol stack containing protocols to instrument
     * @param instrumentationScopeName The instrumentation scope name for the OpenTelemetry meter
     */
    public static void registerMetrics(OpenTelemetry openTelemetry, ProtocolStack protocolStack, String instrumentationScopeName) {
        Meter meter = openTelemetry.getMeter(instrumentationScopeName);
        List<Protocol> protocols = protocolStack.getProtocols();
        // Load all available MetricsInstrumentation providers and create a map
        @SuppressWarnings("rawtypes")
        ServiceLoader<MetricsInstrumentation> serviceLoader = ServiceLoader.load(MetricsInstrumentation.class);
        Map<Class<? extends Protocol>, MetricsInstrumentation<?>> instrumentationMap = new HashMap<>();
        MetricsInstrumentation<Protocol> genericInstrumentation = null;

        for (MetricsInstrumentation<?> instrumentation : serviceLoader) {
            Class<? extends Protocol> protocolClass = instrumentation.getProtocolClass();
            instrumentationMap.put(protocolClass, instrumentation);
            log.trace("loaded instrumentation for protocol %s", protocolClass.getSimpleName());

            // Keep reference to generic instrumentation (Protocol.class)
            if (protocolClass == Protocol.class) {
                //noinspection unchecked
                genericInstrumentation = (MetricsInstrumentation<Protocol>) instrumentation;
            }
        }

        // Iterate through protocols in the stack and register metrics if instrumentation exists
        int registeredCount = 0;
        int genericCount = 0;

        for (Protocol protocol : protocols) {
            MetricsInstrumentation<?> instrumentation = instrumentationMap.get(protocol.getClass());

            if (instrumentation != null && instrumentation.getProtocolClass() != Protocol.class) {
                // Use specific instrumentation if available
                log.debug("found protocol %s, registering specific metrics instrumentation", protocol.getClass().getSimpleName());

                InstrumentationContext context = new BasicInstrumentationContext(protocol, meter);
                instrumentation.registerMetrics(context);
                registeredCount++;
            } else if (genericInstrumentation != null) {
                // Fall back to generic @Observable processor
                log.trace("no specific instrumentation for protocol %s, using generic @Observable processor", protocol.getClass().getSimpleName());

                InstrumentationContext context = new BasicInstrumentationContext(protocol, meter);
                genericInstrumentation.registerMetrics(context);
                genericCount++;
            }
        }

        if (registeredCount == 0 && genericCount == 0) {
            log.debug("no metrics instrumentation registered (no matching protocols found in stack)");
        } else {
            log.debug("registered %d specific and %d generic instrumentation(s)", registeredCount, genericCount);
        }
    }
}
