package com.emoseman.hidpi.model;

import com.intellij.util.xmlb.annotations.Attribute;

public class FontSetting {
    @Attribute("family")
    public String family = "";

    @Attribute("size")
    public int size = 13;

    @Attribute("lineSpacing")
    public float lineSpacing = 1.2f;

    public FontSetting copy() {
        FontSetting copy = new FontSetting();
        copy.family = family;
        copy.size = size;
        copy.lineSpacing = lineSpacing;
        return copy;
    }
}
