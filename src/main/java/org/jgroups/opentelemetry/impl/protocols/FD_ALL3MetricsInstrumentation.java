package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.FD_ALL3;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link FD_ALL3}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class FD_ALL3MetricsInstrumentation implements MetricsInstrumentation<FD_ALL3> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        FD_ALL3 protocol = (FD_ALL3) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        // Runtime metrics (always exposed)
        helper.registerLongCounter("heartbeats.sent",
                "Number of heartbeats sent",
                ObservableUnit.MESSAGES,
                measurement -> measurement.record(protocol.getHeartbeatsSent()));

        helper.registerLongCounter("heartbeats.received",
                "Number of heartbeats received",
                ObservableUnit.MESSAGES,
                measurement -> measurement.record(protocol.getHeartbeatsReceived()));

        helper.registerLongCounter("suspect_events",
                "Number of suspect events sent",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(protocol.getSuspectEventsSent()));

        helper.registerLongGauge("has_suspected_members",
                "Indicates whether there are currently any suspected members (1=yes, 0=no)",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(ReflectionHelper.getBooleanValue(protocol, "has_suspected_mbrs") ? 1 : 0));

        helper.registerLongGauge("timeout_checker.running",
                "Indicates whether the timeout checker task is running (1=running, 0=stopped)",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(protocol.isTimeoutCheckerRunning() ? 1 : 0));

        helper.registerLongGauge("heartbeat_sender.running",
                "Indicates whether the heartbeat sender task is running (1=running, 0=stopped)",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(protocol.isHeartbeatSenderRunning() ? 1 : 0));

        // Configuration metrics
        if (context.exposeConfigurationMetrics()) {
            helper.registerLongGauge("timeout",
                    "Timeout in milliseconds after which a node is suspected if no heartbeat or data received",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getTimeout()));

            helper.registerLongGauge("interval",
                    "Interval in milliseconds at which a heartbeat is sent to the cluster",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getInterval()));

            helper.registerLongGauge("num_bits",
                    "Number of bits for each member (timeout / interval)",
                    ObservableUnit.DIMENSIONLESS,
                    measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "num_bits")));
        }
    }
}
