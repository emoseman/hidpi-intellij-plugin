package com.emoseman.hidpi.actions;

import com.emoseman.hidpi.model.HidpiProfile;
import com.emoseman.hidpi.services.ProfileApplicationService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ApplyProfileAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        HidpiProfile profile = ProfileActionSupport.chooseProfile();
        if (profile == null) {
            return;
        }
        ProfileApplicationService.getInstance().apply(profile, e.getProject());
    }
}
