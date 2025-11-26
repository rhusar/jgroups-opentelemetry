package org.jgroups.protocols.opentelemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import org.jgroups.annotations.MBean;
import org.jgroups.annotations.Property;
import org.jgroups.conf.AttributeType;
import org.jgroups.opentelemetry.impl.MetricsRegistrar;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.stack.Protocol;

import java.time.Duration;

/**
 * JGroups protocol that provides OpenTelemetry metrics integration.
 * This protocol automatically discovers and registers metrics from protocols in the stack
 * using ServiceLoader to find MetricsInstrumentation implementations.
 *
 * <p>Metrics instrumentation is discovered automatically via the ServiceLoader mechanism.
 * To add new instrumentation, implement {@link MetricsInstrumentation} with a public no-arg
 * constructor and annotate it with {@code @MetaInfServices(MetricsInstrumentation.class)}.</p>
 *
 * <p>The protocol can either use a programmatically configured OpenTelemetry instance via
 * {@link #setOpenTelemetry(OpenTelemetry)}, or automatically configure an OTLP exporter
 * by setting the {@code endpoint} property.</p>
 *
 * @author Radoslav Husar
 */
@MBean(description = "Protocol that instruments JGroups protocols with OpenTelemetry metrics")
public class OPENTELEMETRY extends Protocol {

    @Property(description = "Instrumentation scope name for OpenTelemetry meter",
            systemProperty = {"jgroups.opentelemetry.scope_name", "JGROUPS_OPENTELEMETRY_SCOPE_NAME"})
    protected String instrumentationScopeName = "org.jgroups";

    @Property(description = "Whether to expose protocol configuration values as metrics (e.g., thresholds, enabled flags, capacity limits)",
            systemProperty = {"jgroups.opentelemetry.expose_configuration_metrics", "JGROUPS_OPENTELEMETRY_EXPOSE_CONFIGURATION_METRICS"})
    protected boolean exposeConfigurationMetrics = true;

    @Property(description = "OTLP endpoint URL for metrics export (e.g., http://localhost:4317). If set, OpenTelemetry SDK will be automatically configured",
            systemProperty = {"jgroups.opentelemetry.endpoint", "JGROUPS_OPENTELEMETRY_ENDPOINT"})
    protected String endpoint;

    @Property(description = "Interval in milliseconds for periodic metric export",
            type = AttributeType.TIME,
            systemProperty = {"jgroups.opentelemetry.export_interval", "JGROUPS_OPENTELEMETRY_EXPORT_INTERVAL"})
    protected long exportInterval = 60000; // 60 seconds default

    protected OpenTelemetry openTelemetry;
    protected SdkMeterProvider meterProvider;
    protected boolean sdkCreatedByProtocol = false;

    public OPENTELEMETRY() {
    }

    public OPENTELEMETRY setOpenTelemetry(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        return this;
    }

    public String getInstrumentationScopeName() {
        return instrumentationScopeName;
    }

    public OPENTELEMETRY setInstrumentationScopeName(String instrumentationScopeName) {
        this.instrumentationScopeName = instrumentationScopeName;
        return this;
    }

    public boolean isExposeConfigurationMetrics() {
        return exposeConfigurationMetrics;
    }

    public OPENTELEMETRY setExposeConfigurationMetrics(boolean exposeConfigurationMetrics) {
        this.exposeConfigurationMetrics = exposeConfigurationMetrics;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public OPENTELEMETRY setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public long getExportInterval() {
        return exportInterval;
    }

    public OPENTELEMETRY setExportInterval(long exportInterval) {
        this.exportInterval = exportInterval;
        return this;
    }

    @Override
    public void init() throws Exception {
        super.init();

        // If no OpenTelemetry instance provided and endpoint is configured, create SDK automatically
        if (openTelemetry == null && endpoint != null && !endpoint.isEmpty()) {
            openTelemetry = createOpenTelemetrySdk();
            sdkCreatedByProtocol = true;
        }

        // Fall back to global instance if still null
        if (openTelemetry == null) {
            openTelemetry = GlobalOpenTelemetry.get();
        }

        if (openTelemetry != null) {
            MetricsRegistrar.registerMetrics(openTelemetry, getProtocolStack(), instrumentationScopeName, exposeConfigurationMetrics);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        // Clean up SDK if we created it
        if (sdkCreatedByProtocol && meterProvider != null) {
            meterProvider.close();
            meterProvider = null;
            openTelemetry = null;
            sdkCreatedByProtocol = false;
        }
    }

    /**
     * Creates an OpenTelemetry SDK instance configured with OTLP gRPC exporter.
     *
     * @return configured OpenTelemetry SDK instance
     */
    protected OpenTelemetry createOpenTelemetrySdk() {
        log.info("Creating OpenTelemetry SDK with OTLP endpoint: %s, export interval: %d ms",
                endpoint, exportInterval);

        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint(endpoint)
                .build();

        PeriodicMetricReader metricReader = PeriodicMetricReader.builder(metricExporter)
                .setInterval(Duration.ofMillis(exportInterval))
                .build();

        meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(metricReader)
                .build();

        return OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();
    }

}
