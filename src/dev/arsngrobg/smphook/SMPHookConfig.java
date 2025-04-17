package dev.arsngrobg.smphook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import dev.arsngrobg.smphook.core.server.JVMOption;

public final class SMPHookConfig {
    @JsonRootName("server")
    public record ServerConfiguration(
        @JsonProperty("jar")        String jar,
        @JsonProperty("minHeap")    String minHeap,
        @JsonProperty("maxHeap")    String maxHeap,
        @JsonProperty("jvmOptions") JVMOption... options
    ) {}

    private final @JsonProperty("server") ServerConfiguration serverConfiguration;

    public SMPHookConfig(@JsonProperty("server") ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }
}
