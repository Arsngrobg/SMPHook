package arsngrobg.smphook.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arsngrobg.smphook.annotations.NonNull;

public final class Config {
    public static Config fromFile(@NonNull String filePath) {
        if (filePath == null) {
            throw new Error("SMPHookError: config file path cannot be null.");
        }

        File confFile = new File(filePath);
        if (!confFile.exists()) {
            throw new Error("SMPHookError: config file does not exist.");
        }
        if (!confFile.canRead()) {
            throw new Error("SMPHookError: config file is unaccessable.");
        }

        try (FileInputStream fistream = new FileInputStream(confFile)) {
            byte[] bytes = fistream.readAllBytes();
            String fileSource = new String(bytes);
            return Config.fromSource(fileSource);
        } catch (IOException e) {
            throw (Error) new Error("SMPHookError: config file could not be read.").initCause(e);
        }
    }

    public static Config fromSource(@NonNull String configsrc) {
        if (configsrc == null) {
            throw new Error("SMPHookError: config source string cannot be null.");
        }

        Map<String, ConfigValue<?>[]> values = new HashMap<>();

        String[] lines = configsrc.split("\n");

        String               section       = null;
        List<ConfigValue<?>> sectionValues = new ArrayList<>();

        for (String line : lines) {
            line = line.replaceAll("\\s+", "");
            if (line.isEmpty()) continue;

            if (line.startsWith("[") && line.endsWith("]")) {
                values.put(section, sectionValues.toArray(ConfigValue<?>[]::new));

                section = line.substring(1, line.length() - 1);
                if (section.replaceAll("\\s+", "").isEmpty()) {
                    throw new Error("SMPHookError: config section is empty.");
                }

                sectionValues = new ArrayList<>();
                continue;
            }

            String[] keyValue = line.split("=");
            if (keyValue.length == 0) {
                throw new Error("SMPHookError: config key with no assignment.");
            }

            String key = keyValue[0];
            Object val = null;
            if (keyValue.length == 2) {
                val = keyValue[1];
            }

            ConfigValue<?> confValue = new ConfigValue<>(key, val);
            sectionValues.add(confValue);
        }

        values.put(section, sectionValues.toArray(ConfigValue<?>[]::new));

        return new Config(values);
    }

    private final Map<String, ConfigValue<?>[]> values = new HashMap<>();

    private Config(Map<String, ConfigValue<?>[]> values) {
        if (values == null) {
            throw new Error("SMPHookError: config value map cannot be null.");
        }

        this.values.putAll(values);
    }

    public boolean export(@NonNull String destFile) {
        File file = new File(destFile);
        try (FileOutputStream fostream = new FileOutputStream(file)) {
            byte[] bytes = toString().getBytes(Charset.defaultCharset());
            fostream.write(bytes);
            return true;
        } catch (IOException ignored) { return false; }
    }

    public String getString(@NonNull String name) {
        ConfigValue<?> confValue = get(name);
        if (!confValue.isType(String.class)) {
            throw new Error("SMPHookError: config value is not a string.");
        }

        return (String) confValue.getValue();
    }

    public long getLong(@NonNull String name) {
        ConfigValue<?> confValue = get(name);
        if (!confValue.isType(Long.class)) {
            throw new Error("SMPHookError: config value is not a long.");
        }

        return (long) confValue.getValue();
    }

    public int getInteger(@NonNull String name) {
        ConfigValue<?> confValue = get(name);
        if (!confValue.isType(Integer.class)) {
            throw new Error("SMPHookError: config value is not an integer.");
        }

        return (int) confValue.getValue();
    }

    public float getFloat(@NonNull String name) {
        ConfigValue<?> confValue = get(name);
        if (!confValue.isType(Float.class)) {
            throw new Error("SMPHookError: config value is not a float.");
        }

        return (float) confValue.getValue();
    }

    public double getDouble(@NonNull String name) {
        ConfigValue<?> confValue = get(name);
        if (!confValue.isType(Double.class)) {
            throw new Error("SMPHookError: config value is not a double.");
        }

        return (double) confValue.getValue();
    }

    public boolean getBoolean(@NonNull String name) {
        ConfigValue<?> confValue = get(name);
        if (!confValue.isType(Boolean.class)) {
            throw new Error("SMPHookError: config value is not a boolean.");
        }

        return (boolean) confValue.getValue();
    }

    public ConfigValue<?> get(@NonNull String name) {
        if (name == null) {
            throw new Error("SMPHookError: name cannot be null.");
        }

        String[] splitName = name.split("\\.", 2);
        if (splitName.length == 1) {
            return Arrays.stream(getAllDefaultValues()).filter(v -> v.getName().equals(name)).findFirst().get();
        } else return Arrays.stream(getAllSectionValues(splitName[0])).filter(v -> v.getName().equals(splitName[1])).findFirst().get();
    }

    public ConfigValue<?>[] getAllSectionValues(String section) {
        if (section == null) return getAllDefaultValues();

        return values.computeIfAbsent(section, key -> {
            throw new Error(String.format("SMPHookError: invalid key '%s'", key));
        });
    }

    public ConfigValue<?>[] getAllDefaultValues() {
        return values.computeIfAbsent(null, key -> {
            throw new Error("SMPHookError: no default values available.");
        });
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        values.forEach((sec, vals) -> {
            if (sec != null) stringBuilder.append("[").append(sec).append("]").append("\n");

            for (ConfigValue<?> val : vals) {
                stringBuilder.append(val).append("\n");                
            }

            if (sec == null && vals.length != 0) {
                stringBuilder.append("\n");
            }
        });

        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        if (obj == this) return true;
        Config asConfig = (Config) obj;
        return values.equals(asConfig.values);
    }
}
