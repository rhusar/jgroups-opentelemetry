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
 * Test case for {@link MERGE3MetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class MERGE3MetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new MERGE3();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Runtime metrics
            "jgroups.merge3.views.cached",
            "jgroups.merge3.merge_events",
            "jgroups.merge3.view_consistency_checker.running",
            "jgroups.merge3.info_sender.running",
            // Configuration metrics
            "jgroups.merge3.interval.min",
            "jgroups.merge3.interval.max",
            "jgroups.merge3.interval.check",
            "jgroups.merge3.max_participants_in_merge"
        );
    }

    /**
     * MERGE3 requires a discovery protocol and GMS to be in the stack.
     */
    @Override
    protected JChannel createChannel(OPENTELEMETRY otelProtocol) throws Exception {
        Protocol protocolUnderTest = createProtocolInstance();

        return new JChannel(
            new SHARED_LOOPBACK(),
            new SHARED_LOOPBACK_PING(),  // Discovery protocol
            protocolUnderTest,           // MERGE3
            new NAKACK2(),
            otelProtocol,
            new UNICAST3(),
            new STABLE(),
            new GMS(),
            new FRAG2()
        );
    }
}
