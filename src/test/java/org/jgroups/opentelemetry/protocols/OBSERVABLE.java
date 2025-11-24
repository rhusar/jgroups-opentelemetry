package org.jgroups.opentelemetry.protocols;

import org.jgroups.Message;
import org.jgroups.annotations.observability.Observable;
import org.jgroups.annotations.observability.ObservableType;
import org.jgroups.annotations.observability.ObservableUnit;
import org.jgroups.stack.Protocol;

import java.util.concurrent.atomic.LongAdder;

/**
 * Test protocol that counts messages passing up and down the stack.
 * Uses @Observable annotations to expose metrics to OpenTelemetry.
 *
 * @author Radoslav Husar
 */
public class OBSERVABLE extends Protocol {

    @Observable(
        name = "messages.up",
        type = ObservableType.COUNTER,
        unit = ObservableUnit.MESSAGES,
        description = "Total number of messages passed up the stack"
    )
    public final LongAdder num_msgs_up = new LongAdder();

    @Observable(
        name = "messages.down",
        type = ObservableType.COUNTER,
        unit = ObservableUnit.MESSAGES,
        description = "Total number of messages passed down the stack"
    )
    public final LongAdder num_msgs_down = new LongAdder();

    @Override
    public Object up(Message msg) {
        num_msgs_up.increment();
        return up_prot.up(msg);
    }

    @Override
    public Object down(Message msg) {
        num_msgs_down.increment();
        return down_prot.down(msg);
    }
}
