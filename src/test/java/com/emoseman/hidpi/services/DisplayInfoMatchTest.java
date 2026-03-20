package com.emoseman.hidpi.services;

import com.emoseman.hidpi.display.DisplayInfo;
import com.emoseman.hidpi.model.DisplayRule;
import org.junit.Assert;
import org.junit.Test;

import java.awt.Rectangle;

public class DisplayInfoMatchTest {
    @Test
    public void testRuleMatchesDisplay() {
        DisplayRule rule = new DisplayRule();
        rule.graphicsDeviceId = "id-1";
        rule.bounds = "0,0,3840x2160";
        rule.scale = 2.0d;

        DisplayInfo info = new DisplayInfo("id-1", new Rectangle(0, 0, 1920, 1080), "0,0,3840x2160", 2.0d);
        Assert.assertTrue(DisplayInfo.matches(rule, info));
    }

    @Test
    public void testRuleScaleTolerance() {
        DisplayRule rule = new DisplayRule();
        rule.graphicsDeviceId = "id-2";
        rule.bounds = "0,0,1920x1080";
        rule.scale = 1.25d;

        DisplayInfo info = new DisplayInfo("id-2", new Rectangle(0, 0, 1920, 1080), 1.23d);
        Assert.assertTrue(DisplayInfo.matches(rule, info));
    }

    @Test
    public void testRuleMismatch() {
        DisplayRule rule = new DisplayRule();
        rule.graphicsDeviceId = "id-3";
        rule.bounds = "0,0,1920x1080";
        rule.scale = 1.0d;

        DisplayInfo info = new DisplayInfo("id-4", new Rectangle(0, 0, 2560, 1440), 1.0d);
        Assert.assertFalse(DisplayInfo.matches(rule, info));
    }

    @Test
    public void testLegacyLogicalBoundsStillMatch() {
        DisplayRule rule = new DisplayRule();
        rule.graphicsDeviceId = "id-1";
        rule.bounds = "0,0,1920x1080";
        rule.scale = 2.0d;

        DisplayInfo info = new DisplayInfo("id-1", new Rectangle(0, 0, 1920, 1080), "0,0,3840x2160", 2.0d);
        Assert.assertTrue(DisplayInfo.matches(rule, info));
    }
}
