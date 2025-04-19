package dev.arsngrobg.smphook;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import dev.arsngrobg.smphook.core.server.HeapArg;
import dev.arsngrobg.smphook.core.server.JVMOption;
import static dev.arsngrobg.smphook.SMPHookError.condition;

/**
 * <p>The {@code SMPHookConfig} class represents an object representation of the local {@code "hook.json"} file.
 *    It is implemented as a singleton and is instantiated using the {@link SMPHookConfig#load(String)} method.
 *    To retrieve the most recent configuration - use the {@link SMPHookConfig#get()} method.
 * </p>
 * 
 * <p>This class is immutable and thread-safe for reading purposes.</p>
 * 
 * @author Arsngrobg
 * @since  1.0
 * @see    SMPHook
 */
public final class SMPHookConfig {
    // deserializer for JVMOption
    private static final class JVMOptionDeserializer extends StdDeserializer<JVMOption> {
        public JVMOptionDeserializer() {
            super(JVMOption.class);
        }

        @Override
        public JVMOption deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JacksonException {
            JsonNode root = parser.getCodec().readTree(parser);

            String name = root.get("name").asText();
            if (root.has("enabled") && root.has("value")) {
                throw new IOException("JVMOption must have either an 'enabled' or 'value' field - not both.");
            }

            if (root.has("enabled")) {
                boolean enabled = root.get("enabled").asBoolean();
                return enabled ? JVMOption.enabled(name) : JVMOption.disabled(name);
            }

            if (root.has("value")) {
                String value = root.get("value").asText();
                return JVMOption.assigned(name, value);
            }

            throw new IOException("Illegal JVMOption JSON.");
        }
    }

    @JsonIgnore
    private static SMPHookConfig config = null;

    /**
     * <p>Loads the configuration file defined by the supplied {@code configPath} path.</p>
     * 
     * @param configPath the local path to the configuration file
     * @return the newest configuration instance
     * @throws SMPHookError if the configuration could not be correctly parsed or the configuration file is invalid
     */
    public static SMPHookConfig load(String configPath) throws SMPHookError {
        File asFileRef = SMPHookError.throwIfFail(() -> new File(configPath));
        SMPHookError.caseThrow(
            condition(() -> !asFileRef.exists(), SMPHookError.with(ErrorType.FILE, "The config file provided does not exist.")),
            condition(() -> !asFileRef.isFile(), SMPHookError.with(ErrorType.FILE, "The config file provided is not a file.")),
            condition(() -> {
                int fileExtStart = configPath.lastIndexOf('.');
                String fileExt = configPath.substring(fileExtStart);
                return !fileExt.equals(".json");
            }, SMPHookError.with(ErrorType.FILE, "The config file provided is not a .json file."))
        );

        SimpleModule module = new SimpleModule();
        module.addDeserializer(JVMOption.class, new JVMOptionDeserializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        config = SMPHookError.throwIfFail(() -> mapper.readValue(asFileRef, SMPHookConfig.class));

        return config;
    }

    /**
     * <p>Retrieves the most recently-loaded configuration.</p>
     * 
     * <p>If no configuration was loaded - an {@link SMPHookError} is thrown by this method.</p>
     * 
     * @return the most-recently loaded configuration instance
     * @throws SMPHookError if no configuration was loaded
     */
    public static SMPHookConfig get() throws SMPHookError {
        if (config == null) {
            throw SMPHookError.with(ErrorType.FILE, "Config file has not been loaded.");
        }

        return config;
    }

    /** <p>The Data Transfer Object (DTO) for the server configuration JSON.</p> */
    @JsonRootName("server")
    public record ServerConfiguration(
        @JsonProperty("jar-path")    String jarPath,
        @JsonProperty("min-heap")    HeapArg minHeap,
        @JsonProperty("max-heap")    HeapArg maxHeap,
        @JsonProperty("JVM-options") JVMOption... options
    ) {}

    @JsonProperty("server")
    private final ServerConfiguration serverConfiguration;

    @JsonCreator
    private SMPHookConfig(@JsonProperty("server") ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    /** @return the JSON wrapper for the server configuration */
    @JsonGetter("server")
    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }
}
