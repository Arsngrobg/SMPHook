package dev.arsngrobg.smphook;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import dev.arsngrobg.smphook.core.server.HeapArg;
import dev.arsngrobg.smphook.core.server.JVMOption;
import static dev.arsngrobg.smphook.SMPHookError.condition;

public final class SMPHookConfig {
    /**
     * <p>Loads the config using the supplied {@code confPath} argument.</p>
     * 
     * <p>The {@code confPath} must point to a valid JSON file, relative to the SMPHook executable.</p>
     * 
     * @param confPath - the relative path to the config JSON file
     * @return the loaded {@code SMPHookConfig} object
     * @throws SMPHookError if the {@code confPath} does not point to a file, or is not a json file
     */
    public static SMPHookConfig load(String confPath) throws SMPHookError {
        File confFile = SMPHookError.throwIfFail(() -> new File(confPath));
        SMPHookError.caseThrow(
            condition(() -> !confFile.exists(), SMPHookError.with(ErrorType.FILE, "The config file provided does not exist.")),
            condition(() -> !confFile.isFile(), SMPHookError.with(ErrorType.FILE, "The config file provided is not a file.")),
            condition(() -> {
                int fileExtStart = confPath.lastIndexOf('.');
                String fileExt = confPath.substring(fileExtStart);
                return !fileExt.equals(".json");
            }, SMPHookError.with(ErrorType.FILE, "The config file provided is not a .json file."))
        );

        ObjectMapper mapper = new ObjectMapper();
        SMPHookConfig config = SMPHookError.throwIfFail(() -> mapper.readValue(confFile, SMPHookConfig.class));
        return config;
    }

    /** <p>Data Transfer Object (DTO) for the server configuration.</p> */
    @JsonRootName("server")
    public record ServerConfiguration(
        @JsonProperty("jar-path")    String jarPath,
        @JsonProperty("min-heap")    HeapArg minHeap,
        @JsonProperty("max-heap")    HeapArg maxHeap,
        @JsonProperty("JVM-options") JVMOption[] options
    ) {}

    private static final PrettyPrinter PRETTY_PRINTER = new PrettyPrinter() {
        @Override
        public void writeRootValueSeparator(JsonGenerator gen) throws IOException {
            
        }

        @Override
        public void writeStartObject(JsonGenerator gen) throws IOException {

        }

        @Override
        public void writeEndObject(JsonGenerator gen, int nrOfEntries) throws IOException {

        }

        @Override
        public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException {

        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {

        }

        @Override
        public void writeStartArray(JsonGenerator gen) throws IOException {

        }

        @Override
        public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException {

        }

        @Override
        public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {

        }

        @Override
        public void beforeArrayValues(JsonGenerator gen) throws IOException {
            
        }

        @Override
        public void beforeObjectEntries(JsonGenerator gen) throws IOException {

        }
    };

    private final ServerConfiguration serverConfig;

    @JsonCreator
    private SMPHookConfig(@JsonProperty("server") ServerConfiguration serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void export(String exportPath) throws SMPHookError {
        File exportFile = SMPHookError.throwIfFail(() -> new File(exportPath));
        SMPHookError.caseThrow(
            condition(() -> !exportFile.isFile(), SMPHookError.with(ErrorType.FILE, "The config file provided is not a file.")),
            condition(() -> {
                int fileExtStart = exportPath.lastIndexOf('.');
                String fileExt = exportPath.substring(fileExtStart);
                return !fileExt.equals(".json");
            }, SMPHookError.with(ErrorType.FILE, "The config file provided is not a .json file."))
        );

        ObjectMapper mapper = new ObjectMapper();
        SMPHookError.throwIfFail(() -> mapper.writeValue(exportFile, this));
    }

    /** @return the server configuration Data Transfer Object (DTO) */
    @JsonGetter("server")
    public ServerConfiguration getServerConfig() {
        return serverConfig;
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(
            serverConfig.jarPath,
            serverConfig.minHeap,
            serverConfig.maxHeap,
            serverConfig.options
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SMPHookConfig asConf)) return false;
        return serverConfig.equals(asConf.serverConfig);
    }

    // TODO: toString
}
