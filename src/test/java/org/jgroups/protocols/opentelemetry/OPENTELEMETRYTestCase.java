package org.jgroups.protocols.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import org.jgroups.opentelemetry.TestMetricExporter;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.util.Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the OPENTELEMETRY protocol integration with JGroups.
 *
 * @author Radoslav Husar
 */
class OPENTELEMETRYTestCase {

    private List<JChannel> channels;
    private OpenTelemetry openTelemetry;

    @BeforeEach
    void setUp() {
        channels = new ArrayList<>();
        TestMetricExporter metricExporter = new TestMetricExporter();

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
    void testClusterFormationWithOpenTelemetryProtocol() throws Exception {
        // Create first channel
        JChannel channel1 = createChannel("A");
        channels.add(channel1);
        channel1.connect("test-cluster");

        // Create second channel
        JChannel channel2 = createChannel("B");
        channels.add(channel2);
        channel2.connect("test-cluster");

        // Wait for cluster to form
        Util.waitUntilAllChannelsHaveSameView(10000, 500, channel1, channel2);

        // Verify cluster size
        assertEquals(2, channel1.getView().size());
        assertEquals(2, channel2.getView().size());

        // Send some messages to generate metrics
        for (int i = 0; i < 10; i++) {
            channel1.send(new ObjectMessage(null, "Message " + i));
        }

        // Allow time for metrics to be collected
        Thread.sleep(500);

        // Verify both channels are connected
        assertTrue(channel1.isConnected());
        assertTrue(channel2.isConnected());
    }

    @Test
    void testMessageExchangeWithMetrics() throws Exception {
        // Create a cluster with three nodes
        JChannel channel1 = createChannel("Node1");
        JChannel channel2 = createChannel("Node2");
        JChannel channel3 = createChannel("Node3");

        channels.add(channel1);
        channels.add(channel2);
        channels.add(channel3);

        channel1.connect("metrics-cluster");
        channel2.connect("metrics-cluster");
        channel3.connect("metrics-cluster");

        // Wait for cluster to form
        Util.waitUntilAllChannelsHaveSameView(10000, 500, channel1, channel2, channel3);

        // Verify all nodes see the same view
        assertEquals(3, channel1.getView().size());
        assertEquals(3, channel2.getView().size());
        assertEquals(3, channel3.getView().size());

        // Send messages from each node
        channel1.send(new ObjectMessage(null, "Hello from Node1"));
        channel2.send(new ObjectMessage(null, "Hello from Node2"));
        channel3.send(new ObjectMessage(null, "Hello from Node3"));

        // Allow metrics to be collected
        Thread.sleep(500);

        // Verify all channels are still connected
        assertTrue(channel1.isConnected());
        assertTrue(channel2.isConnected());
        assertTrue(channel3.isConnected());
    }

    /**
     * Creates a JChannel with a protocol stack that includes OPENTELEMETRY.
     */
    private JChannel createChannel(String name) throws Exception {
        OPENTELEMETRY otel = new OPENTELEMETRY()
            .setOpenTelemetry(openTelemetry);

        return new JChannel(
            new SHARED_LOOPBACK(),
            new SHARED_LOOPBACK_PING(),
            new NAKACK2(),
            otel,  // Add OpenTelemetry protocol to the stack
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        ).name(name);
    }
}
