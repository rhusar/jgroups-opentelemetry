package org.jgroups.annotations.observability;

/**
 * Defines the scope of an observable metric, distinguishing between runtime operational
 * metrics and configuration/static metrics.
 *
 * <p>This classification allows metric collection systems to selectively export metrics
 * based on their scope, providing control over what gets exposed to telemetry backends.</p>
 *
 * @author Radoslav Husar
 */
public enum ObservableScope {
    /**
     * Runtime operational metrics that change during normal protocol operation.
     * These metrics represent the dynamic state and behavior of the protocol.
     *
     * <p>Examples include:</p>
     * <ul>
     *   <li>Message counters (messages sent, received, dropped)</li>
     *   <li>Current queue sizes</li>
     *   <li>Active connection counts</li>
     *   <li>Retransmission rates</li>
     * </ul>
     *
     * <p>Runtime metrics are always exported regardless of configuration settings.</p>
     */
    RUNTIME,

    /**
     * Configuration metrics that represent protocol settings and thresholds.
     * These metrics typically derive from protocol properties and change infrequently
     * (usually only at startup or via runtime reconfiguration).
     *
     * <p>Examples include:</p>
     * <ul>
     *   <li>Threshold values (min/max bounds)</li>
     *   <li>Capacity limits (queue size, buffer size)</li>
     *   <li>Enabled/disabled flags</li>
     *   <li>Timeout values</li>
     * </ul>
     *
     * <p>Configuration metrics provide important context for understanding operational metrics
     * and detecting configuration drift across cluster nodes. They are only exported when
     * {@code includeConfigurationMetrics} is enabled on the OPENTELEMETRY protocol.</p>
     */
    CONFIGURATION
}
