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
 * Test case for {@link UFCMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class UFCMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new UFC();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Runtime metrics
            "jgroups.ufc.credit.requests.received",
            "jgroups.ufc.credit.requests.sent",
            "jgroups.ufc.credit.responses.received",
            "jgroups.ufc.credit.responses.sent",
            "jgroups.ufc.blocked",
            "jgroups.ufc.blocked.avg",
            // Configuration metrics
            "jgroups.ufc.credits.max",
            "jgroups.ufc.credits.min",
            "jgroups.ufc.credits.threshold.min",
            "jgroups.ufc.blocked.max"
        );
    }

    /**
     * UFC requires a transport and GMS.
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
            new GMS(),
            protocolUnderTest,           // UFC
            new FRAG2()
        );
    }
}
