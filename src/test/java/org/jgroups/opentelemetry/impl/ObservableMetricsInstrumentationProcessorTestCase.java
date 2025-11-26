package org.jgroups.opentelemetry.impl;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import org.jgroups.opentelemetry.protocols.OBSERVABLE;
import org.jgroups.protocols.SHARED_LOOPBACK;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ObservableMetricsProcessor using {@link org.jgroups.opentelemetry.protocols.OBSERVABLE}.
 *
 * @author Radoslav Husar
 */
public class ObservableMetricsInstrumentationProcessorTestCase {

    private InMemoryMetricReader metricReader;
    private OpenTelemetry openTelemetry;
    private Meter meter;
    private JChannel channel1;
    private JChannel channel2;

    @BeforeEach
    public void setUp() {
        metricReader = InMemoryMetricReader.create();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(metricReader)
                .build();
        openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();
        meter = openTelemetry.getMeter("org.jgroups.test");
    }

    @AfterEach
    public void tearDown() {
        if (channel1 != null) {
            channel1.close();
        }
        if (channel2 != null) {
            channel2.close();
        }
    }

    @Test
    public void testObservableMetricsAreRegistered() throws Exception {
        // Create a channel with OBSERVABLE protocol
        channel1 = new JChannel(
                new SHARED_LOOPBACK(),
                new OBSERVABLE()
        );
        channel1.connect("test-cluster");

        // Get the test protocol instance
        OBSERVABLE testProtocol = channel1.getProtocolStack().findProtocol(OBSERVABLE.class);
        assertNotNull(testProtocol, "Test protocol should be in the stack");

        // Register metrics using ObservableMetricsProcessor
        ObservableMetricsInstrumentationProcessor processor = new ObservableMetricsInstrumentationProcessor();
        InstrumentationContext context = new BasicInstrumentationContext(testProtocol, meter, true);

        processor.registerMetrics(context);

        // Note: We can't easily count registered metrics anymore without modifying the implementation
        // so we'll verify metrics exist in the collection step

        // Force metric collection
        metricReader.forceFlush();
        Collection<MetricData> metrics = metricReader.collectAllMetrics();

        // Verify both metrics are present
        assertTrue(containsMetric(metrics, "org.jgroups.observable.messages.up"),
                "Should have messages.up metric");
        assertTrue(containsMetric(metrics, "org.jgroups.observable.messages.down"),
                "Should have messages.down metric");
    }

    @Test
    public void testObservableMetricsCountMessages() throws Exception {
        // Create a 2-node cluster
        channel1 = new JChannel(
                new SHARED_LOOPBACK(),
                new OBSERVABLE()
        );
        channel1.setName("Node1");
        channel1.connect("metrics-test-cluster");

        channel2 = new JChannel(
                new SHARED_LOOPBACK(),
                new OBSERVABLE()
        );
        channel2.setName("Node2");
        channel2.connect("metrics-test-cluster");

        // Wait for view to stabilize
        Thread.sleep(500);

        // Get the test protocol from channel1
        OBSERVABLE testProtocol = channel1.getProtocolStack()
                .findProtocol(OBSERVABLE.class);

        // Register metrics for channel1's test protocol
        ObservableMetricsInstrumentationProcessor processor = new ObservableMetricsInstrumentationProcessor();
        InstrumentationContext context = new BasicInstrumentationContext(testProtocol, meter, true);
        processor.registerMetrics(context);

        // Record initial counts
        long initialUp = testProtocol.num_msgs_up.sum();
        long initialDown = testProtocol.num_msgs_down.sum();

        // Send messages from channel1 to channel2
        int messagesToSend = 10;
        for (int i = 0; i < messagesToSend; i++) {
            channel1.send(new ObjectMessage(channel2.getAddress(), "Message " + i));
        }

        // Wait for messages to be processed
        Thread.sleep(500);

        // Check that down count increased
        long newDown = testProtocol.num_msgs_down.sum();
        long downDelta = newDown - initialDown;
        assertTrue(downDelta >= messagesToSend,
                "Should have sent at least " + messagesToSend + " down messages, delta: " + downDelta);

        // Force metric collection
        metricReader.forceFlush();
        Collection<MetricData> metrics = metricReader.collectAllMetrics();

        // Verify metrics were registered and are accessible
        MetricData downMetric = findMetric(metrics, "org.jgroups.observable.messages.down");
        assertNotNull(downMetric, "Should have messages.down metric");
        long downCount = getMetricValue(downMetric);
        assertTrue(downCount >= messagesToSend,
                "Metric should report at least " + messagesToSend + " down messages, got " + downCount);

        MetricData upMetric = findMetric(metrics, "org.jgroups.observable.messages.up");
        assertNotNull(upMetric, "Should have messages.up metric");
        long upCount = getMetricValue(upMetric);
        assertTrue(upCount >= 0, "Up count should be non-negative, got " + upCount);

        System.out.printf("Metrics collected - Up: %d, Down: %d (sent: %d)%n", upCount, downCount, messagesToSend);
    }

    @Test
    public void testObservableMetricsWithZeroMessages() throws Exception {
        // Create a single-node cluster (no message exchange)
        channel1 = new JChannel(
                new SHARED_LOOPBACK(),
                new OBSERVABLE()
        );
        channel1.connect("single-node-cluster");

        // Get the test protocol
        OBSERVABLE testProtocol = channel1.getProtocolStack()
                .findProtocol(OBSERVABLE.class);

        // Register metrics
        ObservableMetricsInstrumentationProcessor processor = new ObservableMetricsInstrumentationProcessor();
        InstrumentationContext context = new BasicInstrumentationContext(testProtocol, meter, true);
        processor.registerMetrics(context);

        // Force metric collection
        metricReader.forceFlush();
        Collection<MetricData> metrics = metricReader.collectAllMetrics();

        // Verify metrics exist with initial values
        MetricData upMetric = findMetric(metrics, "org.jgroups.observable.messages.up");
        MetricData downMetric = findMetric(metrics, "org.jgroups.observable.messages.down");

        assertNotNull(upMetric, "Should have messages.up metric");
        assertNotNull(downMetric, "Should have messages.down metric");

        // Note: Values might be > 0 due to cluster formation messages
        long upCount = getMetricValue(upMetric);
        long downCount = getMetricValue(downMetric);

        assertTrue(upCount >= 0, "Up count should be non-negative");
        assertTrue(downCount >= 0, "Down count should be non-negative");

        System.out.printf("Initial metrics - Up: %d, Down: %d%n", upCount, downCount);
    }

    private boolean containsMetric(Collection<MetricData> metrics, String name) {
        return metrics.stream().anyMatch(m -> m.getName().equals(name));
    }

    private MetricData findMetric(Collection<MetricData> metrics, String name) {
        return metrics.stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private long getMetricValue(MetricData metric) {
        if (metric == null) {
            return -1;
        }
        return metric.getLongGaugeData().getPoints().stream()
                .findFirst()
                .map(LongPointData::getValue)
                .orElse(-1L);
    }
}
