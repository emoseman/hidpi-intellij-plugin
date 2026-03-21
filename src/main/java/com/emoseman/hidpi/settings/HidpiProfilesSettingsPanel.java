package com.emoseman.hidpi.settings;

import com.emoseman.hidpi.display.DisplayDetectionService;
import com.emoseman.hidpi.display.DisplayInfo;
import com.emoseman.hidpi.model.DisplayRule;
import com.emoseman.hidpi.model.HidpiProfile;
import com.emoseman.hidpi.services.HidpiProfilesService;
import com.emoseman.hidpi.services.IntellijIdeSettingsAccessor;
import com.emoseman.hidpi.services.ProfileApplicationService;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;

import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
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
    private final JTextField profileName = new JTextField();
    private final JBCheckBox profileDefault = new JBCheckBox("Default profile");
    private final JBCheckBox profileRequiresRestart = new JBCheckBox("Requires restart");
    private final JBCheckBox accessibilityOverrideUiFont = new JBCheckBox("Use accessibility font override for IDE UI");
    private final JBCheckBox supportScreenReaders = new JBCheckBox("Support screen readers");
    private final JBCheckBox ruleEnabled = new JBCheckBox("Enable auto-switch rule");
    private final JTextField ruleGraphicsDeviceId = new JTextField();
    private final JTextField ruleBounds = new JTextField();
    private final JSpinner ruleScale = decimalSpinner(1.0d, 0.0d, 10.0d, 0.05d);
    private final String[] fontFamilies = availableFontFamilies();
    private final JComboBox<String> editorFontFamily = new JComboBox<>(fontFamilies.clone());
    private final JSpinner editorFontSize = new JSpinner(new SpinnerNumberModel(13, 1, 200, 1));
    private final JSpinner editorLineSpacing = decimalSpinner(1.2d, 0.5d, 5.0d, 0.05d);
    private final JComboBox<String> consoleFontFamily = new JComboBox<>(fontFamilies.clone());
    private final JSpinner consoleFontSize = new JSpinner(new SpinnerNumberModel(13, 1, 200, 1));
    private final JSpinner consoleLineSpacing = decimalSpinner(1.2d, 0.5d, 5.0d, 0.05d);
    private final JComboBox<String> uiFontFamily = new JComboBox<>(fontFamilies.clone());
    private final JSpinner uiFontSize = new JSpinner(new SpinnerNumberModel(-1, -1, 200, 1));
    private final JSpinner presentationModeFontSize = new JSpinner(new SpinnerNumberModel(-1, -1, 200, 1));
    private final JBLabel detailHint = new JBLabel("Select a profile to edit its font settings.");
    private static final int PROFILE_EDITOR_WIDTH = 350;
    private boolean syncingProfileFields;
    private boolean updatingFontChoices;

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
        configureProfilesTable();

        JBScrollPane editorScrollPane = new JBScrollPane(
                createProfileEditorPanel(),
                JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        editorScrollPane.setBorder(JBUI.Borders.empty());
        Dimension editorSize = JBUI.size(PROFILE_EDITOR_WIDTH, 1);
        editorScrollPane.setPreferredSize(editorSize);
        editorScrollPane.setMinimumSize(editorSize);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JBScrollPane(table), editorScrollPane);
        splitPane.setResizeWeight(1.0d);
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, event -> {
            if (!splitPane.isShowing()) {
                return;
            }
            int availableWidth = splitPane.getWidth();
            int desiredDivider = Math.max(0, availableWidth - JBUI.scale(PROFILE_EDITOR_WIDTH));
            if (availableWidth > 0 && splitPane.getDividerLocation() != desiredDivider) {
                splitPane.setDividerLocation(desiredDivider);
            }
        });
        SwingUtilities.invokeLater(() -> {
            int availableWidth = splitPane.getWidth();
            if (availableWidth > 0) {
                splitPane.setDividerLocation(Math.max(0, availableWidth - JBUI.scale(PROFILE_EDITOR_WIDTH)));
            }
        });
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
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1) {
                    int row = table.rowAtPoint(event.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                        applySelected();
                    }
                }
            }
        });
        bindProfileFieldListeners();
        configureSpinner(ruleScale);
        configureSpinner(editorLineSpacing);
        configureSpinner(consoleLineSpacing);
        configureSpinner(editorFontSize);
        configureSpinner(consoleFontSize);
        configureSpinner(uiFontSize);
        configureSpinner(presentationModeFontSize);
    }

    private void configureProfilesTable() {
        table.setRowHeight(JBUI.scale(60));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, JBUI.scale(6)));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setOpaque(false);
        table.setTableHeader(null);
        table.setDefaultRenderer(Object.class, new ProfileTableCellRenderer());
        table.setRowMargin(0);

        table.getColumnModel().getColumn(0).setPreferredWidth(JBUI.scale(180));
        table.getColumnModel().getColumn(0).setMinWidth(JBUI.scale(150));
        table.getColumnModel().getColumn(1).setPreferredWidth(JBUI.scale(260));
        table.getColumnModel().getColumn(1).setMinWidth(JBUI.scale(220));
        table.getColumnModel().getColumn(2).setPreferredWidth(JBUI.scale(160));
        table.getColumnModel().getColumn(2).setMinWidth(JBUI.scale(140));
        table.getColumnModel().getColumn(3).setPreferredWidth(JBUI.scale(240));
        table.getColumnModel().getColumn(3).setMinWidth(JBUI.scale(180));
    }

    public JComponent getComponent() {
        return root;
    }

    public void resetFromService() {
        HidpiProfilesService service = HidpiProfilesService.getInstance();
        tableModel.setProfiles(service.listProfiles());
        autoSwitch.setSelected(service.isAutoSwitchEnabled());
        if (tableModel.getRowCount() > 0) {
            if (!selectProfile(service.getLastAppliedProfileId())) {
                HidpiProfile defaultProfile = tableModel.getProfiles().stream().filter(profile -> profile.isDefault).findFirst().orElse(null);
                if (defaultProfile == null || !selectProfile(defaultProfile.id)) {
                    table.setRowSelectionInterval(0, 0);
                }
            }
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

        GridBagConstraints row = (GridBagConstraints) gbc.clone();
        panel.add(sectionLabel("Profile"), row);
        row = nextRow(row);
        panel.add(labeledField("Name", profileName), row);
        row = nextRow(row);
        panel.add(profileDefault, row);
        row = nextRow(row);
        panel.add(profileRequiresRestart, row);

        row = nextRow(row);
        panel.add(sectionLabel("Accessibility"), row);
        row = nextRow(row);
        panel.add(accessibilityOverrideUiFont, row);
        row = nextRow(row);
        panel.add(supportScreenReaders, row);

        row = nextRow(row);
        panel.add(sectionLabel("Auto-Switch Rule"), row);
        row = nextRow(row);
        panel.add(ruleEnabled, row);
        row = nextRow(row);
        panel.add(labeledField("Graphics Device ID", ruleGraphicsDeviceId), row);
        row = nextRow(row);
        panel.add(labeledField("Bounds", ruleBounds), row);
        row = nextRow(row);
        panel.add(labeledField("Scale", ruleScale), row);

        row = nextRow(row);
        panel.add(sectionLabel("Editor"), row);
        row = nextRow(row);
        panel.add(labeledField("Font Family", editorFontFamily), row);
        row = nextRow(row);
        panel.add(labeledField("Font Size", editorFontSize), row);
        row = nextRow(row);
        panel.add(labeledField("Line Spacing", editorLineSpacing), row);

        row = nextRow(row);
        panel.add(sectionLabel("Console"), row);
        row = nextRow(row);
        panel.add(labeledField("Font Family", consoleFontFamily), row);
        row = nextRow(row);
        panel.add(labeledField("Font Size", consoleFontSize), row);
        row = nextRow(row);
        panel.add(labeledField("Line Spacing", consoleLineSpacing), row);

        row = nextRow(row);
        panel.add(sectionLabel("UI"), row);
        row = nextRow(row);
        panel.add(labeledField("Font Family", uiFontFamily), row);
        row = nextRow(row);
        panel.add(labeledField("Font Size", uiFontSize), row);
        row = nextRow(row);
        panel.add(labeledField("Presentation Mode Size", presentationModeFontSize), row);
        row = nextRow(row);
        panel.add(detailHint, row);

        GridBagConstraints filler = nextRow(row);
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
        bindTextField(profileName);
        bindCheckBox(profileDefault);
        bindCheckBox(profileRequiresRestart);
        bindCheckBox(accessibilityOverrideUiFont);
        bindCheckBox(supportScreenReaders);
        bindCheckBox(ruleEnabled);
        bindTextField(ruleGraphicsDeviceId);
        bindTextField(ruleBounds);
        bindSpinner(ruleScale);
        editorFontFamily.addActionListener(event -> syncCurrentEditorValues());
        consoleFontFamily.addActionListener(event -> syncCurrentEditorValues());
        uiFontFamily.addActionListener(event -> syncCurrentEditorValues());
        bindSpinner(editorFontSize);
        bindSpinner(editorLineSpacing);
        bindSpinner(consoleFontSize);
        bindSpinner(consoleLineSpacing);
        bindSpinner(uiFontSize);
        bindSpinner(presentationModeFontSize);
        attachComboEditorListener(editorFontFamily);
        attachComboEditorListener(consoleFontFamily);
        attachComboEditorListener(uiFontFamily);
    }

    private void bindTextField(JTextField textField) {
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

    private void bindCheckBox(JBCheckBox checkBox) {
        checkBox.addActionListener(event -> syncCurrentEditorValues());
    }

    private void bindSpinner(JSpinner spinner) {
        ChangeListener listener = event -> syncCurrentEditorValues();
        spinner.addChangeListener(listener);
    }

    private void attachComboEditorListener(JComboBox<String> comboBox) {
        comboBox.setEditable(true);
        if (comboBox.getEditor().getEditorComponent() instanceof JTextField textField) {
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent event) {
                    syncCurrentEditorValues();
                    scheduleFontFilter(comboBox, textField);
                }

                @Override
                public void removeUpdate(DocumentEvent event) {
                    syncCurrentEditorValues();
                    scheduleFontFilter(comboBox, textField);
                }

                @Override
                public void changedUpdate(DocumentEvent event) {
                    syncCurrentEditorValues();
                    scheduleFontFilter(comboBox, textField);
                }
            });
        }
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
                restoreAllFontChoices(comboBox);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent event) {
                restoreAllFontChoices(comboBox);
            }
        });
    }

    private void scheduleFontFilter(JComboBox<String> comboBox, JTextField textField) {
        if (syncingProfileFields || updatingFontChoices) {
            return;
        }
        SwingUtilities.invokeLater(() -> applyFontFilter(comboBox, textField));
    }

    private void loadSelectedProfileIntoEditor() {
        syncingProfileFields = true;
        try {
            HidpiProfile selected = selectedProfile();
            restoreAllFontChoices(editorFontFamily);
            restoreAllFontChoices(consoleFontFamily);
            restoreAllFontChoices(uiFontFamily);
            boolean enabled = selected != null;
            profileName.setEnabled(enabled);
            profileDefault.setEnabled(enabled);
            profileRequiresRestart.setEnabled(enabled);
            accessibilityOverrideUiFont.setEnabled(enabled);
            supportScreenReaders.setEnabled(enabled);
            ruleEnabled.setEnabled(enabled);
            editorFontFamily.setEnabled(enabled);
            editorFontSize.setEnabled(enabled);
            editorLineSpacing.setEnabled(enabled);
            consoleFontFamily.setEnabled(enabled);
            consoleFontSize.setEnabled(enabled);
            consoleLineSpacing.setEnabled(enabled);
            uiFontFamily.setEnabled(enabled && selected != null && selected.accessibilityOverrideUiFont);
            uiFontSize.setEnabled(enabled && selected != null && selected.accessibilityOverrideUiFont);
            presentationModeFontSize.setEnabled(enabled);
            if (!enabled) {
                detailHint.setText("Select a profile to edit its settings.");
                profileName.setText("");
                profileDefault.setSelected(false);
                profileRequiresRestart.setSelected(false);
                accessibilityOverrideUiFont.setSelected(false);
                supportScreenReaders.setSelected(false);
                ruleEnabled.setSelected(false);
                ruleGraphicsDeviceId.setText("");
                ruleBounds.setText("");
                ruleScale.setValue(1.0d);
                setRuleFieldState(false);
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

            detailHint.setText("All editable profile settings are available in this pane.");
            profileName.setText(selected.name);
            profileDefault.setSelected(selected.isDefault);
            profileRequiresRestart.setSelected(selected.requiresRestart);
            accessibilityOverrideUiFont.setSelected(selected.accessibilityOverrideUiFont);
            supportScreenReaders.setSelected(selected.supportScreenReaders);
            DisplayRule rule = selected.autoSwitchRule == null ? new DisplayRule() : selected.autoSwitchRule;
            ruleEnabled.setSelected(selected.autoSwitchRule != null && selected.autoSwitchRule.enabled);
            ruleGraphicsDeviceId.setText(rule.graphicsDeviceId);
            ruleBounds.setText(rule.bounds);
            ruleScale.setValue(rule.scale);
            setRuleFieldState(selected.autoSwitchRule != null && selected.autoSwitchRule.enabled);
            editorFontFamily.setSelectedItem(selected.editor.family);
            editorFontSize.setValue(selected.editor.size);
            editorLineSpacing.setValue((double) selected.editor.lineSpacing);
            consoleFontFamily.setSelectedItem(selected.console.family);
            consoleFontSize.setValue(selected.console.size);
            consoleLineSpacing.setValue((double) selected.console.lineSpacing);
            uiFontFamily.setSelectedItem(selected.uiFontFamily);
            uiFontSize.setValue(selected.uiFontSize);
            presentationModeFontSize.setValue(selected.presentationModeFontSize);
            setUiFontFieldState(selected.accessibilityOverrideUiFont);
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

        selected.name = profileName.getText().trim();
        selected.isDefault = profileDefault.isSelected();
        selected.requiresRestart = profileRequiresRestart.isSelected();
        selected.accessibilityOverrideUiFont = accessibilityOverrideUiFont.isSelected();
        selected.supportScreenReaders = supportScreenReaders.isSelected();
        if (ruleEnabled.isSelected()) {
            if (selected.autoSwitchRule == null) {
                selected.autoSwitchRule = new DisplayRule();
            }
            selected.autoSwitchRule.enabled = true;
            selected.autoSwitchRule.graphicsDeviceId = ruleGraphicsDeviceId.getText().trim();
            selected.autoSwitchRule.bounds = ruleBounds.getText().trim();
            selected.autoSwitchRule.scale = ((Number) ruleScale.getValue()).doubleValue();
        } else {
            selected.autoSwitchRule = null;
        }
        setRuleFieldState(ruleEnabled.isSelected());
        setUiFontFieldState(selected.accessibilityOverrideUiFont);
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

    private void setRuleFieldState(boolean enabled) {
        ruleGraphicsDeviceId.setEnabled(enabled);
        ruleBounds.setEnabled(enabled);
        ruleScale.setEnabled(enabled);
    }

    private void setUiFontFieldState(boolean enabled) {
        uiFontFamily.setEnabled(enabled);
        uiFontSize.setEnabled(enabled);
    }

    private String comboValue(JComboBox<String> comboBox) {
        Object item = comboBox.getEditor().getItem();
        return item == null ? "" : item.toString().trim();
    }

    private boolean selectProfile(String profileId) {
        if (profileId == null || profileId.isBlank()) {
            return false;
        }
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            HidpiProfile profile = tableModel.getAt(row);
            if (profile != null && Objects.equals(profile.id, profileId)) {
                table.setRowSelectionInterval(row, row);
                return true;
            }
        }
        return false;
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
                && left.accessibilityOverrideUiFont == right.accessibilityOverrideUiFont
                && left.supportScreenReaders == right.supportScreenReaders
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

    private void applyFontFilter(JComboBox<String> comboBox, JTextField textField) {
        if (syncingProfileFields || updatingFontChoices || !textField.isDisplayable()) {
            return;
        }

        String typed = textField.getText();
        int caret = textField.getCaretPosition();
        String filter = typed == null ? "" : typed.trim().toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String family : fontFamilies) {
            if (filter.isEmpty() || family.toLowerCase().contains(filter)) {
                matches.add(family);
            }
        }

        updatingFontChoices = true;
        try {
            replaceComboChoices(comboBoxModel(comboBox), matches);
            comboBox.getEditor().setItem(typed);
        } finally {
            updatingFontChoices = false;
        }

        if (!Objects.equals(textField.getText(), typed)) {
            textField.setText(typed);
        }
        textField.setCaretPosition(Math.min(caret, textField.getText().length()));
        if (comboBox.isShowing()) {
            comboBox.setPopupVisible(textField.isFocusOwner() && !matches.isEmpty());
        }
    }

    private void restoreAllFontChoices(JComboBox<String> comboBox) {
        if (updatingFontChoices) {
            return;
        }
        Object currentItem = comboBox.getEditor().getItem();
        updatingFontChoices = true;
        try {
            replaceComboChoices(comboBoxModel(comboBox), List.of(fontFamilies));
            comboBox.getEditor().setItem(currentItem);
        } finally {
            updatingFontChoices = false;
        }
    }

    private void replaceComboChoices(DefaultComboBoxModel<String> model, List<String> choices) {
        if (model.getSize() == choices.size()) {
            boolean sameChoices = true;
            for (int i = 0; i < choices.size(); i++) {
                if (!Objects.equals(model.getElementAt(i), choices.get(i))) {
                    sameChoices = false;
                    break;
                }
            }
            if (sameChoices) {
                return;
            }
        }

        model.removeAllElements();
        for (String choice : choices) {
            model.addElement(choice);
        }
    }

    private DefaultComboBoxModel<String> comboBoxModel(JComboBox<String> comboBox) {
        if (comboBox.getModel() instanceof DefaultComboBoxModel<String> model) {
            return model;
        }
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(fontFamilies.clone());
        comboBox.setModel(model);
        return model;
    }

    private final class ProfileTableCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();

        private ProfileTableCellRenderer() {
            super(new BorderLayout(0, JBUI.scale(3)));
            title.setFont(JBFont.label().asBold());
            subtitle.setFont(JBFont.small());
            subtitle.setForeground(JBColor.GRAY);
            subtitle.setVerticalAlignment(SwingConstants.TOP);
            setBorder(new EmptyBorder(JBUI.insets(8, 10)));
            add(title, BorderLayout.NORTH);
            add(subtitle, BorderLayout.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            HidpiProfile profile = tableModel.getAt(row);
            if (profile == null) {
                title.setText("");
                subtitle.setText("");
                return this;
            }

            switch (column) {
                case 0 -> {
                    title.setText(profile.name);
                    subtitle.setText(compactProfileFlags(profile));
                }
                case 1 -> {
                    title.setText(profile.editor.family + " " + profile.editor.size + " / " + profile.console.family + " " + profile.console.size);
                    subtitle.setText("UI " + uiFontSummary(profile) + "  |  Presentation " + numericOrDefault(profile.presentationModeFontSize));
                }
                case 2 -> {
                    title.setText(profile.accessibilityOverrideUiFont ? "Accessibility UI font enabled" : "Accessibility UI font disabled");
                    subtitle.setText(profile.supportScreenReaders ? "Screen readers on" : "Screen readers off");
                }
                case 3 -> {
                    title.setText(profile.autoSwitchRule == null ? "No display rule" : "Auto-switch rule active");
                    subtitle.setText(profile.autoSwitchRule == null
                            ? "Use Detect Current Display to bind a monitor"
                            : profile.autoSwitchRule.bounds + "  |  scale " + String.format("%.2f", profile.autoSwitchRule.scale));
                }
                default -> {
                    title.setText(Objects.toString(value, ""));
                    subtitle.setText("");
                }
            }

            JBColor background = isSelected ? new JBColor(0xDCEBFF, 0x2F4154) : new JBColor(0xF7F9FC, 0x31343B);
            java.awt.Color foreground = isSelected ? new JBColor(0x1E2A36, 0xE6EEF8) : JBColor.foreground();
            java.awt.Color secondary = isSelected ? new JBColor(0x496074, 0xB8C7D9) : JBColor.GRAY;
            setBackground(background);
            title.setForeground(foreground);
            subtitle.setForeground(secondary);
            return this;
        }

        private String compactProfileFlags(HidpiProfile profile) {
            List<String> flags = new ArrayList<>();
            if (profile.isDefault) {
                flags.add("Default");
            }
            if (profile.requiresRestart) {
                flags.add("Restart required");
            }
            if (!profile.isDefault && !profile.requiresRestart) {
                flags.add("Standard profile");
            }
            return String.join("  |  ", flags);
        }

        private String uiFontSummary(HidpiProfile profile) {
            String family = profile.uiFontFamily == null || profile.uiFontFamily.isBlank() ? "system" : profile.uiFontFamily;
            return family + " " + numericOrDefault(profile.uiFontSize);
        }

        private String numericOrDefault(int value) {
            return value > 0 ? Integer.toString(value) : "default";
        }
    }
}
