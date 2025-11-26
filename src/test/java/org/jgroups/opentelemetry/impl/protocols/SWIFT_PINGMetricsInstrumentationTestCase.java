package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.SWIFT_PING;
import org.jgroups.stack.Protocol;
import org.junit.jupiter.api.Disabled;

import java.util.List;

/**
 * Test case for {@link SWIFT_PINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
@Disabled("Disabled - SWIFT_PING requires actual OpenStack Swift credentials")
class SWIFT_PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new SWIFT_PING();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // SWIFT_PING extends FILE_PING which extends Discovery
        return List.of("jgroups.swift_ping.is_coord", "jgroups.swift_ping.discovery_requests",
                       "jgroups.swift_ping.writes", "jgroups.swift_ping.reads");
    }
}
