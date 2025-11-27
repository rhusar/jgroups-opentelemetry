package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.MPING;
import org.jgroups.stack.Protocol;
import org.junit.jupiter.api.Disabled;

import java.util.List;

/**
 * Test case for {@link MPINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
@Disabled("Doesn't work reliably on GHA")
class MPINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new MPING();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // MPING extends PING which extends Discovery, so it should expose Discovery metrics
        return List.of("jgroups.mping.is_coord", "jgroups.mping.discovery_requests");
    }
}
