package org.jgroups.opentelemetry.demo;

import org.jgroups.*;
import org.jgroups.protocols.*;
import org.jgroups.protocols.opentelemetry.OPENTELEMETRY;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.Protocol;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demo application showing JGroups OpenTelemetry metrics integration.
 *
 * This application creates a JGroups cluster and continuously sends messages
 * to generate observable metrics that are exported to the OTLP collector.
 *
 * @author Radoslav Husar
 */
public class MetricsDemo implements Receiver {

    private final JChannel channel;
    private final String nodeName;
    private final AtomicLong messageCounter = new AtomicLong(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public MetricsDemo(String nodeName) throws Exception {
        this.nodeName = nodeName;

        // Create JGroups channel with OpenTelemetry protocol
        Protocol[] protocols = new Protocol[] {
            new UDP(),
            new PING(),
            new MERGE3(),
            new FD_SOCK(),
            new FD_ALL3(),
            new VERIFY_SUSPECT2(),
            new NAKACK2(),
            new OPENTELEMETRY()
                .setEndpoint("http://localhost:4317")
                .setExportInterval(10000)  // Export every 10 seconds
                .setInstrumentationScopeName("org.jgroups.demo")
                .setExposeConfigurationMetrics(true)
                .setEnableMessageSizeHistogram(true),  // Track message size distribution
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new UFC(),
            new MFC(),
            new FRAG2()
        };

        channel = new JChannel(protocols);
        channel.setName(nodeName);
        channel.setReceiver(this);
    }

    public void start() throws Exception {
        System.out.println("Starting JGroups node: " + nodeName);
        System.out.println("Connecting to cluster...");
        channel.connect("metrics-demo-cluster");

        System.out.println("Connected! Local address: " + channel.getAddress());
        System.out.println("Cluster view: " + channel.getView());
        System.out.println("\nMetrics are being exported to OTLP endpoint: http://localhost:4317");
        System.out.println("View metrics in Grafana: http://localhost:3000\n");

        // Start sending messages periodically
        scheduler.scheduleAtFixedRate(this::sendMessage, 2, 50, TimeUnit.MILLISECONDS);

        // Keep the application running
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        System.out.println("\nShutting down...");
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        channel.close();
        System.out.println("Node stopped.");
    }

    private void sendMessage() {
        try {
            long count = messageCounter.incrementAndGet();
            String payload = String.format("Message #%d from %s at %d",
                count, nodeName, System.currentTimeMillis());

            Message msg = new ObjectMessage(null, payload);
            channel.send(msg);

            System.out.printf("[SENT] %s (total sent: %d)%n", payload, count);
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }

    public void receive(Message msg) {
        String payload = msg.getObject();
        System.out.printf("[RECEIVED] %s from %s%n", payload, msg.getSrc());
    }

    public void viewAccepted(View newView) {
        System.out.printf("\n[VIEW CHANGE] New view: %s (members: %d)%n",
            newView.getViewId(), newView.size());
        System.out.println("Members: " + newView.getMembers());
        System.out.println("Coordinator: " + newView.getCoord());
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java -jar jgroups-opentelemetry-demo.jar <node-name>");
            System.err.println("Example: java -jar jgroups-opentelemetry-demo.jar node1");
            System.exit(1);
        }

        String nodeName = args[0];
        MetricsDemo demo = new MetricsDemo(nodeName);
        demo.start();

        // Keep main thread alive
        Thread.currentThread().join();
    }
}
