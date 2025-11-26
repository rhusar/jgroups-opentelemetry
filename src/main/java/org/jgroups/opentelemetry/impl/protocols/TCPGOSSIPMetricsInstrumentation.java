package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.TCPGOSSIP;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link TCPGOSSIP}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class TCPGOSSIPMetricsInstrumentation extends AbstractDiscoveryMetricsInstrumentation<TCPGOSSIP> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common Discovery metrics
        super.registerMetrics(context);

        // Add TCPGOSSIP-specific metrics here if needed in the future
    }
}
