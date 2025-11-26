package org.jgroups.opentelemetry.impl;

import io.opentelemetry.api.metrics.Meter;
import org.jgroups.opentelemetry.spi.InstrumentationContext;
import org.jgroups.stack.Protocol;

/**
 * Basic implementation of {@link InstrumentationContext}.
 * This immutable implementation holds all dependencies needed to register metrics for a protocol.
 *
 * @author Radoslav Husar
 */
public record BasicInstrumentationContext(Protocol protocol, Meter meter, boolean exposeConfigurationMetrics) implements InstrumentationContext {
}
