package dev.arsngrobg.smphook.events;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dev.arsngrobg.smphook.SMPHook;
import dev.arsngrobg.smphook.SMPHookError;

public final class CustomEventType implements EventType {
    public static CustomEventType derived(BaseEventType baseType) throws SMPHookError {
        Field baseTypeField = SMPHookError.throwIfFail(() -> BaseEventType.class.getField(baseType.name()));
        EquivalentServerOutput annotation = baseTypeField.getAnnotation(EquivalentServerOutput.class);
        String prototype = annotation.value();
        return CustomEventType.derived(baseType, prototype);
    }

    /**
     * <p>Constructs a new {@code CustomEventType} with the supplied {@code baseType} and {@code prototype}.
     *    This is for redefining the {@code prototype} string for a specific {@link BaseEventType},
     *    due to the fact that certain Minecraft mods can affect the server output.
     * </p>
     * 
     * <p>For example, the {@link BaseEventType#PLAYER_MESSAGE} has the generic representation of:
     *    <blockquote><pre>
     *       [%username%] %message%
     *    </pre></blockquote>
     *    However, a mod can affect this message, where it becomes:
     *    <blockquote><pre>
     *       15:23 | [%username%] %message%
     *    </pre></blockquote>
     * </p>
     * 
     * @param  baseType
     * @param prototype
     * @return
     */
    public static CustomEventType derived(BaseEventType baseType, String prototype) {
        SMPHookError.strictlyRequireNonNull(baseType, "baseType");

        List<TypeWrapper<?>> dummy = new ArrayList<>();
        for (int idx = 0; idx < baseType.argCount(); idx++) {
            dummy.add(baseType.getArgumentType(idx));
        }

        return new CustomEventType(
            baseType.name(),
            SMPHookError.strictlyRequireNonNull(prototype, "prototype"),
            dummy.toArray(TypeWrapper[]::new)
        );
    }

    /**
     * <p>Constructs a new {@code CustomEventType} with the supplied {@code id}, {@code prototype} string, and its {@code argumentTypes}.</p>
     * 
     * <p>The {@code id} parameter must be a valid enum identifier (e.g. {@code __MY_COOL_EVEN34T} is a valid enum identifier).</p>
     * 
     * @param            id - the unique ID of this {@code CustomEventType}
     * @param     prototype - the prototype string of this {@code CustomEventType}
     * @param argumentTypes - the positional types of this {@code CustomEventTypes}
     * @return a new {@code CustomEventType} object
     * @throws SMPHookError if any supplied value are {@code null}, or the {@code id} is an invalid identifier
     */
    public static CustomEventType custom(String id, String prototype, TypeWrapper<?>... argumentTypes) throws SMPHookError {
        SMPHookError.strictlyRequireNonNull(id, "id");

        return SMPHookError.ifFail(() -> {
            BaseEventType baseType = BaseEventType.valueOf(id); // this fails if the id doesn't match any BaseEventType
            return CustomEventType.derived(baseType, prototype);
        }, () -> {
            for (int idx = 0, ch = id.charAt(idx); idx < id.length(); idx++) {
                if (idx == 0 && (!Character.isAlphabetic(ch) && ch != '_')) {
                    throw SMPHookError.withMessage("EventType ID must start with an underscore or a character.");
                }

                if (!Character.isAlphabetic(ch) && Character.isDigit(ch) && ch != '_') {
                    throw SMPHookError.withMessage("EventType ID cannot contain special characters (except from underscores).");
                }
            }

            return new CustomEventType(
                id,
                SMPHookError.strictlyRequireNonNull(prototype, "prototype"),
                SMPHookError.strictlyRequireNonNull(argumentTypes, "argumentTypes")
            );
        });
    }

    private final String id;
    private final String prototype;
    private final TypeWrapper<?>[] argumentTypes;

    private CustomEventType(String id, String prototype, TypeWrapper<?>... argumentTypes) {
        this.id            = id;
        this.prototype     = prototype;
        this.argumentTypes = argumentTypes;
    }

    /** @return the unique ID of this {@code EventType} */
    public String getId() {
        return id;
    }

    /** @return the prototype string of this {@code EventType} */
    public String getPrototype() {
        return prototype;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeWrapper<T> getArgumentType(int idx) throws SMPHookError {
        return SMPHookError.throwIfFail(() -> (TypeWrapper<T>) argumentTypes[idx]);
    }

    @Override
    public int argCount() {
        return argumentTypes.length;
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof CustomEventType asEvent)) return false;
        return id.equals(asEvent.id);
    }

    @Override
    public String toString() {
        return String.format("EventType[%s]", id);
    }
}
