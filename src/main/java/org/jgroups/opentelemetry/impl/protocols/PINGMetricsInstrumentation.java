package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.PING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link PING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class PINGMetricsInstrumentation extends AbstractDiscoveryMetricsInstrumentation<PING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common Discovery metrics
        super.registerMetrics(context);

        // Add PING-specific metrics here if needed in the future
    }
}
