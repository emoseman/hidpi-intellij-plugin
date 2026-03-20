package com.emoseman.hidpi.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;

public final class DisplayEventBridgeService implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        AWTEventListener listener = event -> {
            if (event.getID() == ComponentEvent.COMPONENT_MOVED || event.getID() == WindowEvent.WINDOW_GAINED_FOCUS) {
                AutoSwitchService.getInstance().triggerEvaluation(project);
            }
        };

        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
        Disposer.register(project, () -> Toolkit.getDefaultToolkit().removeAWTEventListener(listener));
    }
}
