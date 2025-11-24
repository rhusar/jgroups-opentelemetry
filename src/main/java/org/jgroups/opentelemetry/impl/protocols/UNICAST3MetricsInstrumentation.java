package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.protocols.UNICAST3;
import org.kohsuke.MetaInfServices;

/**
 * Instruments the UNICAST3 protocol with OpenTelemetry metrics.
 * Registers metric instruments that expose UNICAST3 internal state with
 * standardized names and units.
 *
 * <p>This class is stateless and automatically discovered via ServiceLoader.</p>
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class UNICAST3MetricsInstrumentation implements MetricsInstrumentation<UNICAST3> {

    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * Registers all UNICAST3 metrics with OpenTelemetry.
     * Metric values are read asynchronously via callbacks.
     */
    @Override
    public void registerMetrics(InstrumentationContext context) {
        UNICAST3 protocol = (UNICAST3) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        // Counter-based metrics (cumulative counts over time)
        helper.registerLongCounter("messages.sent",
                "Total number of unicast messages sent",
                ObservableUnit.MESSAGES,
                measurement -> measurement.record(ReflectionHelper.getLongAdderValue(protocol, "num_msgs_sent")));
        helper.registerLongCounter("messages.received",
                "Total number of unicast messages received",
                ObservableUnit.MESSAGES,
                measurement -> measurement.record(ReflectionHelper.getLongAdderValue(protocol, "num_msgs_received")));
        helper.registerLongCounter("retransmissions",
                "Number of retransmitted messages (indicates network issues)",
                ObservableUnit.MESSAGES,
                measurement -> measurement.record(ReflectionHelper.getLongAdderValue(protocol, "num_xmits")));
        helper.registerLongCounter("xmit_requests.sent",
                "Number of retransmit requests sent",
                ObservableUnit.REQUESTS,
                measurement -> measurement.record(ReflectionHelper.getLongAdderValue(protocol, "xmit_reqs_sent")));
        helper.registerLongCounter("xmit_requests.received",
                "Number of retransmit requests received",
                ObservableUnit.REQUESTS,
                measurement -> measurement.record(ReflectionHelper.getLongAdderValue(protocol, "xmit_reqs_received")));
        helper.registerLongCounter("acks.sent",
                "Number of acknowledgments sent",
                ObservableUnit.ACKS,
                measurement -> measurement.record(ReflectionHelper.getLongAdderValue(protocol, "num_acks_sent")));
        helper.registerLongCounter("acks.received",
                "Number of acknowledgments received",
                ObservableUnit.ACKS,
                measurement -> measurement.record(ReflectionHelper.getLongAdderValue(protocol, "num_acks_received")));

        // Gauge metrics (current state)
        helper.registerLongGauge("connections",
                "Total number of connections",
                ObservableUnit.CONNECTIONS,
                measurement -> measurement.record(protocol.getNumConnections()));
        helper.registerLongGauge("connections.send",
                "Number of outgoing send connections",
                ObservableUnit.CONNECTIONS,
                measurement -> measurement.record(protocol.getNumSendConnections()));
        helper.registerLongGauge("connections.receive",
                "Number of incoming receive connections",
                ObservableUnit.CONNECTIONS,
                measurement -> measurement.record(protocol.getNumReceiveConnections()));
        helper.registerLongGauge("messages.unacked",
                "Number of unacknowledged messages (indicates backpressure)",
                ObservableUnit.MESSAGES,
                measurement -> measurement.record(protocol.getNumUnackedMessages()));
        helper.registerLongGauge("xmit_table.missing_messages",
                "Number of missing messages in receive windows",
                ObservableUnit.MESSAGES,
                measurement -> measurement.record(protocol.getXmitTableMissingMessages()));
        helper.registerLongGauge("xmit_table.undelivered_messages",
                "Number of undelivered messages in all receive windows",
                ObservableUnit.MESSAGES,
                measurement -> measurement.record(protocol.getXmitTableUndeliveredMessages()));

        log.debug("registered UNICAST3 metrics with OpenTelemetry");
    }
}
