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
 * Test case for {@link FD_SOCK2MetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class FD_SOCK2MetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new FD_SOCK2();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Runtime metrics
            "jgroups.fd_sock2.suspects",
            "jgroups.fd_sock2.suspect_events",
            // Configuration metrics
            "jgroups.fd_sock2.offset",
            "jgroups.fd_sock2.port.range",
            "jgroups.fd_sock2.port.client_bind",
            "jgroups.fd_sock2.port.min",
            "jgroups.fd_sock2.port.max",
            "jgroups.fd_sock2.interval.suspect_msg",
            "jgroups.fd_sock2.timeout.connect",
            "jgroups.fd_sock2.linger"
        );
    }

    /**
     * FD_SOCK2 requires a discovery protocol and GMS.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            new SHARED_LOOPBACK_PING(),
            protocolUnderTest,           // FD_SOCK2
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        );
    }
}
