package com.emoseman.hidpi.model;

import com.intellij.util.xmlb.annotations.Attribute;

public class DisplayRule {
    @Attribute("graphicsDeviceId")
    public String graphicsDeviceId = "";

    @Attribute("bounds")
    public String bounds = "";

    @Attribute("scale")
    public double scale = 1.0d;

    @Attribute("enabled")
    public boolean enabled = true;

    public DisplayRule copy() {
        DisplayRule copy = new DisplayRule();
        copy.graphicsDeviceId = graphicsDeviceId;
        copy.bounds = bounds;
        copy.scale = scale;
        copy.enabled = enabled;
        return copy;
    }
}
