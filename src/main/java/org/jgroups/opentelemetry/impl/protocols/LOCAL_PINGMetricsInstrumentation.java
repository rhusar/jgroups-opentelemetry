package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.LOCAL_PING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link LOCAL_PING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class LOCAL_PINGMetricsInstrumentation extends AbstractDiscoveryMetricsInstrumentation<LOCAL_PING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common Discovery metrics
        super.registerMetrics(context);

        // Add LOCAL_PING-specific metrics here if needed in the future
    }
}
