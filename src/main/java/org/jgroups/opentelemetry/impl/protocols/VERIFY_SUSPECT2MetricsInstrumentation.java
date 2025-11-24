package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.protocols.VERIFY_SUSPECT2;
import org.kohsuke.MetaInfServices;

/**
 * Instruments the VERIFY_SUSPECT2 protocol with OpenTelemetry metrics.
 * Registers metric instruments that expose VERIFY_SUSPECT2 internal state with
 * standardized names and units.
 *
 * <p>This class is stateless and automatically discovered via ServiceLoader.</p>
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class VERIFY_SUSPECT2MetricsInstrumentation implements MetricsInstrumentation<VERIFY_SUSPECT2> {

    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * Registers all VERIFY_SUSPECT2 metrics with OpenTelemetry.
     * Metric values are read asynchronously via callbacks.
     */
    @Override
    public void registerMetrics(InstrumentationContext context) {
        VERIFY_SUSPECT2 protocol = (VERIFY_SUSPECT2) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        helper.registerLongGauge("suspects",
                "Number of currently suspected members being verified",
                ObservableUnit.MEMBERS,
                measurement -> measurement.record(ReflectionHelper.getSetSize(protocol, "suspects")));

        helper.registerLongGauge("verification_task_running",
                "Indicates whether the verification task is currently running (1=running, 0=stopped)",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(ReflectionHelper.getBooleanValue(protocol, "running") ? 1 : 0));

        log.debug("registered VERIFY_SUSPECT2 metrics with OpenTelemetry");
    }
}
