package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.BPING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link BPING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class BPINGMetricsInstrumentation extends AbstractDiscoveryMetricsInstrumentation<BPING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common Discovery metrics
        super.registerMetrics(context);

        // Add BPING-specific metrics here if needed in the future
    }
}
