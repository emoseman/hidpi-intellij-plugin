package com.emoseman.hidpi.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class HidpiProfilesConfigurable implements Configurable {
    private HidpiProfilesSettingsPanel panel;

    @Override
    public @Nls String getDisplayName() {
        return "HiDPI Profiles";
    }

    @Override
    public @Nullable JComponent createComponent() {
        panel = new HidpiProfilesSettingsPanel();
        return panel.getComponent();
    }

    @Override
    public boolean isModified() {
        return panel != null && panel.isModified();
    }

    @Override
    public void apply() {
        if (panel != null) {
            panel.applyToService();
            panel.resetFromService();
        }
    }

    @Override
    public void reset() {
        if (panel != null) {
            panel.resetFromService();
        }
    }

    @Override
    public void disposeUIResources() {
        panel = null;
    }
}
