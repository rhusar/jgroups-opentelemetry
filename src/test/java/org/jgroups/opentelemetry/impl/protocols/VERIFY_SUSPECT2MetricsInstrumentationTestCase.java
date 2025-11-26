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
 * Test case for {@link VERIFY_SUSPECT2MetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class VERIFY_SUSPECT2MetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new VERIFY_SUSPECT2();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // VERIFY_SUSPECT2 should expose:
        // - suspects: number of currently suspected members
        // - verification_task_running: whether verification task is running
        return List.of(
            "jgroups.verify_suspect2.suspects",
            "jgroups.verify_suspect2.verification_task_running"
        );
    }

    /**
     * VERIFY_SUSPECT2 needs to be placed after the discovery protocol and before GMS.
     * Override to customize the protocol stack order.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            new SHARED_LOOPBACK_PING(),  // Discovery protocol
            protocolUnderTest,  // VERIFY_SUSPECT2 must be after discovery, before GMS
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        );
    }
}
