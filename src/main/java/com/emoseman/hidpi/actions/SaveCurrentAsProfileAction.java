package com.emoseman.hidpi.actions;

import com.emoseman.hidpi.model.HidpiProfile;
import com.emoseman.hidpi.services.HidpiProfilesService;
import com.emoseman.hidpi.services.IntellijIdeSettingsAccessor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class SaveCurrentAsProfileAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String name = Messages.showInputDialog(
                e.getProject(),
                "Profile name:",
                "Save Current as HiDPI Profile",
                Messages.getQuestionIcon()
        );
        if (name == null || name.isBlank()) {
            return;
        }
        HidpiProfile profile = IntellijIdeSettingsAccessor.getInstance().capture(name.trim());
        HidpiProfilesService.getInstance().addProfile(profile);
    }
}
