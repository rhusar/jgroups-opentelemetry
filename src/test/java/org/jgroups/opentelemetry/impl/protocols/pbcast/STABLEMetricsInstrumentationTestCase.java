package org.jgroups.opentelemetry.impl.protocols.pbcast;

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
 * Test case for {@link STABLEMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class STABLEMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new STABLE();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Runtime metrics
            "jgroups.pbcast.stable.stable.sent",
            "jgroups.pbcast.stable.stable.received",
            "jgroups.pbcast.stable.stability.sent",
            "jgroups.pbcast.stable.stability.received",
            "jgroups.pbcast.stable.bytes.received",
            "jgroups.pbcast.stable.votes",
            "jgroups.pbcast.stable.suspended",
            "jgroups.pbcast.stable.stable_task.running",
            // Configuration metrics
            "jgroups.pbcast.stable.gossip.avg",
            "jgroups.pbcast.stable.bytes.max"
        );
    }

    /**
     * STABLE requires a complete stack including NAKACK2.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            new SHARED_LOOPBACK_PING(),
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            protocolUnderTest,           // STABLE
            new GMS(),
            new FRAG2()
        );
    }
}
