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
 * Test case for {@link REDMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class REDMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new RED();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // RED exposes congestion control and queue metrics
        return List.of(
            "jgroups.red.messages.total",
            "jgroups.red.messages.dropped",
            "jgroups.red.messages.drop_rate",
            "jgroups.red.queue.avg_size",
            "jgroups.red.queue.capacity",
            "jgroups.red.enabled",
            "jgroups.red.threshold.min",
            "jgroups.red.threshold.max"
        );
    }

    /**
     * RED is a flow control protocol that needs to be placed in the stack with a discovery protocol.
     * Override to customize the protocol stack order.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            new SHARED_LOOPBACK_PING(),  // Discovery protocol
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            protocolUnderTest,  // RED typically goes after GMS
            new FRAG2()
        );
    }
}
