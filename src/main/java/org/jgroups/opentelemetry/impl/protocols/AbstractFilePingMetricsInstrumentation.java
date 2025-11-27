package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.ReflectionHelper;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.protocols.FILE_PING;

/**
 * Base class for instrumenting FILE_PING protocol implementations with OpenTelemetry metrics.
 * Provides shared metric registration logic that is common to all FILE_PING-based protocols
 * (e.g., JDBC_PING, JDBC_PING2, RACKSPACE_PING, SWIFT_PING).
 *
 * <p>FILE_PING extends Discovery and adds file-based storage metrics (writes, reads).
 * Subclasses should override {@link #registerMetrics(InstrumentationContext)} and call
 * {@code super.registerMetrics(context)} first to register the common FILE_PING metrics,
 * then add their protocol-specific metrics.</p>
 *
 * @author Radoslav Husar
 */
public abstract class AbstractFilePingMetricsInstrumentation<T extends FILE_PING> extends AbstractDiscoveryMetricsInstrumentation<T> {

    /**
     * Registers metrics common to all FILE_PING protocol implementations.
     * Subclasses must call {@code super.registerMetrics(context)} first, then add their
     * protocol-specific metrics.
     *
     * @param context the instrumentation context containing the protocol instance and meter
     */
    @Override
    public void registerMetrics(InstrumentationContext context) {
        // Register common Discovery metrics
        super.registerMetrics(context);

        // Register FILE_PING-specific metrics (writes, reads)
        FILE_PING protocol = (FILE_PING) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        helper.registerLongGauge("writes",
                "Number of times discovery information was written",
                ObservableUnit.UNITY,
                measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "writes")));

        helper.registerLongGauge("reads",
                "Number of times discovery information was read",
                ObservableUnit.UNITY,
                measurement -> measurement.record(ReflectionHelper.getIntValue(protocol, "reads")));
    }
}
