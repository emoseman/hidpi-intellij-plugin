package com.emoseman.hidpi.settings;

import com.emoseman.hidpi.display.DisplayDetectionService;
import com.emoseman.hidpi.display.DisplayInfo;
import com.emoseman.hidpi.model.DisplayRule;
import com.emoseman.hidpi.model.HidpiProfile;
import com.emoseman.hidpi.services.HidpiProfilesService;
import com.emoseman.hidpi.services.IntellijIdeSettingsAccessor;
import com.emoseman.hidpi.services.ProfileApplicationService;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HidpiProfilesSettingsPanel {
    private final JPanel root = new JPanel(new BorderLayout());
    private final HidpiProfilesTableModel tableModel = new HidpiProfilesTableModel();
    private final JTable table = new JTable(tableModel);
    private final JBCheckBox autoSwitch = new JBCheckBox("Enable display-based auto-switch");
    private final JBLabel warning = new JBLabel("Note: UI font and scale behavior varies by platform. Restart may be required.");
    private final JComboBox<String> editorFontFamily = new JComboBox<>(availableFontFamilies());
    private final JSpinner editorFontSize = new JSpinner(new SpinnerNumberModel(13, 1, 200, 1));
    private final JSpinner editorLineSpacing = decimalSpinner(1.2d, 0.5d, 5.0d, 0.05d);
    private final JComboBox<String> consoleFontFamily = new JComboBox<>(availableFontFamilies());
    private final JSpinner consoleFontSize = new JSpinner(new SpinnerNumberModel(13, 1, 200, 1));
    private final JSpinner consoleLineSpacing = decimalSpinner(1.2d, 0.5d, 5.0d, 0.05d);
    private final JComboBox<String> uiFontFamily = new JComboBox<>(availableFontFamilies());
    private final JSpinner uiFontSize = new JSpinner(new SpinnerNumberModel(-1, -1, 200, 1));
    private final JSpinner presentationModeFontSize = new JSpinner(new SpinnerNumberModel(-1, -1, 200, 1));
    private final JBLabel detailHint = new JBLabel("Select a profile to edit its font settings.");
    private boolean syncingProfileFields;

    public HidpiProfilesSettingsPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addCurrent = new JButton("Add Current");
        JButton apply = new JButton("Apply");
        JButton rename = new JButton("Rename");
        JButton duplicate = new JButton("Duplicate");
        JButton delete = new JButton("Delete");
        JButton setDefault = new JButton("Set Default");
        JButton detectDisplay = new JButton("Detect Current Display");
        JButton createRule = new JButton("Create Rule from Current Display");

        buttonPanel.add(addCurrent);
        buttonPanel.add(apply);
        buttonPanel.add(rename);
        buttonPanel.add(duplicate);
        buttonPanel.add(delete);
        buttonPanel.add(setDefault);
        buttonPanel.add(detectDisplay);
        buttonPanel.add(createRule);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JBScrollPane(table), createProfileEditorPanel());
        splitPane.setResizeWeight(0.45d);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(autoSwitch, BorderLayout.WEST);
        bottom.add(warning, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        addCurrent.addActionListener(event -> addCurrent());
        apply.addActionListener(event -> applySelected());
        rename.addActionListener(event -> renameSelected());
        duplicate.addActionListener(event -> duplicateSelected());
        delete.addActionListener(event -> deleteSelected());
        setDefault.addActionListener(event -> setDefaultSelected());
        detectDisplay.addActionListener(event -> detectCurrentDisplay());
        createRule.addActionListener(event -> createRuleForSelected());
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedProfileIntoEditor();
            }
        });
        bindProfileFieldListeners();
        configureSpinner(editorLineSpacing);
        configureSpinner(consoleLineSpacing);
        configureSpinner(editorFontSize);
        configureSpinner(consoleFontSize);
        configureSpinner(uiFontSize);
        configureSpinner(presentationModeFontSize);
    }

    public JComponent getComponent() {
        return root;
    }

    public void resetFromService() {
        HidpiProfilesService service = HidpiProfilesService.getInstance();
        tableModel.setProfiles(service.listProfiles());
        autoSwitch.setSelected(service.isAutoSwitchEnabled());
        if (tableModel.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            table.clearSelection();
            loadSelectedProfileIntoEditor();
        }
    }

    public void applyToService() {
        HidpiProfilesService service = HidpiProfilesService.getInstance();
        syncCurrentEditorValues();
        service.replaceProfiles(tableModel.getProfiles());
        service.setAutoSwitchEnabled(autoSwitch.isSelected());
    }

    public boolean isModified() {
        HidpiProfilesService service = HidpiProfilesService.getInstance();
        syncCurrentEditorValues();
        return autoSwitch.isSelected() != service.isAutoSwitchEnabled()
                || !sameProfiles(tableModel.getProfiles(), service.listProfiles());
    }

    private HidpiProfile selectedProfile() {
        return tableModel.getAt(table.getSelectedRow());
    }

    private void addCurrent() {
        String name = Messages.showInputDialog("Profile name:", "Add Current Profile", Messages.getQuestionIcon());
        if (name == null || name.isBlank()) {
            return;
        }
        HidpiProfile profile = IntellijIdeSettingsAccessor.getInstance().capture(name.trim());
        profile.id = UUID.randomUUID().toString();
        tableModel.addProfile(profile);
        selectProfile(profile.id);
    }

    private void applySelected() {
        HidpiProfile selected = selectedProfile();
        if (selected == null) {
            return;
        }
        syncCurrentEditorValues();
        ProfileApplicationService.getInstance().apply(selected, null);
    }

    private void renameSelected() {
        HidpiProfile selected = selectedProfile();
        if (selected == null) {
            return;
        }
        String name = Messages.showInputDialog("New profile name:", "Rename HiDPI Profile", Messages.getQuestionIcon());
        if (name == null || name.isBlank()) {
            return;
        }
        selected.name = name.trim();
        tableModel.fireTableDataChanged();
        selectProfile(selected.id);
    }

    private void duplicateSelected() {
        HidpiProfile selected = selectedProfile();
        if (selected == null) {
            return;
        }
        String name = Messages.showInputDialog("Name for duplicate:", "Duplicate HiDPI Profile", Messages.getQuestionIcon());
        if (name == null || name.isBlank()) {
            return;
        }
        HidpiProfile copy = selected.copy();
        copy.id = UUID.randomUUID().toString();
        copy.name = name.trim();
        copy.isDefault = false;
        tableModel.addProfile(copy);
        selectProfile(copy.id);
    }

    private void deleteSelected() {
        HidpiProfile selected = selectedProfile();
        if (selected == null) {
            return;
        }
        int confirm = Messages.showYesNoDialog("Delete profile " + selected.name + "?", "Delete HiDPI Profile", null);
        if (confirm == Messages.YES) {
            int row = table.getSelectedRow();
            tableModel.removeProfile(selected.id);
            if (tableModel.getRowCount() > 0) {
                int newRow = Math.min(row, tableModel.getRowCount() - 1);
                table.setRowSelectionInterval(newRow, newRow);
            } else {
                table.clearSelection();
                loadSelectedProfileIntoEditor();
            }
        }
    }

    private void setDefaultSelected() {
        HidpiProfile selected = selectedProfile();
        if (selected == null) {
            return;
        }
        tableModel.setDefaultProfile(selected.id);
        selectProfile(selected.id);
    }

    private void detectCurrentDisplay() {
        DisplayInfo info = DisplayDetectionService.getInstance().detectCurrentDisplay();
        Messages.showInfoMessage(info.summary(), "Current Display");
    }

    private void createRuleForSelected() {
        HidpiProfile selected = selectedProfile();
        if (selected == null) {
            return;
        }
        DisplayInfo info = DisplayDetectionService.getInstance().detectCurrentDisplay();
        DisplayRule rule = new DisplayRule();
        rule.graphicsDeviceId = info.graphicsDeviceId();
        rule.bounds = info.nativeBoundsKey();
        rule.scale = info.scale();
        selected.autoSwitchRule = rule;
        tableModel.fireTableDataChanged();
        selectProfile(selected.id);
    }

    private JPanel createProfileEditorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;

        panel.add(sectionLabel("Editor"), gbc);
        panel.add(labeledField("Font Family", editorFontFamily), nextRow(gbc));
        panel.add(labeledField("Font Size", editorFontSize), nextRow(gbc));
        panel.add(labeledField("Line Spacing", editorLineSpacing), nextRow(gbc));

        panel.add(sectionLabel("Console"), nextRow(gbc));
        panel.add(labeledField("Font Family", consoleFontFamily), nextRow(gbc));
        panel.add(labeledField("Font Size", consoleFontSize), nextRow(gbc));
        panel.add(labeledField("Line Spacing", consoleLineSpacing), nextRow(gbc));

        panel.add(sectionLabel("UI"), nextRow(gbc));
        panel.add(labeledField("Font Family", uiFontFamily), nextRow(gbc));
        panel.add(labeledField("Font Size", uiFontSize), nextRow(gbc));
        panel.add(labeledField("Presentation Mode Size", presentationModeFontSize), nextRow(gbc));
        panel.add(detailHint, nextRow(gbc));

        GridBagConstraints filler = nextRow(gbc);
        filler.weighty = 1.0d;
        filler.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), filler);
        return panel;
    }

    private JLabel sectionLabel(String text) {
        return new JLabel(text);
    }

    private JPanel labeledField(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private GridBagConstraints nextRow(GridBagConstraints gbc) {
        GridBagConstraints copy = (GridBagConstraints) gbc.clone();
        copy.gridy++;
        return copy;
    }

    private void bindProfileFieldListeners() {
        editorFontFamily.addActionListener(event -> syncCurrentEditorValues());
        consoleFontFamily.addActionListener(event -> syncCurrentEditorValues());
        uiFontFamily.addActionListener(event -> syncCurrentEditorValues());
        editorFontSize.addChangeListener(event -> syncCurrentEditorValues());
        editorLineSpacing.addChangeListener(event -> syncCurrentEditorValues());
        consoleFontSize.addChangeListener(event -> syncCurrentEditorValues());
        consoleLineSpacing.addChangeListener(event -> syncCurrentEditorValues());
        uiFontSize.addChangeListener(event -> syncCurrentEditorValues());
        presentationModeFontSize.addChangeListener(event -> syncCurrentEditorValues());
        attachComboEditorListener(editorFontFamily);
        attachComboEditorListener(consoleFontFamily);
        attachComboEditorListener(uiFontFamily);
    }

    private void attachComboEditorListener(JComboBox<String> comboBox) {
        comboBox.setEditable(true);
        if (comboBox.getEditor().getEditorComponent() instanceof JTextField textField) {
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    syncCurrentEditorValues();
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    syncCurrentEditorValues();
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    syncCurrentEditorValues();
                }
            });
        }
    }

    private void loadSelectedProfileIntoEditor() {
        syncingProfileFields = true;
        try {
            HidpiProfile selected = selectedProfile();
            boolean enabled = selected != null;
            editorFontFamily.setEnabled(enabled);
            editorFontSize.setEnabled(enabled);
            editorLineSpacing.setEnabled(enabled);
            consoleFontFamily.setEnabled(enabled);
            consoleFontSize.setEnabled(enabled);
            consoleLineSpacing.setEnabled(enabled);
            uiFontFamily.setEnabled(enabled);
            uiFontSize.setEnabled(enabled);
            presentationModeFontSize.setEnabled(enabled);
            if (!enabled) {
                detailHint.setText("Select a profile to edit its font settings.");
                editorFontFamily.setSelectedItem("");
                consoleFontFamily.setSelectedItem("");
                uiFontFamily.setSelectedItem("");
                editorFontSize.setValue(13);
                editorLineSpacing.setValue(1.2d);
                consoleFontSize.setValue(13);
                consoleLineSpacing.setValue(1.2d);
                uiFontSize.setValue(-1);
                presentationModeFontSize.setValue(-1);
                return;
            }

            detailHint.setText("Manual font size overrides are saved with the selected profile.");
            editorFontFamily.setSelectedItem(selected.editor.family);
            editorFontSize.setValue(selected.editor.size);
            editorLineSpacing.setValue((double) selected.editor.lineSpacing);
            consoleFontFamily.setSelectedItem(selected.console.family);
            consoleFontSize.setValue(selected.console.size);
            consoleLineSpacing.setValue((double) selected.console.lineSpacing);
            uiFontFamily.setSelectedItem(selected.uiFontFamily);
            uiFontSize.setValue(selected.uiFontSize);
            presentationModeFontSize.setValue(selected.presentationModeFontSize);
        } finally {
            syncingProfileFields = false;
        }
    }

    private void syncCurrentEditorValues() {
        if (syncingProfileFields) {
            return;
        }
        HidpiProfile selected = selectedProfile();
        if (selected == null) {
            return;
        }

        selected.editor.family = comboValue(editorFontFamily);
        selected.editor.size = ((Number) editorFontSize.getValue()).intValue();
        selected.editor.lineSpacing = ((Number) editorLineSpacing.getValue()).floatValue();
        selected.console.family = comboValue(consoleFontFamily);
        selected.console.size = ((Number) consoleFontSize.getValue()).intValue();
        selected.console.lineSpacing = ((Number) consoleLineSpacing.getValue()).floatValue();
        selected.uiFontFamily = comboValue(uiFontFamily);
        selected.uiFontSize = ((Number) uiFontSize.getValue()).intValue();
        selected.presentationModeFontSize = ((Number) presentationModeFontSize.getValue()).intValue();

        int row = table.getSelectedRow();
        if (row >= 0) {
            tableModel.fireTableRowsUpdated(row, row);
        } else {
            tableModel.fireTableDataChanged();
        }
    }

    private String comboValue(JComboBox<String> comboBox) {
        Object item = comboBox.getEditor().getItem();
        return item == null ? "" : item.toString().trim();
    }

    private void selectProfile(String profileId) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            HidpiProfile profile = tableModel.getAt(row);
            if (profile != null && Objects.equals(profile.id, profileId)) {
                table.setRowSelectionInterval(row, row);
                return;
            }
        }
    }

    private boolean sameProfiles(List<HidpiProfile> left, List<HidpiProfile> right) {
        List<HidpiProfile> sortedLeft = left.stream()
                .map(HidpiProfile::copy)
                .sorted(Comparator.comparing((HidpiProfile profile) -> profile.name.toLowerCase()).thenComparing(profile -> profile.id))
                .toList();
        List<HidpiProfile> sortedRight = right.stream()
                .map(HidpiProfile::copy)
                .sorted(Comparator.comparing((HidpiProfile profile) -> profile.name.toLowerCase()).thenComparing(profile -> profile.id))
                .toList();
        if (sortedLeft.size() != sortedRight.size()) {
            return false;
        }
        for (int i = 0; i < sortedLeft.size(); i++) {
            if (!sameProfile(sortedLeft.get(i), sortedRight.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean sameProfile(HidpiProfile left, HidpiProfile right) {
        return Objects.equals(left.id, right.id)
                && Objects.equals(left.name, right.name)
                && left.isDefault == right.isDefault
                && left.requiresRestart == right.requiresRestart
                && sameFontSetting(left.editor, right.editor)
                && sameFontSetting(left.console, right.console)
                && Objects.equals(left.uiFontFamily, right.uiFontFamily)
                && left.uiFontSize == right.uiFontSize
                && left.presentationModeFontSize == right.presentationModeFontSize
                && sameRule(left.autoSwitchRule, right.autoSwitchRule);
    }

    private boolean sameFontSetting(com.emoseman.hidpi.model.FontSetting left, com.emoseman.hidpi.model.FontSetting right) {
        return Objects.equals(left.family, right.family)
                && left.size == right.size
                && Math.abs(left.lineSpacing - right.lineSpacing) < 0.0001f;
    }

    private boolean sameRule(DisplayRule left, DisplayRule right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return Objects.equals(left.graphicsDeviceId, right.graphicsDeviceId)
                && Objects.equals(left.bounds, right.bounds)
                && Math.abs(left.scale - right.scale) < 0.0001d
                && left.enabled == right.enabled;
    }

    private static JSpinner decimalSpinner(double value, double min, double max, double step) {
        return new JSpinner(new SpinnerNumberModel(value, min, max, step));
    }

    private static void configureSpinner(JSpinner spinner) {
        if (spinner.getEditor() instanceof JSpinner.NumberEditor editor) {
            NumberFormat format = editor.getFormat();
            format.setGroupingUsed(false);
        }
    }

    private static String[] availableFontFamilies() {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String[] withBlank = new String[names.length + 1];
        withBlank[0] = "";
        System.arraycopy(names, 0, withBlank, 1, names.length);
        return withBlank;
    }
}
