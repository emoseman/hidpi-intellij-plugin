package com.emoseman.hidpi.services;

import com.emoseman.hidpi.model.HidpiProfile;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.APP)
public final class ProfileApplicationService {
    private static final Logger LOG = Logger.getInstance(ProfileApplicationService.class);

    public static ProfileApplicationService getInstance() {
        return ApplicationManager.getApplication().getService(ProfileApplicationService.class);
    }

    public ProfileApplyResult apply(HidpiProfile profile, @Nullable Project project) {
        ProfileApplyResult result = IntellijIdeSettingsAccessor.getInstance().apply(profile);
        if (result.isSuccess()) {
            HidpiProfilesService service = HidpiProfilesService.getInstance();
            service.setLastAppliedProfileId(profile.id);
            LOG.info("Applied HiDPI profile " + profile.name + ", restartRequired=" + result.isRestartRequired());
            String details = result.getWarnings().isEmpty() ? "" : "\n" + String.join("\n", result.getWarnings());
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("Modern HiDPI Profiles")
                    .createNotification(result.getMessage() + details, NotificationType.INFORMATION)
                    .notify(project);
        } else {
            LOG.warn("Failed to apply profile " + profile.name + ": " + result.getMessage());
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("Modern HiDPI Profiles")
                    .createNotification(result.getMessage(), NotificationType.ERROR)
                    .notify(project);
        }
        return result;
    }
}
