package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.JDBC_PING2;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link JDBC_PING2}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class JDBC_PING2MetricsInstrumentation extends AbstractFilePingMetricsInstrumentation<JDBC_PING2> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common FILE_PING metrics (which includes Discovery metrics)
        super.registerMetrics(context);

        // Add JDBC_PING2-specific metrics here if needed in the future
    }
}
