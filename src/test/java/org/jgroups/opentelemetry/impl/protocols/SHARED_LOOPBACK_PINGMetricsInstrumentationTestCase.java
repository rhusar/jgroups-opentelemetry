package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.SHARED_LOOPBACK_PING;
import org.jgroups.stack.Protocol;

import java.util.List;

/**
 * Test case for {@link SHARED_LOOPBACK_PINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class SHARED_LOOPBACK_PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new SHARED_LOOPBACK_PING();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // SHARED_LOOPBACK_PING extends Discovery, so it should expose Discovery metrics
        return List.of("jgroups.shared_loopback_ping.is_coord", "jgroups.shared_loopback_ping.discovery_requests");
    }
}
