package org.jgroups.annotations.observability;

/**
 * Enumeration of OpenTelemetry asynchronous metric types for the @Observable annotation.
 *
 * <p>These types correspond to OpenTelemetry's asynchronous (callback-based) instruments,
 * which are appropriate for metrics read from existing fields or computed via methods
 * rather than recorded inline with operations.</p>
 *
 * <p>OpenTelemetry defines six main instrument types:</p>
 * <ul>
 *   <li><b>Synchronous instruments</b>: Counter, UpDownCounter, Histogram (recorded inline)</li>
 *   <li><b>Asynchronous instruments</b>: ObservableCounter, ObservableUpDownCounter, ObservableGauge (via callbacks)</li>
 * </ul>
 *
 * <p>Since @Observable is designed for callback-based observation of existing state,
 * these types map to the asynchronous variants.</p>
 *
 * @author Radoslav Husar
 * @see <a href="https://opentelemetry.io/docs/specs/otel/metrics/api/">OpenTelemetry Metrics API</a>
 */
public enum ObservableType {

    /**
     * An asynchronous counter (ObservableCounter) for monotonically increasing values.
     * Used for cumulative metrics that only increase over time (e.g., total messages sent).
     *
     * <p><b>Note:</b> The OpenTelemetry Java SDK currently implements this using
     * asynchronous gauges, as native async counters are not yet fully supported.</p>
     */
    COUNTER,

    /**
     * An asynchronous up-down counter (ObservableUpDownCounter) for values that can increase or decrease.
     * Used for metrics that fluctuate (e.g., number of active connections, current queue size).
     *
     * <p><b>Note:</b> The OpenTelemetry Java SDK currently implements this using
     * asynchronous gauges, as native async up-down counters are not yet fully supported.</p>
     */
    UP_DOWN_COUNTER,

    /**
     * An asynchronous gauge (ObservableGauge) representing a current value at a point in time.
     * Used for metrics that represent a snapshot of state (e.g., memory usage, temperature).
     *
     * <p>Unlike counters, gauges have no inherent meaning between measurements - each
     * observation is independent.</p>
     */
    GAUGE,

    /**
     * A histogram for recording statistical distributions of values.
     *
     * <p><b>Note:</b> Histograms are typically synchronous instruments in OpenTelemetry
     * (recorded inline with operations). This type is included for completeness but
     * may have limited support in asynchronous observation contexts.</p>
     *
     * <p>For most use cases with @Observable, consider using GAUGE instead.</p>
     */
    HISTOGRAM
}
