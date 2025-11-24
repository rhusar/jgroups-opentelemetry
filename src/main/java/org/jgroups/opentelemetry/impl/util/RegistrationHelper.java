package org.jgroups.opentelemetry.impl.util;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.spi.InstrumentationContext;

import java.util.function.Consumer;

/**
 * Helper class for metric registration operations.
 * Provides methods for registering metrics with OpenTelemetry.
 *
 * @author Radoslav Husar
 */
public final class RegistrationHelper {

    private final InstrumentationContext context;

    /**
     * Creates a new registration helper for the given instrumentation context.
     *
     * @param context The instrumentation context
     */
    public RegistrationHelper(InstrumentationContext context) {
        this.context = context;
    }

    /**
     * Registers a long gauge metric with the OpenTelemetry meter.
     * This is a convenience method that wraps the common pattern of creating
     * a gauge builder, configuring it, and building it with a callback.
     * The full metric name is constructed by prepending the protocol prefix from the context.
     * The callback is automatically wrapped to attach context attributes (e.g., cluster name) to measurements.
     *
     * @param nameComponent The metric name component (e.g., "messages.sent"), will be prefixed with the protocol prefix
     * @param description A human-readable description of the metric
     * @param unit The unit of measurement from {@link ObservableUnit}
     * @param callback The callback that records the metric value
     */
    public void registerLongGauge(String nameComponent, String description, ObservableUnit unit, Consumer<ObservableLongMeasurement> callback) {
        String fullName = context.getPrefix() + nameComponent;

        context.meter()
            .gaugeBuilder(fullName)
            .setDescription(description)
            .setUnit(unit.toString())
            .ofLongs()
            .buildWithCallback(measurement -> callback.accept(new ObservableLongMeasurementWrapper(measurement, context)));
    }

    /**
     * Registers an asynchronous long counter metric with the OpenTelemetry meter.
     * Counters are monotonically increasing values (never decrease).
     * The full metric name is constructed by prepending the protocol prefix from the context.
     * The callback is automatically wrapped to attach context attributes (e.g., cluster name) to measurements.
     *
     * @param nameComponent The metric name component (e.g., "messages.sent"), will be prefixed with the protocol prefix
     * @param description A human-readable description of the metric
     * @param unit The unit of measurement from {@link ObservableUnit}
     * @param callback The callback that records the metric value
     */
    public void registerLongCounter(String nameComponent, String description, ObservableUnit unit, Consumer<ObservableLongMeasurement> callback) {
        String fullName = context.getPrefix() + nameComponent;

        context.meter()
            .counterBuilder(fullName)
            .setDescription(description)
            .setUnit(unit.toString())
            .buildWithCallback(measurement -> callback.accept(new ObservableLongMeasurementWrapper(measurement, context)));
    }

    /**
     * Registers an asynchronous long up-down counter metric with the OpenTelemetry meter.
     * Up-down counters can increase or decrease (e.g., number of active connections).
     * The full metric name is constructed by prepending the protocol prefix from the context.
     * The callback is automatically wrapped to attach context attributes (e.g., cluster name) to measurements.
     *
     * @param nameComponent The metric name component (e.g., "connections.active"), will be prefixed with the protocol prefix
     * @param description A human-readable description of the metric
     * @param unit The unit of measurement from {@link ObservableUnit}
     * @param callback The callback that records the metric value
     */
    public void registerLongUpDownCounter(String nameComponent, String description, ObservableUnit unit, Consumer<ObservableLongMeasurement> callback) {
        String fullName = context.getPrefix() + nameComponent;

        context.meter()
            .upDownCounterBuilder(fullName)
            .setDescription(description)
            .setUnit(unit.toString())
            .buildWithCallback(measurement -> callback.accept(new ObservableLongMeasurementWrapper(measurement, context)));
    }

    /**
     * Registers a synchronous long histogram metric with the OpenTelemetry meter.
     * Histograms record distributions of values (e.g., latency measurements).
     * The full metric name is constructed by prepending the protocol prefix from the context.
     *
     * @param nameComponent The metric name component (e.g., "latency"), will be prefixed with the protocol prefix
     * @param description A human-readable description of the metric
     * @param unit The unit of measurement from {@link ObservableUnit}
     * @return The LongHistogram instrument that can be used to record values
     */
    public LongHistogram registerLongHistogram(String nameComponent, String description, ObservableUnit unit) {
        String fullName = context.getPrefix() + nameComponent;

        return context.meter()
            .histogramBuilder(fullName)
            .setDescription(description)
            .setUnit(unit.toString())
            .ofLongs()
            .build();
    }

    /**
     * Wrapper for ObservableLongMeasurement that automatically attaches context attributes.
     */
    private static class ObservableLongMeasurementWrapper implements ObservableLongMeasurement {
        private final ObservableLongMeasurement delegate;
        private final Attributes attributes;

        ObservableLongMeasurementWrapper(ObservableLongMeasurement delegate, InstrumentationContext context) {
            this.delegate = delegate;
            this.attributes = context.getAttributes();
        }

        @Override
        public void record(long value) {
            delegate.record(value, attributes);
        }

        @Override
        public void record(long value, Attributes attributes) {
            delegate.record(value, attributes);
        }
    }
}
