package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.TCPGOSSIP;
import org.jgroups.stack.Protocol;
import org.junit.jupiter.api.Disabled;

import java.util.List;

/**
 * Test case for {@link TCPGOSSIPMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
@Disabled("Disabled - TCPGOSSIP requires GossipRouter server")
class TCPGOSSIPMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new TCPGOSSIP();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // TCPGOSSIP extends Discovery, so it should expose Discovery metrics
        return List.of("jgroups.tcpgossip.is_coord", "jgroups.tcpgossip.discovery_requests");
    }
}
