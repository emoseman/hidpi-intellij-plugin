package com.emoseman.hidpi.display;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.wm.WindowManager;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;

@Service(Service.Level.APP)
public final class DisplayDetectionService {
    public static DisplayDetectionService getInstance() {
        return ApplicationManager.getApplication().getService(DisplayDetectionService.class);
    }

    public DisplayInfo detectCurrentDisplay() {
        Window focused = WindowManager.getInstance().getMostRecentFocusedWindow();
        if (focused == null) {
            Frame frame = WindowManager.getInstance().findVisibleFrame();
            focused = frame;
        }
        if (focused != null) {
            GraphicsConfiguration gc = focused.getGraphicsConfiguration();
            if (gc != null) {
                return fromGraphicsConfiguration(gc);
            }
        }
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        if (devices.length == 0) {
            return new DisplayInfo("unknown", new Rectangle(), "0,0,0x0", 1.0d);
        }
        return fromGraphicsConfiguration(devices[0].getDefaultConfiguration());
    }

    public DisplayInfo fromGraphicsConfiguration(GraphicsConfiguration gc) {
        String id = gc.getDevice() != null ? gc.getDevice().getIDstring() : "unknown";
        Rectangle bounds = gc.getBounds();
        double scaleX = gc.getDefaultTransform() == null ? 1.0d : gc.getDefaultTransform().getScaleX();
        double scale = scaleX <= 0 ? 1.0d : scaleX;
        return new DisplayInfo(id, bounds, nativeBoundsKey(gc, bounds, scale), scale);
    }

    private String nativeBoundsKey(GraphicsConfiguration gc, Rectangle bounds, double scale) {
        GraphicsDevice device = gc.getDevice();
        if (device != null && device.getDisplayMode() != null
                && device.getDisplayMode().getWidth() > 0
                && device.getDisplayMode().getHeight() > 0) {
            return bounds.x + "," + bounds.y + "," + device.getDisplayMode().getWidth() + "x" + device.getDisplayMode().getHeight();
        }

        int scaledWidth = Math.max(0, (int) Math.round(bounds.width * scale));
        int scaledHeight = Math.max(0, (int) Math.round(bounds.height * scale));
        return bounds.x + "," + bounds.y + "," + scaledWidth + "x" + scaledHeight;
    }
}
