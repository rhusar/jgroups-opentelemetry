package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.JChannel;
import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.*;
import org.jgroups.protocols.opentelemetry.OPENTELEMETRY;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.Protocol;
import org.junit.jupiter.api.Disabled;

import java.util.List;

/**
 * Test case for {@link TCPPINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
@Disabled("Requires initial_hosts configuration and network discovery")
class TCPPINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        // Provide minimal required configuration for testing
        // Set dummy initial hosts (won't be used in SHARED_LOOPBACK)
        return new TCPPING()
            .setValue("initial_hosts", "localhost[7800]")
            .setValue("port_range", "0");
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Discovery metrics
            "jgroups.tcpping.is_coord",
            "jgroups.tcpping.discovery_requests"
        );
    }

    /**
     * TCPPING requires a transport and GMS.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            protocolUnderTest,           // TCPPING
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        );
    }
}
