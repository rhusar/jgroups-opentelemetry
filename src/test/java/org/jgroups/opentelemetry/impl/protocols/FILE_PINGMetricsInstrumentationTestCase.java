package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.FILE_PING;
import org.jgroups.stack.Protocol;

import java.util.List;

/**
 * Test case for {@link FILE_PINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class FILE_PINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new FILE_PING();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // FILE_PING extends Discovery and adds file-based storage metrics
        return List.of("jgroups.file_ping.is_coord", "jgroups.file_ping.discovery_requests",
                       "jgroups.file_ping.writes", "jgroups.file_ping.reads");
    }
}
