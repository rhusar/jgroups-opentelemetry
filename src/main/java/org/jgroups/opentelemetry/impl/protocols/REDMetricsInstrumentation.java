package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.RED;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link RED}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class REDMetricsInstrumentation implements MetricsInstrumentation<RED> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        RED protocol = (RED) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        // Counter-based metrics (cumulative counts over time)
        helper.registerLongCounter("messages.total",
                "Total number of messages processed by RED",
                ObservableUnit.UNITY,
                measurement -> measurement.record(ReflectionHelper.getLongAdderValue(protocol, "total_msgs")));

        helper.registerLongCounter("messages.dropped",
                "Total number of messages dropped due to queue congestion",
                ObservableUnit.UNITY,
                measurement -> measurement.record(ReflectionHelper.getLongAdderValue(protocol, "dropped_msgs")));

        // Gauge metrics (current state)
        helper.registerDoubleGauge("queue.avg_size",
                "Exponentially weighted moving average of the bundler's queue size",
                ObservableUnit.UNITY,
                measurement -> measurement.record(ReflectionHelper.getDoubleValue(protocol, "avg_queue_size")));

        helper.registerLongGauge("queue.capacity",
                "Maximum capacity of the bundler's queue",
                ObservableUnit.UNITY,
                measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "queue_capacity")));

        helper.registerDoubleGauge("messages.drop_rate",
                "Ratio of dropped messages to total messages",
                ObservableUnit.UNITY,
                measurement -> {
                    double rate = protocol.getDropRate();
                    // TODO this needs better handling
                    // Only record if the value is valid (not NaN or Infinity)
                    if (Double.isFinite(rate)) {
                        measurement.record(rate);
                    } else {
                        measurement.record(0.0);
                    }
                });

        // Configuration metrics (enabled/thresholds)
        if (context.exposeConfigurationMetrics()) {
            helper.registerLongGauge("enabled",
                    "Whether RED is currently active (1=enabled, 0=disabled)",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.isEnabled() ? 1 : 0));

            helper.registerLongGauge("threshold.min",
                    "Minimum threshold below which no messages are dropped",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(ReflectionHelper.getLongValue(protocol, "min")));

            helper.registerLongGauge("threshold.max",
                    "Maximum threshold above which all messages are dropped",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(ReflectionHelper.getLongValue(protocol, "max")));
        }
    }
}
