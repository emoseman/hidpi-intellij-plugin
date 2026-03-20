package com.emoseman.hidpi.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public final class StartupInitializationService implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        HidpiProfilesService profilesService = HidpiProfilesService.getInstance();
        if (!profilesService.isAutoSwitchEnabled()) {
            return;
        }
        AutoSwitchService.getInstance().triggerEvaluation(project);
    }
}
