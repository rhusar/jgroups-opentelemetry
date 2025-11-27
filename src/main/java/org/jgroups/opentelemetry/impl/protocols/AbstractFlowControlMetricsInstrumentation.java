package org.jgroups.opentelemetry.impl.protocols;

import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.opentelemetry.impl.util.RegistrationHelper;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.opentelemetry.spi.MetricsInstrumentation;
import org.jgroups.protocols.FlowControl;

/**
 * Abstract base class for flow control protocol metrics instrumentation.
 * Provides common metrics for all {@link FlowControl}-based protocols (UFC, MFC).
 *
 * @author Radoslav Husar
 */
public abstract class AbstractFlowControlMetricsInstrumentation<T extends FlowControl> implements MetricsInstrumentation<T> {

    @Override
    public void registerMetrics(InstrumentationContext context) {
        T protocol = (T) context.protocol();
        RegistrationHelper helper = new RegistrationHelper(context);

        // Runtime metrics (always exposed) - common to all FlowControl protocols
        helper.registerLongCounter("credit.requests.received",
                "Number of credit requests received from senders",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getNumberOfCreditRequestsReceived()));

        helper.registerLongCounter("credit.requests.sent",
                "Number of credit requests sent to receivers",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getNumberOfCreditRequestsSent()));

        helper.registerLongCounter("credit.responses.received",
                "Number of credit responses (replenishments) received from receivers",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getNumberOfCreditResponsesReceived()));

        helper.registerLongCounter("credit.responses.sent",
                "Number of credit responses (replenishments) sent to senders",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getNumberOfCreditResponsesSent()));

        helper.registerLongGauge("blocked",
                "Number of times flow control blocked a sender waiting for credits",
                ObservableUnit.UNITY,
                measurement -> measurement.record(protocol.getNumberOfBlockings()));

        // Average time blocked - subclasses may override if unit conversion is needed
        // since these use different unites per implementation
        registerAverageTimeBlocked(helper, protocol);

        // Configuration metrics
        if (context.exposeConfigurationMetrics()) {
            helper.registerLongGauge("credits.max",
                    "Maximum number of bytes to send per receiver before credits must be replenished",
                    ObservableUnit.BYTES,
                    measurement -> measurement.record(protocol.getMaxCredits()));

            helper.registerLongGauge("credits.min",
                    "Threshold at which a receiver sends more credits to a sender",
                    ObservableUnit.BYTES,
                    measurement -> measurement.record(protocol.getMinCredits()));

            helper.registerDoubleGauge("credits.threshold.min",
                    "Threshold (as percentage of max_credits) at which a receiver sends more credits",
                    ObservableUnit.UNITY,
                    measurement -> measurement.record(protocol.getMinThreshold()));

            helper.registerLongGauge("blocked.max",
                    "Maximum time in milliseconds to block waiting for credits before sending a replenishment request",
                    ObservableUnit.MILLISECONDS,
                    measurement -> measurement.record(protocol.getMaxBlockTime()));
        }
    }

    /**
     * Registers the average time blocked metric. Subclasses can override this method
     * if they need to use different units (e.g., MFC reports in nanoseconds, UFC in milliseconds).
     *
     * @param helper the registration helper
     * @param protocol the flow control protocol instance
     */
    protected void registerAverageTimeBlocked(RegistrationHelper helper, T protocol) {
        helper.registerDoubleGauge("blocked.avg",
                "Average time in milliseconds that senders were blocked waiting for credits",
                ObservableUnit.MILLISECONDS,
                measurement -> measurement.record(protocol.getAverageTimeBlocked()));
    }
}
