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
 *   <li><b>Custom units</b>: Enclosed in curly braces (e.g., {@code {messages}}, {@code {connections}})</li>
 *   <li><b>Dimensionless</b>: Represented as {@code 1} for ratios or percentages</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * @Observable(
 *     name = "jgroups.unicast3.messages.sent",
 *     type = ObservableType.COUNTER,
 *     unit = ObservableUnit.MESSAGES,
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
     * Messages unit - for counting discrete messages exchanged in the cluster.
     * <p>UCUM representation: {@code {messages}}</p>
     * <p>Use for: Message counters, message queue sizes, messages in flight</p>
     */
    MESSAGES("{messages}"),

    /**
     * Bytes unit - for measuring data size in bytes.
     * <p>UCUM representation: {@code By}</p>
     * <p>Use for: Message payload sizes, buffer sizes, memory usage</p>
     * <p><b>Note:</b> UCUM uses {@code By} (capital B) for bytes to distinguish from bits ({@code b})</p>
     */
    BYTES("By"),

    /**
     * Connections unit - for counting network connections.
     * <p>UCUM representation: {@code {connections}}</p>
     * <p>Use for: Active TCP connections, connection pool sizes, peer connections</p>
     */
    CONNECTIONS("{connections}"),

    /**
     * Requests unit - for counting requests sent or received.
     * <p>UCUM representation: {@code {requests}}</p>
     * <p>Use for: Retransmit requests, RPC requests, API calls</p>
     */
    REQUESTS("{requests}"),

    /**
     * Acknowledgments unit - for counting protocol acknowledgments.
     * <p>UCUM representation: {@code {acks}}</p>
     * <p>Use for: ACK messages, confirmation responses, protocol handshakes</p>
     */
    ACKS("{acks}"),

    /**
     * Operations unit - for counting database or I/O operations.
     * <p>UCUM representation: {@code {operations}}</p>
     * <p>Use for: Database reads, writes, removes, file operations</p>
     */
    OPERATIONS("{operations}"),

    /**
     * Members unit - for counting cluster members.
     * <p>UCUM representation: {@code {members}}</p>
     * <p>Use for: Discovered members, cluster size, peer counts</p>
     */
    MEMBERS("{members}"),

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
     * Dimensionless unit - for ratios, percentages, or pure counts without specific units.
     * <p>UCUM representation: {@code 1}</p>
     * <p>Use for: Utilization ratios (0.0-1.0), error rates, percentages, multipliers</p>
     * <p><b>Note:</b> For percentages, store as decimal (0.95 for 95%) and let visualization
     * tools format appropriately</p>
     */
    DIMENSIONLESS("1");

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
