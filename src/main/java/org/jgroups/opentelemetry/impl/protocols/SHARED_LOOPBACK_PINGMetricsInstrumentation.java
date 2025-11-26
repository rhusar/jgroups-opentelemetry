package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.SHARED_LOOPBACK_PING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link SHARED_LOOPBACK_PING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class SHARED_LOOPBACK_PINGMetricsInstrumentation extends AbstractDiscoveryMetricsInstrumentation<SHARED_LOOPBACK_PING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common Discovery metrics
        super.registerMetrics(context);

        // Add SHARED_LOOPBACK_PING-specific metrics here if needed in the future
    }
}
