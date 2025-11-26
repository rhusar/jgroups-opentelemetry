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
 * Test case for {@link FD_SOCKMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class FD_SOCKMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new FD_SOCK();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Runtime metrics
            "jgroups.fd_sock.suspects",
            "jgroups.fd_sock.suspect_events",
            "jgroups.fd_sock.monitor.running",
            // Configuration metrics
            "jgroups.fd_sock.timeout.get_cache",
            "jgroups.fd_sock.timeout.sock_conn",
            "jgroups.fd_sock.interval.suspect_msg",
            "jgroups.fd_sock.cache.max_elements",
            "jgroups.fd_sock.cache.max_age",
            "jgroups.fd_sock.num_tries",
            "jgroups.fd_sock.port.start",
            "jgroups.fd_sock.port.client_bind",
            "jgroups.fd_sock.port.range",
            "jgroups.fd_sock.keep_alive"
        );
    }

    /**
     * FD_SOCK requires a discovery protocol and GMS.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            new SHARED_LOOPBACK_PING(),
            protocolUnderTest,           // FD_SOCK
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        );
    }
}
