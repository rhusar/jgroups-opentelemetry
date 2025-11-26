package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.protocols.FILE_PING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link FILE_PING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(org.jgroups.opentelemetry.spi.MetricsInstrumentation.class)
public class FILE_PINGMetricsInstrumentation extends AbstractFilePingMetricsInstrumentation<FILE_PING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common FILE_PING metrics (which includes Discovery metrics)
        super.registerMetrics(context);

        // Add FILE_PING-specific metrics here if needed in the future
    }
}
