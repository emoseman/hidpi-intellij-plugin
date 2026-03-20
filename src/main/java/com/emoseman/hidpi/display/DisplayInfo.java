package com.emoseman.hidpi.display;

import com.emoseman.hidpi.model.DisplayRule;

import java.awt.Rectangle;
import java.util.Objects;

public class DisplayInfo {
    private final String graphicsDeviceId;
    private final Rectangle bounds;
    private final String nativeBoundsKey;
    private final double scale;

    public DisplayInfo(String graphicsDeviceId, Rectangle bounds, double scale) {
        this(graphicsDeviceId, bounds, null, scale);
    }

    public DisplayInfo(String graphicsDeviceId, Rectangle bounds, String nativeBoundsKey, double scale) {
        this.graphicsDeviceId = graphicsDeviceId == null ? "" : graphicsDeviceId;
        this.bounds = bounds == null ? new Rectangle() : new Rectangle(bounds);
        this.nativeBoundsKey = nativeBoundsKey == null || nativeBoundsKey.isBlank() ? formatBoundsKey(this.bounds) : nativeBoundsKey;
        this.scale = scale;
    }

    public String graphicsDeviceId() {
        return graphicsDeviceId;
    }

    public Rectangle bounds() {
        return new Rectangle(bounds);
    }

    public double scale() {
        return scale;
    }

    public String boundsKey() {
        return formatBoundsKey(bounds);
    }

    public String nativeBoundsKey() {
        return nativeBoundsKey;
    }

    public String summary() {
        String resolutionSummary = Objects.equals(nativeBoundsKey, boundsKey())
                ? nativeBoundsKey
                : nativeBoundsKey + " (logical " + boundsKey() + ")";
        return graphicsDeviceId + " | " + resolutionSummary + " | scale " + String.format("%.2f", scale);
    }

    public static boolean matches(DisplayRule rule, DisplayInfo info) {
        if (rule == null || !rule.enabled || info == null) {
            return false;
        }
        boolean idMatches = rule.graphicsDeviceId == null || rule.graphicsDeviceId.isBlank()
                || Objects.equals(rule.graphicsDeviceId, info.graphicsDeviceId());
        boolean boundsMatch = rule.bounds == null || rule.bounds.isBlank()
                || Objects.equals(rule.bounds, info.nativeBoundsKey())
                || Objects.equals(rule.bounds, info.boundsKey());
        boolean scaleMatch = Math.abs(rule.scale - info.scale()) < 0.05d;
        if (rule.scale <= 0d) {
            scaleMatch = true;
        }
        return idMatches && boundsMatch && scaleMatch;
    }

    private static String formatBoundsKey(Rectangle bounds) {
        return bounds.x + "," + bounds.y + "," + bounds.width + "x" + bounds.height;
    }
}
