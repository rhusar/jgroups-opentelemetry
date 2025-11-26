package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.JDBC_PING;
import org.jgroups.stack.Protocol;

import java.util.List;

/**
 * Test case for {@link JDBC_PINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class JDBC_PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        // Use H2 in-memory database for testing
        return new JDBC_PING()
            .setConnectionUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            .setConnectionDriver("org.h2.Driver");
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // JDBC_PING should expose:
        // - JDBC_PING-specific metrics: writes, reads, discovery_requests
        // - Discovery base class metric: is_coord
        return List.of(
            "jgroups.jdbc_ping.writes",
            "jgroups.jdbc_ping.reads",
            "jgroups.jdbc_ping.discovery_requests",
            "jgroups.jdbc_ping.is_coord"
        );
    }
}
