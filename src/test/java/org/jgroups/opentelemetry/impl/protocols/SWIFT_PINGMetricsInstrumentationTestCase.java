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
 * Test case for {@link SWIFT_PINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
@Disabled("Requires OpenStack Swift authentication")
class SWIFT_PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        // Provide minimal required configuration for testing
        return new SWIFT_PING()
            .setValue("auth_url", "http://localhost:5000/v2.0")
            .setValue("tenant", "test-tenant")
            .setValue("username", "test-user")
            .setValue("password", "test-password");
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Discovery metrics
            "jgroups.swift_ping.is_coord",
            "jgroups.swift_ping.discovery_requests",
            // FILE_PING metrics
            "jgroups.swift_ping.writes",
            "jgroups.swift_ping.reads"
        );
    }

    /**
     * SWIFT_PING requires a transport and GMS.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            protocolUnderTest,           // SWIFT_PING
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        );
    }
}
