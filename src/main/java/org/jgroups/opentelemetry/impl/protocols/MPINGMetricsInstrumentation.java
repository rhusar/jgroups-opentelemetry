package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.MPING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link MPING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class MPINGMetricsInstrumentation extends AbstractDiscoveryMetricsInstrumentation<MPING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common Discovery metrics
        super.registerMetrics(context);

        // Add MPING-specific metrics here if needed in the future
    }
}
