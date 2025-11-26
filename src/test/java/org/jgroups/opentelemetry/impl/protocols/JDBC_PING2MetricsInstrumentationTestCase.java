package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.JDBC_PING2;
import org.jgroups.stack.Protocol;

import java.util.List;

/**
 * Test case for {@link JDBC_PING2MetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class JDBC_PING2MetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        // Use H2 in-memory database for testing
        return new JDBC_PING2()
            .setConnectionUrl("jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1")
            .setConnectionDriver("org.h2.Driver");
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // JDBC_PING2 should expose:
        // - JDBC_PING2-specific metrics: writes, reads, discovery_requests
        // - Discovery base class metric: is_coord
        return List.of(
            "jgroups.jdbc_ping2.writes",
            "jgroups.jdbc_ping2.reads",
            "jgroups.jdbc_ping2.discovery_requests",
            "jgroups.jdbc_ping2.is_coord"
        );
    }
}
