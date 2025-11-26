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
 * Test case for {@link MFCMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class MFCMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new MFC();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        return List.of(
            // Runtime metrics
            "jgroups.mfc.credit.requests.received",
            "jgroups.mfc.credit.requests.sent",
            "jgroups.mfc.credit.responses.received",
            "jgroups.mfc.credit.responses.sent",
            "jgroups.mfc.blocked",
            "jgroups.mfc.blocked.avg",
            // Configuration metrics
            "jgroups.mfc.credits.max",
            "jgroups.mfc.credits.min",
            "jgroups.mfc.credits.threshold.min",
            "jgroups.mfc.blocked.max"
        );
    }

    /**
     * MFC requires a transport and GMS.
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
            protocolUnderTest,           // MFC
            new FRAG2()
        );
    }
}
