/**
 * Public API for declarative metrics exposure in JGroups protocols.
 *
 * <p>This package provides a lightweight, annotation-based approach for exposing protocol
 * metrics to observability systems like OpenTelemetry. The API is designed to be framework-agnostic
 * and could be integrated directly into the JGroups core project in the future.</p>
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link org.jgroups.annotations.observability.Observable @Observable} - Annotation for marking
 *       fields and methods that should be exposed as metrics</li>
 *   <li>{@link org.jgroups.annotations.observability.ObservableType} - Enumeration of metric types
 *       (COUNTER, GAUGE, UP_DOWN_COUNTER, HISTOGRAM)</li>
 *   <li>{@link org.jgroups.annotations.observability.ObservableUnit} - UCUM-compliant unit definitions
 *       for standardized metric units</li>
 * </ul>
 *
 * <h2>Design Goals</h2>
 * <p>The API is intentionally designed with minimal dependencies to facilitate potential upstream
 * integration into JGroups:</p>
 * <ul>
 *   <li><b>No external dependencies</b> - Uses only standard Java annotations and enums</li>
 *   <li><b>Framework-agnostic</b> - Not tied to any specific metrics framework (OpenTelemetry, Micrometer, etc.)</li>
 *   <li><b>Declarative approach</b> - Protocol developers simply annotate fields/methods rather than writing
 *       metric registration code</li>
 *   <li><b>Type safety</b> - Strong typing for metric types and units prevents common errors</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class UNICAST3 extends Protocol {
 *     @Observable(
 *         name = "messages.sent",
 *         type = ObservableType.COUNTER,
 *         unit = ObservableUnit.UNITY,
 *         description = "Total number of unicast messages sent"
 *     )
 *     protected final LongAdder num_msgs_sent = new LongAdder();
 *     // Results in metric name: jgroups.unicast3.messages.sent
 *
 *     @Observable(
 *         type = ObservableType.GAUGE,
 *         unit = ObservableUnit.UNITY,
 *         description = "Number of active send connections"
 *     )
 *     public int getNumSendConnections() {
 *         return send_table.size();
 *     }
 *     // Results in metric name: jgroups.unicast3.getNumSendConnections
 * }
 * }</pre>
 *
 * <h2>Integration with Metrics Frameworks</h2>
 * <p>The API itself does not implement metric collection - it only provides metadata annotations.
 * Actual metric registration is handled by framework-specific processors, such as:</p>
 * <ul>
 *   <li>{@link org.jgroups.opentelemetry.impl.ObservableMetricsInstrumentationProcessor} - OpenTelemetry integration</li>
 *   <li>Micrometer integration (future)</li>
 * </ul>
 *
 * <h2>Upstream Integration Path</h2>
 * <p>This package is designed for upstreaming into JGroups core. It already uses the
 * {@code org.jgroups.annotations} namespace for seamless integration. The migration path would be:</p>
 * <ol>
 *   <li>Move this package directly to JGroups core (no package rename needed)</li>
 *   <li>Annotate existing protocol fields/methods with {@code @Observable}</li>
 *   <li>Allow metrics frameworks to discover and register annotated metrics automatically</li>
 *   <li>Maintain backward compatibility with existing JMX-based metrics</li>
 * </ol>
 *
 * <p>Benefits of upstream integration:</p>
 * <ul>
 *   <li>Single source of truth for metric definitions across all observability frameworks</li>
 *   <li>Protocol developers define metrics once, works with any framework</li>
 *   <li>Consistent metric names, types, and units across JGroups deployments</li>
 *   <li>Easier to maintain and evolve metrics as protocols change</li>
 * </ul>
 *
 * @author Radoslav Husar
 * @since 1.0.0
 */
package org.jgroups.annotations.observability;
