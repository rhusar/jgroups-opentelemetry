package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.TCPPING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link TCPPING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class TCPPINGMetricsInstrumentation extends AbstractDiscoveryMetricsInstrumentation<TCPPING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common Discovery metrics
        super.registerMetrics(context);

        // Add TCPPING-specific metrics here if needed in the future
    }
}
