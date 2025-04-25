package dev.arsngrobg.smphook;

import static dev.arsngrobg.smphook.SMPHookError.condition;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import dev.arsngrobg.smphook.core.server.HeapArg;
import dev.arsngrobg.smphook.core.server.JVMOption;

public final class SMPHookConfig {
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

    private final ServerConfiguration serverConfig;

    @JsonCreator
    private SMPHookConfig(@JsonProperty("server") ServerConfiguration serverConfig) {
        this.serverConfig = serverConfig;
    }

    public ServerConfiguration getServerConfig() {
        return serverConfig;
    }
}
