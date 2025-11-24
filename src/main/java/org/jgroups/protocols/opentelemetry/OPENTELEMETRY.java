package org.jgroups.protocols.opentelemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import org.jgroups.Message;
import org.jgroups.stack.Protocol;
import org.jgroups.annotations.MBean;
import org.jgroups.annotations.Property;
import org.jgroups.opentelemetry.impl.MetricsRegistrar;

/**
 * JGroups protocol that provides OpenTelemetry metrics integration.
 * This protocol automatically discovers and registers metrics from protocols in the stack
 * using ServiceLoader to find MetricsInstrumentation implementations.
 *
 * <p>Metrics instrumentation is discovered automatically via the ServiceLoader mechanism.
 * To add new instrumentation, implement {@link MetricsInstrumentation} with a public no-arg
 * constructor and annotate it with {@code @MetaInfServices(MetricsInstrumentation.class)}.</p>
 *
 * @author Radoslav Husar
 */
@MBean(description = "Protocol that instruments JGroups protocols with OpenTelemetry metrics")
public class OPENTELEMETRY extends Protocol {

    @Property(description = "Instrumentation scope name for OpenTelemetry meter",
            systemProperty = {"jgroups.opentelemetry.scope_name", "JGROUPS_OPENTELEMETRY_SCOPE_NAME"})
    protected String instrumentationScopeName = "org.jgroups";

    protected OpenTelemetry openTelemetry;

    public OPENTELEMETRY() {
    }

    public OPENTELEMETRY setOpenTelemetry(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        return this;
    }

    public String getInstrumentationScopeName() {
        return instrumentationScopeName;
    }

    public OPENTELEMETRY setInstrumentationScopeName(String instrumentationScopeName) {
        this.instrumentationScopeName = instrumentationScopeName;
        return this;
    }

    @Override
    public void init() throws Exception {
        super.init();

        if (openTelemetry == null) {
            openTelemetry = GlobalOpenTelemetry.get();
        }
        if (openTelemetry != null) {
            MetricsRegistrar.registerMetrics(openTelemetry, getProtocolStack(), instrumentationScopeName);
        }
    }

}
