package com.emoseman.hidpi.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileApplyResult {
    private final boolean success;
    private final boolean restartRequired;
    private final List<String> warnings;
    private final String message;

    public ProfileApplyResult(boolean success, boolean restartRequired, List<String> warnings, String message) {
        this.success = success;
        this.restartRequired = restartRequired;
        this.warnings = new ArrayList<>(warnings);
        this.message = message;
    }

    public static ProfileApplyResult success(String message) {
        return new ProfileApplyResult(true, false, List.of(), message);
    }

    public static ProfileApplyResult failed(String message) {
        return new ProfileApplyResult(false, false, List.of(), message);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isRestartRequired() {
        return restartRequired;
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public String getMessage() {
        return message;
    }
}
