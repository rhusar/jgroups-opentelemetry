package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.SWIFT_PING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link SWIFT_PING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class SWIFT_PINGMetricsInstrumentation extends AbstractFilePingMetricsInstrumentation<SWIFT_PING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common FILE_PING metrics (which includes Discovery metrics)
        super.registerMetrics(context);

        // Add SWIFT_PING-specific metrics here if needed in the future
    }
}
