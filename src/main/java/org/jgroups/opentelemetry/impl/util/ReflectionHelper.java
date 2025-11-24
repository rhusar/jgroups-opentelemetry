package org.jgroups.opentelemetry.impl.util;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.jgroups.stack.Protocol;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * Utility class for reflection-based operations on JGroups protocols.
 * Provides helper methods for accessing protected fields that are not exposed via public APIs.
 *
 * @author Radoslav Husar
 */
public final class ReflectionHelper {

    private static final Log log = LogFactory.getLog(ReflectionHelper.class);

    private ReflectionHelper() {
        // Utility class, prevent instantiation
    }

    /**
     * Uses reflection to access protected LongAdder fields in a protocol.
     * This is necessary because fields like num_msgs_sent are not exposed via public methods.
     *
     * @param protocol The protocol instance
     * @param fieldName The name of the LongAdder field to read
     * @return The current sum of the LongAdder, or 0 if the field cannot be accessed
     */
    public static long getLongAdderValue(Protocol protocol, String fieldName) {
        try {
            Field field = protocol.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            LongAdder adder = (LongAdder) field.get(protocol);
            return adder != null ? adder.sum() : 0;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("failed to read %s field %s: %s", protocol.getClass().getSimpleName(), fieldName, e.getMessage());
            }
            return 0;
        }
    }

    /**
     * Uses reflection to access protected int fields in a protocol.
     * This is necessary because fields are not exposed via public methods.
     *
     * @param protocol The protocol instance
     * @param fieldName The name of the int field to read
     * @return The current value of the int field, or 0 if the field cannot be accessed
     */
    public static int getIntValue(Protocol protocol, String fieldName) {
        try {
            Field field = protocol.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(protocol);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("failed to read %s field %s: %s", protocol.getClass().getSimpleName(), fieldName, e.getMessage());
            }
            return 0;
        }
    }

    /**
     * Uses reflection to access protected boolean fields in a protocol.
     * This is necessary because fields are not exposed via public methods.
     *
     * @param protocol The protocol instance
     * @param fieldName The name of the boolean field to read
     * @return The current value of the boolean field, or false if the field cannot be accessed
     */
    public static boolean getBooleanValue(Protocol protocol, String fieldName) {
        try {
            Field field = protocol.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getBoolean(protocol);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("failed to read %s field %s: %s", protocol.getClass().getSimpleName(), fieldName, e.getMessage());
            }
            return false;
        }
    }

    /**
     * Uses reflection to access protected Set fields and return their size.
     * This is necessary because fields are not exposed via public methods.
     *
     * @param protocol The protocol instance
     * @param fieldName The name of the Set field to read
     * @return The current size of the Set, or 0 if the field cannot be accessed
     */
    public static int getSetSize(Protocol protocol, String fieldName) {
        try {
            Field field = protocol.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(protocol);
            if (value instanceof Set) {
                synchronized (value) {
                    return ((Set<?>) value).size();
                }
            }
            return 0;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("failed to read %s field %s: %s", protocol.getClass().getSimpleName(), fieldName, e.getMessage());
            }
            return 0;
        }
    }
}
