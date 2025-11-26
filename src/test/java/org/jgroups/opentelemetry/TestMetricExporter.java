package org.jgroups.opentelemetry;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Simple test metric exporter for capturing metrics in tests.
 * This exporter stores all exported metrics in memory for later inspection.
 *
 * @author Radoslav Husar
 */
public class TestMetricExporter implements MetricExporter {

    private final List<Collection<MetricData>> exports = new ArrayList<>();

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
        exports.add(metrics);
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return AggregationTemporality.CUMULATIVE;
    }

    /**
     * Returns all exported metric collections.
     *
     * @return list of all metric data collections that have been exported
     */
    public List<Collection<MetricData>> getExports() {
        return exports;
    }
}
