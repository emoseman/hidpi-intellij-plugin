package com.emoseman.hidpi.model;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.UUID;

@Tag("profile")
public class HidpiProfile {
    @Attribute("id")
    public String id = UUID.randomUUID().toString();

    @Attribute("name")
    public String name = "New Profile";

    @Attribute("isDefault")
    public boolean isDefault;

    @Attribute("requiresRestart")
    public boolean requiresRestart;

    @Tag("editor")
    public FontSetting editor = new FontSetting();

    @Tag("console")
    public FontSetting console = new FontSetting();

    @Attribute("uiFontFamily")
    public String uiFontFamily = "";

    @Attribute("uiFontSize")
    public int uiFontSize = -1;

    @Attribute("presentationModeFontSize")
    public int presentationModeFontSize = -1;

    @Tag("rule")
    public DisplayRule autoSwitchRule;

    public HidpiProfile copy() {
        HidpiProfile copy = new HidpiProfile();
        copy.id = id;
        copy.name = name;
        copy.isDefault = isDefault;
        copy.requiresRestart = requiresRestart;
        copy.editor = editor.copy();
        copy.console = console.copy();
        copy.uiFontFamily = uiFontFamily;
        copy.uiFontSize = uiFontSize;
        copy.presentationModeFontSize = presentationModeFontSize;
        copy.autoSwitchRule = autoSwitchRule == null ? null : autoSwitchRule.copy();
        return copy;
    }

    public String summary() {
        return String.format(
                "UI: %s %s | Editor: %s %d | Console: %s %d",
                uiFontFamily.isBlank() ? "system" : uiFontFamily,
                uiFontSize <= 0 ? "default" : Integer.toString(uiFontSize),
                editor.family,
                editor.size,
                console.family,
                console.size
        );
    }
}
