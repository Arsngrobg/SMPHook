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

public final class SMPHookConfig {
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

        // TODO: actual parse code here
        SimpleModule module = new SimpleModule();
        module.addDeserializer(JVMOption.class, new JVMOptionDeserializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        config = SMPHookError.throwIfFail(() -> mapper.readValue(asFileRef, SMPHookConfig.class));

        return config;
    }

    public static SMPHookConfig get() throws SMPHookError {
        if (config == null) {
            throw SMPHookError.with(ErrorType.FILE, "Config file has not been loaded.");
        }

        return config;
    }

    @JsonRootName("server")
    public record ServerConfiguration(
        @JsonProperty("jarPath")    String jarPath,
        @JsonProperty("minHeap")    HeapArg minHeap,
        @JsonProperty("maxHeap")    HeapArg maxHeap,
        @JsonProperty("jvmOptions") JVMOption... options
    ) {}

    @JsonProperty("server")
    private final ServerConfiguration serverConfiguration;

    @JsonCreator
    private SMPHookConfig(@JsonProperty("server") ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    @JsonGetter
    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }
}
