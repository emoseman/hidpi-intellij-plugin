package com.emoseman.hidpi.services;

import com.emoseman.hidpi.model.DisplayRule;
import com.emoseman.hidpi.model.HidpiProfile;
import com.emoseman.hidpi.model.HidpiProfilesState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service(Service.Level.APP)
@State(name = "ModernHidpiProfiles", storages = @Storage("modern-hidpi-profiles.xml"))
public final class HidpiProfilesService implements PersistentStateComponent<HidpiProfilesState> {
    private static final Logger LOG = Logger.getInstance(HidpiProfilesService.class);

    private HidpiProfilesState state = new HidpiProfilesState();

    public static HidpiProfilesService getInstance() {
        return ApplicationManager.getApplication().getService(HidpiProfilesService.class);
    }

    @Override
    public @NotNull HidpiProfilesState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull HidpiProfilesState loaded) {
        state = loaded;
        if (state.version <= 0) {
            state.version = 1;
        }
        if (state.profiles == null) {
            state.profiles = new ArrayList<>();
        }
    }

    public synchronized List<HidpiProfile> listProfiles() {
        return state.profiles.stream()
                .sorted(Comparator.comparing(profile -> profile.name.toLowerCase()))
                .map(HidpiProfile::copy)
                .toList();
    }

    public synchronized Optional<HidpiProfile> getProfile(String id) {
        return state.profiles.stream().filter(profile -> profile.id.equals(id)).findFirst().map(HidpiProfile::copy);
    }

    public synchronized Optional<HidpiProfile> getDefaultProfile() {
        return state.profiles.stream().filter(profile -> profile.isDefault).findFirst().map(HidpiProfile::copy);
    }

    public synchronized boolean isAutoSwitchEnabled() {
        return state.autoSwitchEnabled;
    }

    public synchronized void setAutoSwitchEnabled(boolean enabled) {
        state.autoSwitchEnabled = enabled;
    }

    public synchronized String getLastAppliedProfileId() {
        return state.lastAppliedProfileId == null ? "" : state.lastAppliedProfileId;
    }

    public synchronized void setLastAppliedProfileId(String id) {
        state.lastAppliedProfileId = id == null ? "" : id;
    }

    public synchronized HidpiProfile addProfile(HidpiProfile profile) {
        validateName(profile.name, null);
        HidpiProfile copy = profile.copy();
        copy.id = UUID.randomUUID().toString();
        if (copy.isDefault) {
            clearDefault();
        }
        state.profiles.add(copy);
        LOG.info("Added HiDPI profile: " + copy.name);
        return copy.copy();
    }

    public synchronized HidpiProfile updateProfile(HidpiProfile profile) {
        validateName(profile.name, profile.id);
        for (int i = 0; i < state.profiles.size(); i++) {
            HidpiProfile existing = state.profiles.get(i);
            if (existing.id.equals(profile.id)) {
                HidpiProfile copy = profile.copy();
                copy.id = existing.id;
                if (copy.isDefault) {
                    clearDefault();
                }
                state.profiles.set(i, copy);
                LOG.info("Updated HiDPI profile: " + copy.name);
                return copy.copy();
            }
        }
        throw new IllegalArgumentException("Profile not found: " + profile.id);
    }

    public synchronized HidpiProfile duplicateProfile(String id, String newName) {
        HidpiProfile original = getExisting(id);
        validateName(newName, null);
        HidpiProfile clone = original.copy();
        clone.id = UUID.randomUUID().toString();
        clone.name = newName;
        clone.isDefault = false;
        state.profiles.add(clone);
        LOG.info("Duplicated HiDPI profile: " + original.name + " -> " + newName);
        return clone.copy();
    }

    public synchronized void renameProfile(String id, String newName) {
        validateName(newName, id);
        HidpiProfile profile = getExisting(id);
        profile.name = newName;
        LOG.info("Renamed HiDPI profile to: " + newName);
    }

    public synchronized void deleteProfile(String id) {
        HidpiProfile profile = getExisting(id);
        state.profiles.removeIf(current -> current.id.equals(id));
        if (id.equals(state.lastAppliedProfileId)) {
            state.lastAppliedProfileId = "";
        }
        LOG.info("Deleted HiDPI profile: " + profile.name);
    }

    public synchronized void setDefaultProfile(String id) {
        clearDefault();
        HidpiProfile profile = getExisting(id);
        profile.isDefault = true;
        LOG.info("Set default HiDPI profile: " + profile.name);
    }

    public synchronized void clearRule(String id) {
        HidpiProfile profile = getExisting(id);
        profile.autoSwitchRule = null;
    }

    public synchronized void setRule(String id, DisplayRule rule) {
        HidpiProfile profile = getExisting(id);
        profile.autoSwitchRule = rule;
    }

    public synchronized void replaceProfiles(List<HidpiProfile> profiles) {
        List<HidpiProfile> replacements = new ArrayList<>();
        for (HidpiProfile profile : profiles) {
            validateNameForProfiles(profile.name, profile.id, replacements);
            replacements.add(profile.copy());
        }

        boolean foundDefault = false;
        for (HidpiProfile profile : replacements) {
            if (profile.isDefault) {
                if (!foundDefault) {
                    foundDefault = true;
                } else {
                    profile.isDefault = false;
                }
            }
        }

        state.profiles = replacements;
        if (state.lastAppliedProfileId != null && !state.lastAppliedProfileId.isBlank()) {
            boolean exists = state.profiles.stream().anyMatch(profile -> profile.id.equals(state.lastAppliedProfileId));
            if (!exists) {
                state.lastAppliedProfileId = "";
            }
        }
        LOG.info("Replaced HiDPI profiles with " + state.profiles.size() + " entries");
    }

    private HidpiProfile getExisting(String id) {
        return state.profiles.stream().filter(profile -> profile.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + id));
    }

    private void validateName(String name, String currentId) {
        validateNameForProfiles(name, currentId, state.profiles);
    }

    private void validateNameForProfiles(String name, String currentId, List<HidpiProfile> profiles) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }
        boolean duplicate = profiles.stream()
                .anyMatch(profile -> profile.name.equalsIgnoreCase(name) && (currentId == null || !profile.id.equals(currentId)));
        if (duplicate) {
            throw new IllegalArgumentException("Profile name already exists: " + name);
        }
    }

    private void clearDefault() {
        state.profiles.forEach(profile -> profile.isDefault = false);
    }
}
