package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.BPING;
import org.jgroups.stack.Protocol;

import java.util.List;

/**
 * Test case for {@link BPINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
class BPINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new BPING();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // BPING extends Discovery, so it should expose Discovery metrics
        return List.of("jgroups.bping.is_coord", "jgroups.bping.discovery_requests");
    }
}
