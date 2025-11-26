package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.PING;
import org.jgroups.stack.Protocol;

import java.util.List;

/**
 * Test case for {@link PINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new PING();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // PING extends Discovery, so it should expose Discovery metrics
        return List.of("jgroups.ping.is_coord", "jgroups.ping.discovery_requests");
    }
}
