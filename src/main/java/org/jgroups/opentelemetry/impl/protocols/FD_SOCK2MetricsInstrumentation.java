package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.FD_SOCK2;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link FD_SOCK2}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class FD_SOCK2MetricsInstrumentation implements MetricsInstrumentation<FD_SOCK2> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        FD_SOCK2 protocol = (FD_SOCK2) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        // Runtime metrics (always exposed)
        helper.registerLongGauge("suspects",
                "Number of currently suspected members",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getNumSuspectedMembers()));

        helper.registerLongCounter("suspect_events",
                "Number of suspect events generated",
                ObservableUnit.UNITY,
                measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "num_suspect_events")));

        // Configuration metrics (only exposed when exposeConfigurationMetrics=true)
        if (context.exposeConfigurationMetrics()) {
            helper.registerLongGauge("offset",
                    "Offset from the transport's bind port",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.getOffset()));

            helper.registerLongGauge("port.range",
                    "Number of ports to probe for finding a free port",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.getPortRange()));

            helper.registerLongGauge("port.client_bind",
                    "Start port for client socket (0 picks random port)",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.getClientBindPort()));

            helper.registerLongGauge("port.min",
                    "Lowest port the FD_SOCK2 server can listen on",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "min_port")));

            helper.registerLongGauge("port.max",
                    "Highest port the FD_SOCK2 server can listen on",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "max_port")));

            helper.registerLongGauge("interval.suspect_msg",
                    "Interval in milliseconds for broadcasting suspect messages",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getSuspectMsgInterval()));

            helper.registerLongGauge("timeout.connect",
                    "Maximum time in milliseconds to wait for a connect attempt",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "connect_timeout")));

            helper.registerLongGauge("linger",
                    "SO_LINGER in seconds (-1 disables it)",
                    ObservableUnit.SECONDS,
                    measurement -> measurement.record(protocol.getLinger()));
        }
    }
}
