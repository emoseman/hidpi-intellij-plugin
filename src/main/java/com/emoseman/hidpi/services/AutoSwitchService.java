package com.emoseman.hidpi.services;

import com.emoseman.hidpi.display.DisplayDetectionService;
import com.emoseman.hidpi.display.DisplayInfo;
import com.emoseman.hidpi.model.DisplayRule;
import com.emoseman.hidpi.model.HidpiProfile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Service(Service.Level.APP)
public final class AutoSwitchService {
    private static final Logger LOG = Logger.getInstance(AutoSwitchService.class);
    private final Alarm debounceAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, ApplicationManager.getApplication());

    public static AutoSwitchService getInstance() {
        return ApplicationManager.getApplication().getService(AutoSwitchService.class);
    }

    public void triggerEvaluation(@Nullable Project project) {
        debounceAlarm.cancelAllRequests();
        debounceAlarm.addRequest(() -> evaluateNow(project), 600);
    }

    public void evaluateNow(@Nullable Project project) {
        HidpiProfilesService profilesService = HidpiProfilesService.getInstance();
        if (!profilesService.isAutoSwitchEnabled()) {
            return;
        }
        DisplayInfo currentDisplay = DisplayDetectionService.getInstance().detectCurrentDisplay();
        List<HidpiProfile> profiles = profilesService.listProfiles();
        Optional<HidpiProfile> match = profiles.stream()
                .filter(profile -> matches(profile.autoSwitchRule, currentDisplay))
                .findFirst();
        if (match.isEmpty()) {
            return;
        }
        HidpiProfile profile = match.get();
        if (profile.id.equals(profilesService.getLastAppliedProfileId())) {
            return;
        }
        LOG.info("Auto-switch matched profile " + profile.name + " for display " + currentDisplay.summary());
        ProfileApplicationService.getInstance().apply(profile, project);
    }

    private boolean matches(DisplayRule rule, DisplayInfo info) {
        return DisplayInfo.matches(rule, info);
    }
}
