package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.RACKSPACE_PING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link RACKSPACE_PING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class RACKSPACE_PINGMetricsInstrumentation extends AbstractFilePingMetricsInstrumentation<RACKSPACE_PING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common FILE_PING metrics (which includes Discovery metrics)
        super.registerMetrics(context);

        // Add RACKSPACE_PING-specific metrics here if needed in the future
    }
}
