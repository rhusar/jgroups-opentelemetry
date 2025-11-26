package org.jgroups.opentelemetry.impl.protocols.dns;

import org.jgroups.opentelemetry.impl.protocols.AbstractDiscoveryMetricsInstrumentation;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.dns.DNS_PING;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link DNS_PING}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class DNS_PINGMetricsInstrumentation extends AbstractDiscoveryMetricsInstrumentation<DNS_PING> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common Discovery metrics
        super.registerMetrics(context);

        // Add DNS_PING-specific metrics here if needed in the future
    }
}
