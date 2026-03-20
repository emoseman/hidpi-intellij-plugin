package com.emoseman.hidpi.model;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.XCollection;

import java.util.ArrayList;
import java.util.List;

public class HidpiProfilesState {
    @Attribute("version")
    public int version = 1;

    @Attribute("autoSwitchEnabled")
    public boolean autoSwitchEnabled;

    @Attribute("lastAppliedProfileId")
    public String lastAppliedProfileId = "";

    @XCollection
    public List<HidpiProfile> profiles = new ArrayList<>();
}
