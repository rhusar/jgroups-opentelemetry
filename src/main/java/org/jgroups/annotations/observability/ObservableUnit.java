package org.jgroups.annotations.observability;

/**
 * Enumeration of metric units following UCUM (Unified Code for Units of Measure) conventions.
 * Defines standardized units for JGroups metrics exported to OpenTelemetry.
 *
 * <p>UCUM provides a standard, case-sensitive coding system for units of measure that ensures
 * consistent interpretation across different observability platforms and tools. OpenTelemetry
 * recommends using UCUM codes for metric units to enable proper visualization, aggregation,
 * and comparison of metrics.</p>
 *
 * <p><b>UCUM Notation:</b></p>
 * <ul>
 *   <li><b>Standard units</b>: Use standard UCUM codes (e.g., {@code s} for seconds, {@code By} for bytes)</li>
 *   <li><b>Unity</b>: Represented as {@code 1} for ratios, percentages, or discrete counts</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * @Observable(
 *     name = "jgroups.unicast3.messages.sent",
 *     type = ObservableType.COUNTER,
 *     unit = ObservableUnit.UNITY,
 *     description = "Total unicast messages sent"
 * )
 * protected final LongAdder num_msgs_sent = new LongAdder();
 * }</pre>
 *
 * @author Radoslav Husar
 * @see <a href="https://ucum.org/ucum">UCUM Specification</a>
 * @see <a href="https://opentelemetry.io/docs/specs/semconv/general/metrics/">OpenTelemetry Semantic Conventions for Metrics</a>
 */
public enum ObservableUnit {

    /**
     * Unity or dimensionless - for ratios, percentages, or discrete counts.
     * <p>UCUM representation: {@code 1}</p>
     * <p>Use for: Utilization ratios (0.0-1.0), error rates, percentages, multipliers, discrete counts
     * (messages, connections, requests, acknowledgments, operations, members, etc.)</p>
     * <p><b>Note:</b> For percentages, store as decimal (0.95 for 95%) and let visualization
     * tools format appropriately. This is the standard unit for all discrete counting metrics.</p>
     */
    UNITY("1"),

    /**
     * Nanoseconds unit - for high-precision time measurements in nanoseconds.
     * <p>UCUM representation: {@code ns}</p>
     * <p>Use for: High-precision latency, sub-millisecond timing, performance profiling</p>
     */
    NANOSECONDS("ns"),

    /**
     * Milliseconds unit - for time measurements in milliseconds.
     * <p>UCUM representation: {@code ms}</p>
     * <p>Use for: Latency, response times, timeouts, durations under 1 second</p>
     */
    MILLISECONDS("ms"),

    /**
     * Seconds unit - for time measurements in seconds.
     * <p>UCUM representation: {@code s}</p>
     * <p>Use for: Uptime, long-running operation durations, intervals</p>
     */
    SECONDS("s"),

    /**
     * Bytes unit - for measuring data size in bytes.
     * <p>UCUM representation: {@code By}</p>
     * <p>Use for: Message payload sizes, buffer sizes, memory usage</p>
     * <p><b>Note:</b> UCUM uses {@code By} (capital B) for bytes to distinguish from bits ({@code b})</p>
     */
    BYTES("By"),

    /**
     * Kilobytes unit - for measuring data size in kilobytes (1000 bytes).
     * <p>UCUM representation: {@code kBy}</p>
     * <p>Use for: Larger message sizes, cache sizes, throughput measurements</p>
     */
    KILOBYTES("kBy"),

    /**
     * Megabytes unit - for measuring data size in megabytes (1000000 bytes).
     * <p>UCUM representation: {@code MBy}</p>
     * <p>Use for: File sizes, memory allocations, large data transfers</p>
     */
    MEGABYTES("MBy");

    private final String ucumCode;

    ObservableUnit(String ucumCode) {
        this.ucumCode = ucumCode;
    }

    /**
     * Returns the UCUM code for this unit.
     *
     * @return The UCUM representation of the unit
     */
    @Override
    public String toString() {
        return ucumCode;
    }
}
