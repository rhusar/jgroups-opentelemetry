package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.FD_ALL;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link FD_ALL}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class FD_ALLMetricsInstrumentation implements MetricsInstrumentation<FD_ALL> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        FD_ALL protocol = (FD_ALL) context.protocol();
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

            helper.registerLongGauge("timeout_check_interval",
                    "Interval in milliseconds at which the heartbeat timeouts are checked",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getTimeoutCheckInterval()));

            helper.registerLongGauge("use_time_service",
                    "Whether to use TimeService for timestamps (1=enabled, 0=disabled)",
                    ObservableUnit.DIMENSIONLESS,
                    measurement -> measurement.record(ReflectionHelper.getBooleanValue(protocol, "use_time_service") ? 1 : 0));
        }
    }
}
