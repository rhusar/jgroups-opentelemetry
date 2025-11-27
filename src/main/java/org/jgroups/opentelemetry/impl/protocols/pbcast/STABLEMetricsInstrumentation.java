package org.jgroups.opentelemetry.impl.protocols.pbcast;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.util.Util;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Field;

/**
 * Metrics instrumentation for {@link STABLE} protocol.
 * <p>
 * STABLE computes which broadcast messages are stable (delivered by all members) and triggers
 * garbage collection in NAKACK layer. It periodically sends digest messages to coordinator,
 * which sends stability messages when all members have reported their digests.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class STABLEMetricsInstrumentation implements MetricsInstrumentation<STABLE> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        STABLE protocol = (STABLE) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        // Get fields for metrics that need reflection
        Field numBytesReceivedField = Util.getField(STABLE.class, "num_bytes_received");
        Field suspendedField = Util.getField(STABLE.class, "suspended");

        // Runtime metrics (always exposed)
        helper.registerLongCounter("stable.sent",
                "Number of STABLE messages sent (digest reports to coordinator)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getStableSent()));

        helper.registerLongCounter("stable.received",
                "Number of STABLE messages received (digest reports from members)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getStableReceived()));

        helper.registerLongCounter("stability.sent",
                "Number of STABILITY messages sent (garbage collection triggers)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getStabilitySent()));

        helper.registerLongCounter("stability.received",
                "Number of STABILITY messages received (garbage collection triggers)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getStabilityReceived()));

        helper.registerLongGauge("bytes.received",
                "Total bytes accumulated from multicast messages since last STABLE message. *Critical*: Triggers STABLE when exceeds max_bytes",
                ObservableUnit.BYTES,
                measurement -> {
                    Object value = Util.getField(numBytesReceivedField, protocol);
                    measurement.record(value != null ? ((Number) value).longValue() : 0);
                });

        helper.registerLongGauge("votes",
                "Number of STABLE votes received for current digest. When equals member count, STABILITY is sent",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getNumVotes()));

        helper.registerLongGauge("suspended",
                "Indicates whether garbage collection is suspended (1=suspended, 0=active). Suspension indicator",
                ObservableUnit.UNITY,
                measurement -> {
                    Object value = Util.getField(suspendedField, protocol);
                    measurement.record(value != null && (Boolean) value ? 1 : 0);
                });

        helper.registerLongGauge("stable_task.running",
                "Indicates whether the stable task is running (1=running, 0=stopped). Task activity indicator",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getStableTaskRunning() ? 1 : 0));

        // Configuration metrics
        if (context.exposeConfigurationMetrics()) {
            helper.registerLongGauge("gossip.avg",
                    "Average interval in milliseconds between STABLE gossip messages (0 disables periodic gossip)",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getDesiredAverageGossip()));

            helper.registerLongGauge("bytes.max",
                    "Maximum bytes from multicast messages before triggering STABLE message (0 disables byte-based triggering)",
                    ObservableUnit.BYTES,
                    measurement -> measurement.record(protocol.getMaxBytes()));
        }
    }
}
