package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.JChannel;
import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.*;
import org.jgroups.protocols.opentelemetry.OPENTELEMETRY;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.Protocol;

import java.util.List;

/**
 * Test case for {@link FD_ALL3MetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class FD_ALL3MetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new FD_ALL3();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Runtime metrics
            "jgroups.fd_all3.heartbeats.sent",
            "jgroups.fd_all3.heartbeats.received",
            "jgroups.fd_all3.suspect_events",
            "jgroups.fd_all3.has_suspected_members",
            "jgroups.fd_all3.timeout_checker.running",
            "jgroups.fd_all3.heartbeat_sender.running",
            // Configuration metrics
            "jgroups.fd_all3.timeout",
            "jgroups.fd_all3.interval",
            "jgroups.fd_all3.num_bits"
        );
    }

    /**
     * FD_ALL3 requires a discovery protocol and GMS.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            new SHARED_LOOPBACK_PING(),
            protocolUnderTest,           // FD_ALL3
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        );
    }
}
