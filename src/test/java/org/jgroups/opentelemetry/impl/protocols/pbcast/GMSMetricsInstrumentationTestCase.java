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
 * Test case for {@link GMSMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class GMSMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new GMS();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Runtime metrics
            "jgroups.pbcast.gms.views",
            "jgroups.pbcast.gms.is_coord",
            "jgroups.pbcast.gms.is_leaving",
            "jgroups.pbcast.gms.merge.in_progress",
            "jgroups.pbcast.gms.merge.task.running",
            "jgroups.pbcast.gms.merge.killer.running",
            "jgroups.pbcast.gms.view_handler.queue",
            "jgroups.pbcast.gms.view_handler.suspended",
            // Configuration metrics
            "jgroups.pbcast.gms.timeout.join",
            "jgroups.pbcast.gms.timeout.leave",
            "jgroups.pbcast.gms.timeout.merge",
            "jgroups.pbcast.gms.timeout.view_ack_collection",
            "jgroups.pbcast.gms.join_attempts.max"
        );
    }

    /**
     * GMS requires a complete stack including transport.
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
            new STABLE(),
            protocolUnderTest,           // GMS
            new FRAG2()
        );
    }
}
