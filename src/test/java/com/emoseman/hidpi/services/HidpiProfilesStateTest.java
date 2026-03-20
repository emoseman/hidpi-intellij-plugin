package com.emoseman.hidpi.services;

import com.emoseman.hidpi.model.HidpiProfile;
import com.emoseman.hidpi.model.HidpiProfilesState;
import org.junit.Assert;
import org.junit.Test;

public class HidpiProfilesStateTest {
    @Test
    public void testAddDuplicateAndDefaultLifecycle() {
        HidpiProfilesService service = new HidpiProfilesService();
        service.loadState(new HidpiProfilesState());

        HidpiProfile first = new HidpiProfile();
        first.name = "4K";
        first.isDefault = true;
        HidpiProfile addedFirst = service.addProfile(first);

        HidpiProfile second = new HidpiProfile();
        second.name = "1080p";
        HidpiProfile addedSecond = service.addProfile(second);

        Assert.assertEquals(2, service.listProfiles().size());
        service.setDefaultProfile(addedSecond.id);
        Assert.assertTrue(service.getDefaultProfile().isPresent());
        Assert.assertEquals("1080p", service.getDefaultProfile().get().name);

        HidpiProfile duplicate = service.duplicateProfile(addedSecond.id, "1080p Clone");
        Assert.assertEquals("1080p Clone", duplicate.name);

        service.renameProfile(addedFirst.id, "4K Laptop");
        Assert.assertTrue(service.listProfiles().stream().anyMatch(profile -> profile.name.equals("4K Laptop")));

        service.deleteProfile(addedSecond.id);
        Assert.assertEquals(2, service.listProfiles().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateNameRejected() {
        HidpiProfilesService service = new HidpiProfilesService();
        service.loadState(new HidpiProfilesState());

        HidpiProfile first = new HidpiProfile();
        first.name = "Desk";
        service.addProfile(first);

        HidpiProfile duplicateName = new HidpiProfile();
        duplicateName.name = "desk";
        service.addProfile(duplicateName);
    }
}
