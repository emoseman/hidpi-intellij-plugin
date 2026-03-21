package com.emoseman.hidpi.settings;

import com.emoseman.hidpi.model.HidpiProfile;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class HidpiProfilesTableModel extends AbstractTableModel {
    private final List<HidpiProfile> profiles = new ArrayList<>();
    private static final String[] COLUMNS = {"Profile", "Fonts", "Behavior", "Display"};

    public void setProfiles(List<HidpiProfile> values) {
        profiles.clear();
        profiles.addAll(values);
        fireTableDataChanged();
    }

    public List<HidpiProfile> getProfiles() {
        return profiles.stream().map(HidpiProfile::copy).toList();
    }

    public HidpiProfile getAt(int row) {
        if (row < 0 || row >= profiles.size()) {
            return null;
        }
        return profiles.get(row);
    }

    @Override
    public int getRowCount() {
        return profiles.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        HidpiProfile profile = profiles.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> profile.name;
            case 1 -> profile.summary();
            case 2 -> profile.isDefault ? "Yes" : "";
            case 3 -> profile.autoSwitchRule == null ? "" : profile.autoSwitchRule.bounds + " @ " + String.format("%.2f", profile.autoSwitchRule.scale);
            default -> "";
        };
    }

    public void addProfile(HidpiProfile profile) {
        int row = profiles.size();
        profiles.add(profile);
        fireTableRowsInserted(row, row);
    }

    public void removeProfile(String id) {
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).id.equals(id)) {
                profiles.remove(i);
                fireTableRowsDeleted(i, i);
                return;
            }
        }
    }

    public void setDefaultProfile(String id) {
        for (HidpiProfile profile : profiles) {
            profile.isDefault = profile.id.equals(id);
        }
        fireTableDataChanged();
    }
}
