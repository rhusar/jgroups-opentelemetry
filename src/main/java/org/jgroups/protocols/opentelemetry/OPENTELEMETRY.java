package org.jgroups.protocols.opentelemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import org.jgroups.Message;
import org.jgroups.annotations.MBean;
import org.jgroups.annotations.Property;
import org.jgroups.conf.AttributeType;
import org.jgroups.opentelemetry.impl.MetricsRegistrar;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.stack.Protocol;
import org.jgroups.util.MessageBatch;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

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

    @Property(description = "Enable message size histogram tracking (OpenTelemetry-native replacement for SIZE/SIZE2 protocols). " +
            "Records distribution of sent and received message sizes for performance analysis. Disabled by default due to performance overhead.",
            systemProperty = {"jgroups.opentelemetry.enable_message_size_histogram", "JGROUPS_OPENTELEMETRY_ENABLE_MESSAGE_SIZE_HISTOGRAM"})
    protected boolean enableMessageSizeHistogram = false;

    @Property(description = "When true, use Message.size() for histogram, otherwise Message.getLength()")
    protected boolean useTotalSize = false;

    protected OpenTelemetry openTelemetry;
    protected SdkMeterProvider meterProvider;
    protected boolean sdkCreatedByProtocol = false;

    // Message size histograms
    protected LongHistogram messageSizeSent;
    protected LongHistogram messageSizeReceived;

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

    public boolean isEnableMessageSizeHistogram() {
        return enableMessageSizeHistogram;
    }

    public OPENTELEMETRY setEnableMessageSizeHistogram(boolean enableMessageSizeHistogram) {
        this.enableMessageSizeHistogram = enableMessageSizeHistogram;
        return this;
    }

    public boolean isUseTotalSize() {
        return useTotalSize;
    }

    public OPENTELEMETRY setUseTotalSize(boolean useTotalSize) {
        this.useTotalSize = useTotalSize;
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

            // Initialize message size histograms if enabled
            if (enableMessageSizeHistogram) {
                Meter meter = openTelemetry.getMeter(instrumentationScopeName);
                messageSizeSent = meter.histogramBuilder("jgroups.opentelemetry.message.size.sent")
                    .setDescription("Distribution of sent message sizes")
                    .setUnit("By")
                    .ofLongs()
                    .build();

                messageSizeReceived = meter.histogramBuilder("jgroups.opentelemetry.message.size.received")
                    .setDescription("Distribution of received message sizes")
                    .setUnit("By")
                    .ofLongs()
                    .build();

                log.info("Message size histogram tracking enabled (useTotalSize=%b)", useTotalSize);
            }
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

    @Override
    public Object down(Message msg) {
        if (enableMessageSizeHistogram && messageSizeSent != null) {
            recordMessageSize(msg, messageSizeSent);
        }
        return down_prot.down(msg);
    }

    @Override
    public CompletableFuture<Object> down(Message msg, boolean async) {
        if (enableMessageSizeHistogram && messageSizeSent != null) {
            recordMessageSize(msg, messageSizeSent);
        }
        return down_prot.down(msg, async);
    }

    @Override
    public Object up(Message msg) {
        if (enableMessageSizeHistogram && messageSizeReceived != null) {
            recordMessageSize(msg, messageSizeReceived);
        }
        return up_prot.up(msg);
    }

    @Override
    public void up(MessageBatch batch) {
        if (enableMessageSizeHistogram && messageSizeReceived != null) {
            for (Message msg : batch) {
                recordMessageSize(msg, messageSizeReceived);
            }
        }
        up_prot.up(batch);
    }

    protected void recordMessageSize(Message msg, LongHistogram histogram) {
        long size = useTotalSize ? msg.size() : msg.getLength();
        histogram.record(size);
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
