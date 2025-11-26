package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.protocols.UFC;
import org.kohsuke.MetaInfServices;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;

/**
 * Metrics instrumentation for {@link UFC}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class UFCMetricsInstrumentation extends AbstractFlowControlMetricsInstrumentation<UFC> {
    // All metrics are inherited from AbstractFlowControlMetricsInstrumentation
    // UFC reports average_time_blocked in milliseconds, so no override needed
}
