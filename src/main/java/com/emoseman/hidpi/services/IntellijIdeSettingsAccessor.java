package com.emoseman.hidpi.services;

import com.emoseman.hidpi.model.FontSetting;
import com.emoseman.hidpi.model.HidpiProfile;
import com.emoseman.hidpi.util.ReflectionUiSettingsAccessor;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.options.advanced.AdvancedSettings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.APP)
public final class IntellijIdeSettingsAccessor implements IdeSettingsAccessor {
    public static IntellijIdeSettingsAccessor getInstance() {
        return ApplicationManager.getApplication().getService(IntellijIdeSettingsAccessor.class);
    }

    @Override
    public HidpiProfile capture(String name) {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        UISettings uiSettings = UISettings.getInstance();

        HidpiProfile profile = new HidpiProfile();
        profile.name = name;

        FontSetting editor = new FontSetting();
        editor.family = scheme.getEditorFontName();
        editor.size = scheme.getEditorFontSize();
        editor.lineSpacing = scheme.getLineSpacing();
        profile.editor = editor;

        FontSetting console = new FontSetting();
        console.family = scheme.getConsoleFontName();
        console.size = scheme.getConsoleFontSize();
        console.lineSpacing = scheme.getConsoleLineSpacing();
        profile.console = console;

        profile.uiFontFamily = ReflectionUiSettingsAccessor.getUiFontFamily(uiSettings);
        profile.uiFontSize = ReflectionUiSettingsAccessor.getUiFontSize(uiSettings);
        profile.presentationModeFontSize = ReflectionUiSettingsAccessor.getPresentationModeFontSize(uiSettings);

        return profile;
    }

    @Override
    public @NotNull ProfileApplyResult apply(HidpiProfile profile) {
        HidpiProfile backup = capture("backup");
        List<String> warnings = new ArrayList<>();
        try {
            EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
            applyEditorSettings(scheme, profile);
            applyConsoleSettings(scheme, profile);

            UISettings uiSettings = UISettings.getInstance();
            boolean uiChanged = false;
            if (!profile.uiFontFamily.isBlank()) {
                uiChanged = ReflectionUiSettingsAccessor.setUiFontFamily(uiSettings, profile.uiFontFamily) || uiChanged;
            }
            if (profile.uiFontSize > 0) {
                uiChanged = ReflectionUiSettingsAccessor.setUiFontSize(uiSettings, profile.uiFontSize) || uiChanged;
            }
            if (profile.presentationModeFontSize > 0) {
                if (!ReflectionUiSettingsAccessor.setPresentationModeFontSize(uiSettings, profile.presentationModeFontSize)) {
                    warnings.add("Presentation mode font size is not writable on this platform build");
                }
            }
            if (uiChanged) {
                uiSettings.fireUISettingsChanged();
            }

            boolean restartRequired = false;
            if (supportsIdeScaleSetting()) {
                warnings.add("IDE scale changes are intentionally not applied automatically for safety");
                restartRequired = true;
            }
            return new ProfileApplyResult(true, restartRequired, warnings, "Applied profile: " + profile.name);
        } catch (Throwable t) {
            rollback(backup);
            return ProfileApplyResult.failed("Failed to apply profile and rolled back: " + t.getMessage());
        }
    }

    private void applyEditorSettings(EditorColorsScheme scheme, HidpiProfile profile) {
        scheme.setEditorFontName(profile.editor.family);
        scheme.setEditorFontSize(profile.editor.size);
        scheme.setLineSpacing(profile.editor.lineSpacing);
    }

    private void applyConsoleSettings(EditorColorsScheme scheme, HidpiProfile profile) {
        scheme.setConsoleFontName(profile.console.family);
        scheme.setConsoleFontSize(profile.console.size);
        scheme.setConsoleLineSpacing(profile.console.lineSpacing);
    }

    private void rollback(HidpiProfile backup) {
        try {
            EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
            applyEditorSettings(scheme, backup);
            applyConsoleSettings(scheme, backup);
            UISettings uiSettings = UISettings.getInstance();
            if (!backup.uiFontFamily.isBlank()) {
                ReflectionUiSettingsAccessor.setUiFontFamily(uiSettings, backup.uiFontFamily);
            }
            if (backup.uiFontSize > 0) {
                ReflectionUiSettingsAccessor.setUiFontSize(uiSettings, backup.uiFontSize);
            }
            if (backup.presentationModeFontSize > 0) {
                ReflectionUiSettingsAccessor.setPresentationModeFontSize(uiSettings, backup.presentationModeFontSize);
            }
            uiSettings.fireUISettingsChanged();
        } catch (Throwable ignored) {
        }
    }

    private boolean supportsIdeScaleSetting() {
        try {
            return AdvancedSettings.getBoolean("ide.ui.scale.enabled");
        } catch (Throwable ignored) {
            return false;
        }
    }
}
