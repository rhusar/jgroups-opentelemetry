package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.protocols.JDBC_PING;
import org.kohsuke.MetaInfServices;

/**
 * Instruments the JDBC_PING protocol with OpenTelemetry metrics.
 * Registers metric instruments that expose JDBC_PING internal state with
 * standardized names and units.
 *
 * <p>This class is stateless and automatically discovered via ServiceLoader.</p>
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class JDBC_PINGMetricsInstrumentation implements MetricsInstrumentation<JDBC_PING> {

    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * Registers all JDBC_PING metrics with OpenTelemetry.
     * Metric values are read asynchronously via callbacks.
     */
    @Override
    public void registerMetrics(InstrumentationContext context) {
        JDBC_PING protocol = (JDBC_PING) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        helper.registerLongGauge("writes",
                "Number of times discovery information was written",
                ObservableUnit.OPERATIONS,
                measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "writes")));

        helper.registerLongGauge("reads",
                "Number of times discovery information was read",
                ObservableUnit.OPERATIONS,
                measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "reads")));

        helper.registerLongGauge("discovery_requests",
                "Number of discovery requests sent",
                ObservableUnit.REQUESTS,
                measurement -> measurement.record(protocol.getNumberOfDiscoveryRequestsSent()));

        log.debug("registered JDBC_PING metrics with OpenTelemetry");
    }
}
