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
 * Test case for {@link RACKSPACE_PINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
@Disabled("Requires Rackspace cloud credentials and authentication")
class RACKSPACE_PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        // Provide minimal required configuration for testing
        return new RACKSPACE_PING()
            .setValue("username", "test-user")
            .setValue("apiKey", "test-api-key")
            .setValue("region", "US");
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Discovery metrics
            "jgroups.rackspace_ping.is_coord",
            "jgroups.rackspace_ping.discovery_requests",
            // FILE_PING metrics
            "jgroups.rackspace_ping.writes",
            "jgroups.rackspace_ping.reads"
        );
    }

    /**
     * RACKSPACE_PING requires a transport and GMS.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            protocolUnderTest,           // RACKSPACE_PING
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        );
    }
}
