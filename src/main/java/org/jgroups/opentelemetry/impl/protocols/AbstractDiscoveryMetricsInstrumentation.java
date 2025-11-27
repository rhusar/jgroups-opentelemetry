package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.Discovery;

/**
 * Base class for instrumenting Discovery protocol implementations with OpenTelemetry metrics.
 * Provides shared metric registration logic that is common to all Discovery-based protocols
 * (e.g., DNS_PING, JDBC_PING, FILE_PING, TCPPING, etc.).
 *
 * <p>Subclasses should override {@link #registerMetrics(InstrumentationContext)} and call
 * {@code super.registerMetrics(context)} first to register the common Discovery metrics,
 * then add their protocol-specific metrics.</p>
 *
 * @author Radoslav Husar
 */
public abstract class AbstractDiscoveryMetricsInstrumentation<T extends Discovery> implements MetricsInstrumentation<T> {

    /**
     * Registers metrics common to all Discovery protocol implementations.
     * Subclasses must call {@code super.registerMetrics(context)} first, then add their
     * protocol-specific metrics.
     *
     * @param context the instrumentation context containing the protocol instance and meter
     */
    @Override
    public void registerMetrics(InstrumentationContext context) {
        Discovery protocol = (Discovery) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        helper.registerLongGauge("is_coord",
                "Indicates whether this member is the current coordinator (1=coordinator, 0=not coordinator)",
                ObservableUnit.DIMENSIONLESS,
                measurement -> measurement.record(ReflectionHelper.getBooleanValue(protocol, "is_coord") ? 1 : 0));

        helper.registerLongCounter("discovery_requests",
                "Number of discovery requests sent",
                ObservableUnit.REQUESTS,
                measurement -> measurement.record(protocol.getNumberOfDiscoveryRequestsSent()));
    }
}
