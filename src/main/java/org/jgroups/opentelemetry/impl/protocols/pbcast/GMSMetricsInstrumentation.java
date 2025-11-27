package org.jgroups.opentelemetry.impl.protocols.pbcast;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.util.Util;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.Field;

/**
 * Metrics instrumentation for {@link GMS} (Group Membership Service).
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class GMSMetricsInstrumentation implements MetricsInstrumentation<GMS> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        GMS protocol = (GMS) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        // Get field for num_views
        Field numViewsField = Util.getField(GMS.class, "num_views");

        // Runtime metrics (always exposed)
        helper.registerLongGauge("views",
                "Total number of views installed in this member",
                ObservableUnit.UNITY,
                measurement -> {
                    Object value = Util.getField(numViewsField, protocol);
                    measurement.record(value != null ? ((Number) value).longValue() : 0);
                });

        helper.registerLongGauge("is_coord",
                "Indicates whether this member is the current coordinator (1=coordinator, 0=not coordinator)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.isCoord() ? 1 : 0));

        helper.registerLongGauge("is_leaving",
                "Indicates whether this member is in the process of leaving (1=leaving, 0=not leaving)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.isLeaving() ? 1 : 0));

        helper.registerLongGauge("merge.in_progress",
                "Indicates whether a merge is currently in progress (1=merging, 0=not merging)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.isMergeInProgress() ? 1 : 0));

        helper.registerLongGauge("merge.task.running",
                "Indicates whether the merge task is currently running (1=running, 0=stopped)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.isMergeTaskRunning() ? 1 : 0));

        helper.registerLongGauge("merge.killer.running",
                "Indicates whether the merge killer task is currently running (1=running, 0=stopped)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.isMergeKillerRunning() ? 1 : 0));

        helper.registerLongGauge("view_handler.queue",
                "Number of queued view change requests (JOIN/LEAVE/SUSPECT) waiting to be processed",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getViewHandlerSize()));

        helper.registerLongGauge("view_handler.suspended",
                "Indicates whether the view handler is suspended (1=suspended, 0=active)",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.isViewHandlerSuspended() ? 1 : 0));

        // Configuration metrics
        if (context.exposeConfigurationMetrics()) {
            helper.registerLongGauge("timeout.join",
                    "Timeout in milliseconds for join operations",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getJoinTimeout()));

            helper.registerLongGauge("timeout.leave",
                    "Timeout in milliseconds for leave operations",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getLeaveTimeout()));

            helper.registerLongGauge("timeout.merge",
                    "Timeout in milliseconds to complete merge operations",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getMergeTimeout()));

            helper.registerLongGauge("timeout.view_ack_collection",
                    "Timeout in milliseconds to wait for all VIEW acks (0 means wait forever)",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getViewAckCollectionTimeout()));

            helper.registerLongGauge("join_attempts.max",
                    "Maximum number of join attempts before giving up and becoming singleton (0 means never give up)",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.getMaxJoinAttempts()));
        }
    }
}
