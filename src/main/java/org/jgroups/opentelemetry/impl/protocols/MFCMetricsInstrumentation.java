package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.MFC;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link MFC} (Multicast Flow Control).
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class MFCMetricsInstrumentation extends AbstractFlowControlMetricsInstrumentation<MFC> {

    /**
     * Override to report MFC's average_time_blocked in nanoseconds (its native unit).
     */
    @Override
    protected void registerAverageTimeBlocked(RegistrationHelper helper, MFC protocol) {
        helper.registerDoubleGauge("blocked.avg",
                "Average time in nanoseconds that senders were blocked waiting for credits",
                ObservableUnit.NANOSECONDS,
                measurement -> measurement.record(protocol.getAverageTimeBlocked()));
    }
}
