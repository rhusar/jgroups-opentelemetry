/**
 * Test cases for JGroups protocol metrics instrumentation.
 *
 * <p>These tests verify that OpenTelemetry metrics instrumentation implementations
 * correctly register and expose metrics from their respective JGroups protocols.
 * As JGroups core evolves, protocol implementations may change their internal state,
 * add or remove fields, or modify behavior. This test suite ensures that the
 * instrumentation classes remain compatible with the current JGroups protocol
 * implementations and do not break when accessing protocol internals via reflection.</p>
 *
 * <p>Each test case extends {@link org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase} and verifies:</p>
 * <ul>
 *   <li>The expected metrics are properly registered with OpenTelemetry</li>
 *   <li>The JGroups cluster forms correctly with the instrumented protocol</li>
 *   <li>Metrics can be successfully collected without runtime errors</li>
 * </ul>
 *
 * @author Radoslav Husar
 */
package org.jgroups.opentelemetry.impl.protocols;
