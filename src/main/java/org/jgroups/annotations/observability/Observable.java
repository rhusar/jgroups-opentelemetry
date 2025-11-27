package org.jgroups.annotations.observability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark protocol fields or methods that should be exported as OpenTelemetry metrics.
 * This annotation provides a declarative way to define metrics without writing boilerplate code.
 *
 * <p>Metric names are automatically prefixed with {@code jgroups.{protocol}.} where protocol is the
 * lowercase protocol class name (e.g., UNICAST3 becomes {@code jgroups.unicast3.}).</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * public class UNICAST3 extends Protocol {
 *     @Observable(name = "messages.sent",
 *                 type = ObservableType.COUNTER,
 *                 unit = ObservableUnit.UNITY,
 *                 description = "Total number of unicast messages sent")
 *     protected final LongAdder num_msgs_sent = new LongAdder();
 *     // Results in metric name: jgroups.unicast3.messages.sent
 * }
 * }
 * </pre>
 *
 * <p>The annotation can be applied to:</p>
 * <ul>
 *   <li>Fields (e.g., LongAdder, AtomicLong, primitive types)</li>
 *   <li>Methods (e.g., getters that return metric values)</li>
 * </ul>
 *
 * @author Radoslav Husar
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Observable {

    /**
     * The metric name suffix (without the {@code jgroups.{protocol}.} prefix).
     * The full metric name will be {@code jgroups.{protocol}.{name}}.
     * <p>If not specified, a name will be derived from the field/method name
     * (e.g., {@code num_msgs_sent} becomes {@code num.msgs.sent}).</p>
     *
     * <p>Example: {@code name = "messages.sent"} in UNICAST3 becomes {@code jgroups.unicast3.messages.sent}</p>
     *
     * @return The metric name suffix (without protocol prefix)
     */
    String name() default "";

    /**
     * The type of metric to create (Counter, Gauge, Histogram, etc.).
     * This determines how OpenTelemetry will collect and aggregate the metric.
     *
     * @return The metric type
     */
    ObservableType type();

    /**
     * The unit of measurement for this metric.
     * Uses UCUM (Unified Code for Units of Measure) conventions.
     *
     * @return The unit type
     */
    ObservableUnit unit() default ObservableUnit.UNITY;

    /**
     * An optional human-readable description of what this metric measures.
     * This will be included in the metric metadata.
     *
     * @return The metric description
     */
    String description() default "";

    /**
     * The scope of this metric, indicating whether it's runtime operational data or configuration.
     *
     * <p>{@link ObservableScope#RUNTIME} metrics are always exported and represent dynamic protocol
     * behavior (e.g., message counts, queue sizes, active connections).</p>
     *
     * <p>{@link ObservableScope#CONFIGURATION} metrics are only exported when the
     * {@code exposeConfigurationMetrics} option is enabled on the OPENTELEMETRY protocol.
     * These represent protocol settings and thresholds (e.g., min/max bounds, capacity limits,
     * enabled/disabled flags).</p>
     *
     * <p>Default is {@link ObservableScope#RUNTIME}.</p>
     *
     * @return The metric scope
     */
    ObservableScope scope() default ObservableScope.RUNTIME;
}
