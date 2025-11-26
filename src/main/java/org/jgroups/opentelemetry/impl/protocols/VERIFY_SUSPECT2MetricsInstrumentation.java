package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.protocols.VERIFY_SUSPECT2;
import org.kohsuke.MetaInfServices;

/**
 * Metrics instrumentation for {@link VERIFY_SUSPECT2}.
 *
 * @author Radoslav Husar
 */
@MetaInfServices(MetricsInstrumentation.class)
public class VERIFY_SUSPECT2MetricsInstrumentation implements MetricsInstrumentation<VERIFY_SUSPECT2> {

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
    }
}
