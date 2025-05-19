package dev.arsngrobg.smphook;

import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import dev.arsngrobg.smphook.core.server.HeapArg;
import dev.arsngrobg.smphook.core.server.JVMOption;
import static dev.arsngrobg.smphook.SMPHookError.condition;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter;
import com.fasterxml.jackson.core.util.Separators.Spacing;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * <p>The {@code SMPHookConfig} class acts as the global configuration file interface for SMPHook.</p>
 * 
 * <p>To maintain consistency across the whole of the SMPHook system, there must only be one-or-none instances of this class.</p>
 * 
 * <p>To load a configuration file, use the {@link #load(String)} method.</p>
 * <p>To get the previously loaded file, use the {@link #get()} method.</p>
 * <p>
 * 
 * @author Arsngrobg
 * @since  1.0
 */
public final class SMPHookConfig {
    /** <p>The default config path.</p> */
    public static final String DEFAULT_CONFIG_PATH = "hook.json";

    /**
     * <p>The default server settings.</p>
     * 
     * <p><i>The JVM configuration is akin to the one found in the Minecraft launcher for Java Edition.</i></p>
     */
    public static final ServerSettings DEFAULT_SERVER_SETTINGS = new ServerSettings(
        // jar-path
        "smp\\server.jar",

        // min-heap
        HeapArg.ofSize(2, HeapArg.Unit.GIGABYTE),

        // max-heap
        null,

        // JVM-options
        JVMOption.enabled ("UnlockExperimentalVMOptions"),
        JVMOption.enabled ("UseG1GC"),
        JVMOption.assigned("G1NewSizePercent", 20),
        JVMOption.assigned("G1ReservePercent", 20),
        JVMOption.assigned("MaxGCPauseMillis", 50),
        JVMOption.assigned("G1HeapRegionSize", "32M")
    );
    
    /**
     * <p>The server configuration.</p>
     * 
     * <p><i>It consists of the settings required for the initialization of a Minecraft server.</i></p>
     */
    @JsonRootName("server")
    @JsonPropertyOrder({ "jar-path", "min-heap", "max-heap", "JVM-options" })
    public record ServerSettings(
        @JsonProperty("jar-path")    String       jarPath,
        @JsonProperty("min-heap")    HeapArg      minHeap,
        @JsonProperty("max-heap")    HeapArg      maxHeap,
        @JsonProperty("JVM-options") JVMOption... jvmOptions
    ) { @Override public String toString() { return SMPHookError.throwIfFail(() -> new ObjectMapper().writeValueAsString(this)); } }

    // input & output mappers
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectWriter writer;
    static {
        Separators separators = Separators.createDefaultInstance()
                                          .withArrayValueSeparator(',')
                                          .withObjectEntrySeparator(',')
                                          .withArrayValueSpacing(Spacing.AFTER)
                                          .withObjectFieldValueSpacing(Spacing.AFTER)
                                          .withObjectEntrySpacing(Spacing.AFTER);

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter().withSeparators(separators)
                                                            .withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE.withIndent("    "))
                                                            .withObjectIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE.withIndent("    "));

        writer = mapper.writer(pp);
    }

    // singleton
    private static SMPHookConfig instance = null;

    // instance
    @JsonProperty("server")
    private final ServerSettings serverSettings;

    public static synchronized SMPHookConfig loadDefaults() throws SMPHookError {
        SMPHookConfig defaults = new SMPHookConfig(DEFAULT_SERVER_SETTINGS);
        
        SMPHookError.throwIfFail(() -> writer.writeValue(new File(DEFAULT_CONFIG_PATH), defaults));

        return defaults;
    }

    /**
     * <p>Loads the configuration file located at the supplied {@code configPath}.</p>
     * 
     * @param configPath - the configuration file path (relative to SMPHook)
     * @return The newly loaded confguration file object
     * @throws SMPHookError if the {@code configPath} is invalid
     */
    public static synchronized SMPHookConfig load(String configPath) throws SMPHookError {
        File confFile = SMPHookError.throwIfFail(() -> new File(configPath));
        SMPHookError.caseThrow(
            condition(() -> !confFile.exists(),  SMPHookError.with(ErrorType.FILE, "Config file does not exist")),
            condition(() -> !confFile.isFile(),  SMPHookError.with(ErrorType.FILE, "Config file is not a file")),
            condition(() -> !confFile.canRead(), SMPHookError.with(ErrorType.FILE, "Config file is not readable"))
        );

        instance = SMPHookError.throwIfFail(() -> mapper.readValue(confFile, SMPHookConfig.class));

        return instance;
    }

    /**
     * <p>Retrieves the currently loaded configuration.</p>
     * 
     * <p><i>This return value changes for every invokation of the {@link #load(String)} method.</i></p>
     * 
     * @return the currently active {@code SMPHookConfig} instance
     * @throws SMPHookError if there is no {@code SMPHookConfig} loaded
     */
    public static synchronized SMPHookConfig get() throws SMPHookError {
        if (instance == null) {
            throw SMPHookError.with(ErrorType.FILE, "No config file loaded.");
        }

        return instance;
    }

    /**
     * <p>Drops the current handle to the configuration file, and returns the configuration pre-drop.</p>
     * 
     * @return the configuration handle that was in memory pre-drop
     * @throws SMPHookError if there is no active configuration loaded
     */
    public static synchronized SMPHookConfig drop() throws SMPHookError {
        if (instance == null) {
            throw SMPHookError.with(ErrorType.FILE, "No config file loaded");
        }

        SMPHookConfig temp = instance;
        instance = null;
        return temp;
    }

    @JsonCreator
    private SMPHookConfig(@JsonProperty("server") ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    /** @return an immutable copy of the server settings, exact to the file's structure at runtime */
    @JsonGetter("server")
    public ServerSettings serverSettings() {
        return serverSettings;
    }

    @Override
    public int hashCode() {
        return SMPHook.hashOf(serverSettings);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SMPHookConfig asConfig)) return false;
        return serverSettings.equals(asConfig.serverSettings);
    }

    @Override
    public String toString() throws SMPHookError {
        return SMPHookError.throwIfFail(() -> writer.writeValueAsString(this));
    }
}
