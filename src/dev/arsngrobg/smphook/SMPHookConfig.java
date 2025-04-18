package dev.arsngrobg.smphook;

import java.io.File;

import dev.arsngrobg.smphook.SMPHookError.ErrorType;
import static dev.arsngrobg.smphook.SMPHookError.condition;

public final class SMPHookConfig {
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

        return config;
    }

    public static SMPHookConfig get() throws SMPHookError {
        if (config == null) {
            throw SMPHookError.with(ErrorType.FILE, "Config file has not been loaded.");
        }

        return config;
    }
}
