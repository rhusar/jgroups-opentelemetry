package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.opentelemetry.impl.AbstractMetricsInstrumentationTestCase;
import org.jgroups.protocols.TCPPING;
import org.jgroups.stack.Protocol;
import org.junit.jupiter.api.Disabled;

import java.util.List;

/**
 * Test case for {@link TCPPINGMetricsInstrumentation}.
 *
 * @author Radoslav Husar
 */
@Disabled("Disabled - TCPPING requires pre-configured initial hosts")
class TCPPINGMetricsInstrumentationTestCase extends AbstractMetricsInstrumentationTestCase {

    @Override
    protected Protocol createProtocolInstance() {
        return new TCPPING();
    }

    @Override
    protected List<String> getExpectedMetrics() {
        // TCPPING extends Discovery, so it should expose Discovery metrics
        return List.of("jgroups.tcpping.is_coord", "jgroups.tcpping.discovery_requests");
    }
}
