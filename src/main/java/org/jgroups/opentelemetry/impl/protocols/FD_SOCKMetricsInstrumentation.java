package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.FD_SOCK;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link FD_SOCK}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class FD_SOCKMetricsInstrumentation implements MetricsInstrumentation<FD_SOCK> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        FD_SOCK protocol = (FD_SOCK) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        // Runtime metrics (always exposed)
        helper.registerLongGauge("suspects",
                "Number of currently suspected members",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getNumSuspectedMembers()));

        helper.registerLongCounter("suspect_events",
                "Number of suspect events generated",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getNumSuspectEventsGenerated()));

        helper.registerLongGauge("monitor.running",
                "Indicates whether the node crash detection monitor is running (1=running, 0=stopped)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.isNodeCrashMonitorRunning() ? 1 : 0));

        // Configuration metrics (only exposed when exposeConfigurationMetrics=true)
        if (context.exposeConfigurationMetrics()) {
            helper.registerLongGauge("timeout.get_cache",
                    "Timeout in milliseconds for getting socket cache from coordinator",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getGetCacheTimeout()));

            helper.registerLongGauge("timeout.sock_conn",
                    "Maximum time in milliseconds to wait for ping Socket.connect() to return",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "sock_conn_timeout")));

            helper.registerLongGauge("interval.suspect_msg",
                    "Interval in milliseconds for broadcasting suspect messages",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getSuspectMsgInterval()));

            helper.registerLongGauge("cache.max_elements",
                    "Maximum number of elements in the cache until deleted elements are removed",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.getCacheMaxElements()));

            helper.registerLongGauge("cache.max_age",
                    "Maximum age in milliseconds an element marked as removed has to have until it is removed",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getCacheMaxAge()));

            helper.registerLongGauge("num_tries",
                    "Number of attempts coordinator is solicited for socket cache until we give up",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.getNumTries()));

            helper.registerLongGauge("port.start",
                    "Start port for server socket (0 picks random port)",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.getStartPort()));

            helper.registerLongGauge("port.client_bind",
                    "Start port for client socket (0 picks random port)",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.getClientBindPort()));

            helper.registerLongGauge("port.range",
                    "Number of ports to probe for start_port and client_bind_port",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "port_range")));

            helper.registerLongGauge("keep_alive",
                    "Whether to use KEEP_ALIVE on the ping socket (1=enabled, 0=disabled)",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(ReflectionHelper.getBooleanValue(protocol, "keep_alive") ? 1 : 0));
        }
    }
}
