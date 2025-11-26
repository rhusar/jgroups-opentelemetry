package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.LOCAL_PING;
import org.jgroups.stack.Protocol;

import java.util.List;

/**
 * Test case for {@link LOCAL_PINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class LOCAL_PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new LOCAL_PING();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // LOCAL_PING extends Discovery, so it should expose Discovery metrics
        return List.of("jgroups.local_ping.is_coord", "jgroups.local_ping.discovery_requests");
    }
}
