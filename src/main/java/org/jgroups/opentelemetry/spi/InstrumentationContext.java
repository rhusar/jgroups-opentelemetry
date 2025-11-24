package org.jgroups.opentelemetry.spi;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import org.jgroups.Global;
import org.jgroups.stack.Protocol;

/**
 * Context passed to {@link MetricsInstrumentation#registerMetrics(InstrumentationContext)}
 * containing all dependencies needed to register metrics for a protocol.
 *
 * <p>This context interface encapsulates:</p>
 * <ul>
 *   <li>The protocol instance to instrument</li>
 *   <li>The OpenTelemetry meter for registering metrics</li>
 * </ul>
 *
 * @author Radoslav Husar
 */
public interface InstrumentationContext {

    /**
     * Gets the protocol instance to instrument.
     *
     * @return The protocol instance
     */
    Protocol protocol();

    /**
     * Gets the metric name prefix for this protocol, including a trailing dot.
     * The prefix follows the pattern "jgroups.{protocol_name}." where the protocol name
     * is derived from the fully qualified class name with the JGroups protocol package prefix removed.
     * For example:
     * <ul>
     *   <li>org.jgroups.protocols.UNICAST3 → jgroups.unicast3.</li>
     *   <li>org.jgroups.protocols.pbcast.GMS → jgroups.pbcast.gms.</li>
     *   <li>org.jgroups.protocols.JDBC_PING → jgroups.jdbc_ping.</li>
     * </ul>
     *
     * @return The metric name prefix with trailing dot (e.g., "jgroups.unicast3.", "jgroups.pbcast.gms.")
     */
    default String getPrefix() {
        String className = protocol().getClass().getName();
        if (className.startsWith(Global.PREFIX)) {
            className = className.substring(Global.PREFIX.length());
        }
        return "jgroups." + className.toLowerCase() + ".";
    }

    /**
     * Gets the OpenTelemetry meter for registering metrics.
     *
     * @return The meter
     */
    Meter meter();

    /**
     * Gets the attributes to be attached to metrics for this protocol.
     * The attributes include contextual information such as the cluster name and node identifier.
     * These attributes allow distinguishing metrics from different nodes within the same cluster.
     *
     * @return The attributes containing cluster-level and node-level metadata
     */
    default Attributes getAttributes() {
        String clusterName = protocol().getProtocolStack().getChannel().getClusterName();
        String nodeName = protocol().getProtocolStack().getChannel().getName();
        return Attributes.of(
                AttributeKey.stringKey("cluster"), clusterName,
                AttributeKey.stringKey("node"), nodeName
        );
    }
}
