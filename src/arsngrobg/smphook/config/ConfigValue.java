package arsngrobg.smphook.config;

import java.util.Arrays;
import java.util.Objects;

import arsngrobg.smphook.annotations.NonNull;

public final class ConfigValue<T> {
    public static final Class<?>[] SUPPORTED_TYPES = {
        String.class, Long.class, Integer.class, Float.class, Double.class, Boolean.class
    }; // and null values

    private final String name;
    private final T      value;

    public ConfigValue(@NonNull String name, T value) {
        if (value != null) {
            boolean supported = Arrays.stream(SUPPORTED_TYPES).anyMatch(t -> value.getClass() == t);
            if (!supported) {
                throw new Error("SMPHookError: value type is not supported.");
            }
        }

        if (name == null || name.replaceAll("\\s+", "").isEmpty()) {
            throw new Error("SMPHookError: config value name cannot be an empty string.");
        }

        this.name  = name;
        this.value = value;
    }

    public boolean isType(Class<?> type) {
        if (value == null) return true;
        return value.getClass() == type;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        if (value == null) return String.format("%s = ", name);
        else               return String.format("%s = %s", name, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        ConfigValue<?> asConfigValue = (ConfigValue<?>) obj;
        return name.equals(asConfigValue.name) && value.equals(obj);
    }
}
