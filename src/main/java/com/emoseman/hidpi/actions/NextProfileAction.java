package com.emoseman.hidpi.actions;

import com.emoseman.hidpi.model.HidpiProfile;
import com.emoseman.hidpi.services.HidpiProfilesService;
import com.emoseman.hidpi.services.ProfileApplicationService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class NextProfileAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<HidpiProfile> profiles = HidpiProfilesService.getInstance().listProfiles().stream()
                .sorted(Comparator.comparing(profile -> profile.name.toLowerCase()))
                .toList();
        if (profiles.isEmpty()) {
            return;
        }
        String current = HidpiProfilesService.getInstance().getLastAppliedProfileId();
        int index = 0;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).id.equals(current)) {
                index = (i + 1) % profiles.size();
                break;
            }
        }
        ProfileApplicationService.getInstance().apply(profiles.get(index), e.getProject());
    }
}
