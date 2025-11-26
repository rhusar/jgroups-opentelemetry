package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.RACKSPACE_PING;
import org.jgroups.stack.Protocol;
import org.junit.jupiter.api.Disabled;

import java.util.List;

/**
 * Test case for {@link RACKSPACE_PINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
@Disabled("Disabled - RACKSPACE_PING requires actual Rackspace credentials")
class RACKSPACE_PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new RACKSPACE_PING();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // RACKSPACE_PING extends FILE_PING which extends Discovery
        return List.of("jgroups.rackspace_ping.is_coord", "jgroups.rackspace_ping.discovery_requests",
                       "jgroups.rackspace_ping.writes", "jgroups.rackspace_ping.reads");
    }
}
