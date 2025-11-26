package org.jgroups.opentelemetry.impl;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import org.jgroups.JChannel;
import org.jgroups.opentelemetry.TestMetricExporter;
import org.jgroups.protocols.*;
import org.jgroups.protocols.opentelemetry.OPENTELEMETRY;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.Protocol;
import org.jgroups.util.Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract base class for testing protocol metrics instrumentation.
 * Subclasses must provide the protocol instance to test and the list of expected metrics.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * class DNS_PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {
 *     @Override
 *     protected Protocol createProtocolInstance() {
 *         return new DNS_PING().dnsQuery("localhost");
 *     }
 *
 *     @Override
 *     protected List<String> getExpectedMetrics() {
 *         return List.of("jgroups.dns_ping.is_coord");
 *     }
 * }
 * }</pre>
 *
 * @author Radoslav Husar
 */
public abstract class AbstractMetricsInstrumentationTestCase {

    private List<JChannel> channels;
    private OpenTelemetry openTelemetry;
    private TestMetricExporter metricExporter;

    /**
     * Creates the protocol instance to be tested.
     * This protocol will be inserted into the test JGroups stack.
     *
     * @return the protocol instance
     */
    protected abstract Protocol createProtocolInstance();

    /**
     * Returns the list of expected metric names that should be registered
     * when the protocol is instrumented.
     *
     * @return list of expected metric names (e.g., "jgroups.dns_ping.is_coord")
     */
    protected abstract List<String> getExpectedMetrics();

    /**
     * Returns the cluster name to use for testing.
     * Default is "test-cluster". Can be overridden by subclasses.
     *
     * @return the cluster name
     */
    protected String getClusterName() {
        return "test-cluster";
    }

    /**
     * Returns the number of nodes to create in the test cluster.
     * Default is 2. Can be overridden by subclasses.
     *
     * @return the number of nodes
     */
    protected int getNumberOfNodes() {
        return 2;
    }

    @BeforeEach
    void setUp() {
        channels = new ArrayList<>();
        metricExporter = new TestMetricExporter();

        // Set up OpenTelemetry with a test metric exporter
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(metricExporter)
                .setInterval(100, TimeUnit.MILLISECONDS)
                .build())
            .build();

        openTelemetry = OpenTelemetrySdk.builder()
            .setMeterProvider(meterProvider)
            .build();
    }

    @AfterEach
    void tearDown() {
        for (JChannel channel : channels) {
            Util.close(channel);
        }
        channels.clear();
    }

    @Test
    void testMetricsAreRegistered() throws Exception {
        // Create cluster with the specified number of nodes
        int numNodes = getNumberOfNodes();
        for (int i = 0; i < numNodes; i++) {
            JChannel channel = createChannelInternal("Node" + (i + 1));
            channels.add(channel);
            channel.connect(getClusterName());
        }

        // Wait for cluster to form
        Util.waitUntilAllChannelsHaveSameView(10000, 500, channels.toArray(new JChannel[0]));

        // Verify cluster size
        assertEquals(numNodes, channels.get(0).getView().size());

        // Print view information for debugging
        System.out.println("\n=== View Information ===");
        for (JChannel channel : channels) {
            System.out.println(channel.getName() + " view: " + channel.getView());
            System.out.println(channel.getName() + " coordinator: " + channel.getView().getCoord());
            // Check is_coord field directly via reflection
            try {
                Protocol discovery = channel.getProtocolStack().findProtocol(createProtocolInstance().getClass());
                if (discovery != null) {
                    java.lang.reflect.Field isCoordField = discovery.getClass().getSuperclass().getDeclaredField("is_coord");
                    isCoordField.setAccessible(true);
                    boolean isCoord = isCoordField.getBoolean(discovery);
                    System.out.println(channel.getName() + " is_coord field value: " + isCoord);
                }
            } catch (Exception e) {
                System.out.println(channel.getName() + " error reading is_coord: " + e.getMessage());
            }
        }
        System.out.println("=========================\n");

        // Allow time for metrics to be collected
        Thread.sleep(500);

        // Get all collected metrics and print name=value pairs with attributes
        // Make a copy to avoid ConcurrentModificationException
        /*
        System.out.println("\n=== Collected Metrics ===");
        List<MetricData> metricsCopy = metricExporter.getExports().stream()
            .flatMap(Collection::stream)
            .toList();

        metricsCopy.forEach(metricData -> {
            String name = metricData.getName();
            metricData.getData().getPoints().forEach(point -> {
                Object value = null;
                if (point instanceof io.opentelemetry.sdk.metrics.data.LongPointData) {
                    value = ((io.opentelemetry.sdk.metrics.data.LongPointData) point).getValue();
                } else if (point instanceof io.opentelemetry.sdk.metrics.data.DoublePointData) {
                    value = ((io.opentelemetry.sdk.metrics.data.DoublePointData) point).getValue();
                }
                // Format attributes as key=value pairs
                StringBuilder attrs = new StringBuilder();
                point.getAttributes().forEach((key, val) -> {
                    if (!attrs.isEmpty()) attrs.append(", ");
                    attrs.append(key.getKey()).append("=").append(val);
                });
                System.out.println(name + "{" + attrs + "} = " + value);
            });
        });
        System.out.println("=========================\n");
         */

        // Get all collected metrics
        Set<String> collectedMetricNames = metricExporter.getExports().stream()
            .flatMap(Collection::stream)
            .map(MetricData::getName)
            .collect(Collectors.toSet());

        // Verify all expected metrics are present
        List<String> expectedMetrics = getExpectedMetrics();
        List<String> missingMetrics = new ArrayList<>();

        for (String expectedMetric : expectedMetrics) {
            if (!collectedMetricNames.contains(expectedMetric)) {
                missingMetrics.add(expectedMetric);
            }
        }

        assertTrue(missingMetrics.isEmpty(),
            "Missing expected metrics: " + missingMetrics +
            "\nCollected metrics: " + collectedMetricNames);
    }


    /**
     * Creates a JChannel with a protocol stack that includes the protocol under test.
     *
     * <p>Subclasses can override this method to customize the protocol stack order
     * for protocols that require specific positioning (e.g., VERIFY_SUSPECT2 must be
     * after discovery but before GMS).</p>
     *
     * <p>The default implementation creates a basic stack with SHARED_LOOPBACK for transport
     * and the protocol under test at position 2. The OPENTELEMETRY protocol will be added
     * automatically by the base class.</p>
     *
     * @param otelProtocol the OPENTELEMETRY protocol instance (already configured with OpenTelemetry)
     * @return the configured JChannel (without name set)
     * @throws Exception if channel creation fails
     */
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            protocolUnderTest,  // Add the protocol being tested
            new NAKACK2(),
            otelProtocol,  // Add OpenTelemetry protocol to the stack with exporter instrumentation already in place
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        );
    }

    /**
     * Private helper that creates the OPENTELEMETRY protocol, creates the channel,
     * and sets the channel name.
     */
    private JChannel createChannelInternal(String name) throws Exception {
        OPENTELEMETRY otel = new OPENTELEMETRY()
            .setOpenTelemetry(openTelemetry);

        return createChannel(otel).name(name);
    }
}
