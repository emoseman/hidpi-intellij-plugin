package com.emoseman.hidpi.services;

import com.emoseman.hidpi.model.HidpiProfile;

public interface IdeSettingsAccessor {
    HidpiProfile capture(String name);

    ProfileApplyResult apply(HidpiProfile profile);
}
