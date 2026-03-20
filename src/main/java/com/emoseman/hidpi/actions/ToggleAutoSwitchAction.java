package com.emoseman.hidpi.actions;

import com.emoseman.hidpi.services.AutoSwitchService;
import com.emoseman.hidpi.services.HidpiProfilesService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ToggleAutoSwitchAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        HidpiProfilesService service = HidpiProfilesService.getInstance();
        service.setAutoSwitchEnabled(!service.isAutoSwitchEnabled());
        if (service.isAutoSwitchEnabled()) {
            AutoSwitchService.getInstance().triggerEvaluation(e.getProject());
        }
    }
}
