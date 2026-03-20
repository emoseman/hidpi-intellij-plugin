package com.emoseman.hidpi.actions;

import com.emoseman.hidpi.model.HidpiProfile;
import com.emoseman.hidpi.services.HidpiProfilesService;
import com.intellij.openapi.ui.Messages;

import java.util.Comparator;
import java.util.List;

final class ProfileActionSupport {
    private ProfileActionSupport() {
    }

    static HidpiProfile chooseProfile() {
        List<HidpiProfile> profiles = HidpiProfilesService.getInstance().listProfiles().stream()
                .sorted(Comparator.comparing(profile -> profile.name.toLowerCase()))
                .toList();
        if (profiles.isEmpty()) {
            Messages.showInfoMessage("No HiDPI profiles exist yet.", "Modern HiDPI Profiles");
            return null;
        }
        String[] names = profiles.stream().map(profile -> profile.name).toArray(String[]::new);
        String chosen = Messages.showEditableChooseDialog(
                "Choose a profile",
                "Modern HiDPI Profiles",
                null,
                names,
                names[0],
                null
        );
        if (chosen == null) {
            return null;
        }
        return profiles.stream().filter(profile -> profile.name.equals(chosen)).findFirst().orElse(null);
    }
}
