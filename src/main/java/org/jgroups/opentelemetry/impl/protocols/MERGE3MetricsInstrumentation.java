package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.MERGE3;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link MERGE3}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class MERGE3MetricsInstrumentation implements MetricsInstrumentation<MERGE3> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        MERGE3 protocol = (MERGE3) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        // Runtime metrics (always exposed)
        helper.registerLongGauge("views.cached",
                "Number of cached ViewIds from different subgroups",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(protocol.getViews()));

        helper.registerLongCounter("merge_events",
                "Number of times a MERGE event was sent up the stack",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(protocol.getNumMergeEvents()));

        helper.registerLongGauge("view_consistency_checker.running",
                "Indicates whether the view consistency checker task is running (1=running, 0=stopped)",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(protocol.isViewConsistencyCheckerRunning() ? 1 : 0));

        helper.registerLongGauge("info_sender.running",
                "Indicates whether the info sender task is running (1=running, 0=stopped)",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(protocol.isInfoSenderRunning() ? 1 : 0));

        // Configuration metrics (only exposed when exposeConfigurationMetrics=true)
        if (context.exposeConfigurationMetrics()) {
            helper.registerLongGauge("interval.min",
                    "Minimum time in milliseconds before sending an info message",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(ReflectionHelper.getLongValue(protocol, "min_interval")));

            helper.registerLongGauge("interval.max",
                    "Maximum interval in milliseconds when the next info message will be sent",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(ReflectionHelper.getLongValue(protocol, "max_interval")));

            helper.registerLongGauge("interval.check",
                    "Interval in milliseconds after which we check for view inconsistencies",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(ReflectionHelper.getLongValue(protocol, "check_interval")));

            helper.registerLongGauge("max_participants_in_merge",
                    "Maximum number of merge participants to be involved in a merge (0=unlimited)",
                    ObservableUnit.DIMENSIONLESS,
                    measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "max_participants_in_merge")));
        }
    }
}
