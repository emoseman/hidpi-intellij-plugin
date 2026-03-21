package com.emoseman.hidpi.services;

import com.emoseman.hidpi.model.HidpiProfile;
import org.junit.Assert;
import org.junit.Test;

public class ProfileSummaryTest {
    @Test
    public void testSummaryIncludesCapturedValues() {
        HidpiProfile profile = new HidpiProfile();
        profile.name = "Workstation";
        profile.uiFontFamily = "JetBrains Sans";
        profile.uiFontSize = 14;
        profile.accessibilityOverrideUiFont = true;
        profile.editor.family = "JetBrains Mono";
        profile.editor.size = 15;
        profile.console.family = "JetBrains Mono";
        profile.console.size = 14;

        String summary = profile.summary();
        Assert.assertTrue(summary.contains("JetBrains Sans"));
        Assert.assertTrue(summary.contains("accessibility"));
        Assert.assertTrue(summary.contains("JetBrains Mono"));
        Assert.assertTrue(summary.contains("15"));
        Assert.assertTrue(summary.contains("14"));
    }
}
