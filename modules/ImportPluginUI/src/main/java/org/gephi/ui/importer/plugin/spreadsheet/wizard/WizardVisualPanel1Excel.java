/*
Copyright 2008-2016 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2016 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2016 Gephi Consortium.
 */

package org.gephi.ui.importer.plugin.spreadsheet.wizard;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.gephi.io.importer.plugin.file.spreadsheet.ImporterSpreadsheetExcel;
import org.gephi.io.importer.plugin.file.spreadsheet.process.SpreadsheetGeneralConfiguration.Mode;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;

/**
 * @author Eduardo Ramos
 */
public class WizardVisualPanel1Excel extends AbstractWizardVisualPanel1 {

    private final ImporterSpreadsheetExcel importer;

    private final WizardPanel1Excel wizard1;
    private ValidationPanel validationPanel;

    private boolean initialized = false;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filePathLabel;
    private javax.swing.JComboBox modeComboBox;
    private javax.swing.JLabel modeLabel;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JTable previewTable;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JComboBox sheetComboBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form WizardVisualPanel1CSV
     */
    public WizardVisualPanel1Excel(ImporterSpreadsheetExcel importer, WizardPanel1Excel wizard1) {
        super(importer);
        initComponents();
        this.wizard1 = wizard1;
        this.importer = importer;

        for (Mode mode : Mode.values()) {
            modeComboBox.addItem(new ImportModeWrapper(mode));
        }

        //Setup with initial values:
        //Sheet:
        for (String sheetName : importer.getAvailableSheetNames()) {
            sheetComboBox.addItem(sheetName);
        }

        //Mode:
        modeComboBox.setSelectedItem(new ImportModeWrapper(importer.getMode()));

        //File path:
        final String filePath = importer.getFile().getAbsolutePath();
        pathTextField.setText(filePath);
        pathTextField.setToolTipText(filePath);

        initialized = true;

        refreshPreviewTable();
    }

    public ValidationPanel getValidationPanel() {
        if (validationPanel != null) {
            return validationPanel;
        }
        validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(WizardVisualPanel1Excel.this);
        ValidationGroup validationGroup = validationPanel.getValidationGroup();
        validationGroup.add(pathTextField, new Validator<String>() {

            @Override
            public boolean validate(Problems prblms, String string, String t) {
                if (!areValidColumnsForMode()) {
                    prblms.add(getMessage("WizardVisualPanel1CSV.validation.edges.no-source-target-columns"));
                    return false;
                }
                if (hasRowsMissingSourcesOrTargets()) {
                    prblms.add(NbBundle.getMessage(WizardVisualPanel1Excel.class,
                        "WizardVisualPanel1CSV.validation.edges.empty-sources-or-targets"
                    ), Severity.WARNING);
                }
                return true;
            }
        });
        validationPanel.setName(getName());

        return validationPanel;
    }

    @Override
    public final void refreshPreviewTable() {
        super.refreshPreviewTable();
        wizard1.fireChangeEvent();
        pathTextField.setText(pathTextField.getText());//To fire validation panel messages.
    }

    @Override
    public String getName() {
        return getMessage("WizardVisualPanel1Excel.name");
    }

    @Override
    public Mode getSelectedMode() {
        if (modeComboBox.getItemCount() == 0) {
            return Mode.ADJACENCY_LIST;
        }
        return ((ImportModeWrapper) modeComboBox.getSelectedItem()).getMode();
    }

    public int getSelectedSheetIndex() {
        return sheetComboBox.getSelectedIndex();
    }

    public int getColumnCount() {
        return columnCount;
    }

    public boolean hasColumns() {
        return getColumnCount() > 0;
    }

    public boolean areValidColumnsForMode() {
        switch (getSelectedMode()) {
            case EDGES_TABLE:
                return hasSourceNodeColumn && hasTargetNodeColumn;
            case NODES_TABLE:
                return getColumnCount() > 0;
            default:
                return true;
        }
    }

    public boolean isValidData() {
        return areValidColumnsForMode();
    }

    public boolean hasRowsMissingSourcesOrTargets() {
        return hasRowsMissingSourcesOrTargets;
    }

    private String getMessage(String resName) {
        return NbBundle.getMessage(WizardVisualPanel1Excel.class, resName);
    }

    @Override
    protected JTable getPreviewTable() {
        return previewTable;
    }

    @Override
    protected JScrollPane getPreviewTableScrollPane() {
        return scroll;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filePathLabel = new javax.swing.JLabel();
        pathTextField = new javax.swing.JTextField();
        separatorLabel = new javax.swing.JLabel();
        sheetComboBox = new javax.swing.JComboBox();
        modeLabel = new javax.swing.JLabel();
        modeComboBox = new javax.swing.JComboBox();
        previewLabel = new javax.swing.JLabel();
        scroll = new javax.swing.JScrollPane();
        previewTable = new javax.swing.JTable();

        filePathLabel.setText(org.openide.util.NbBundle
            .getMessage(WizardVisualPanel1Excel.class, "WizardVisualPanel1Excel.filePathLabel.text")); // NOI18N

        pathTextField.setEditable(false);
        pathTextField.setText(org.openide.util.NbBundle
            .getMessage(WizardVisualPanel1Excel.class, "WizardVisualPanel1Excel.pathTextField.text")); // NOI18N

        separatorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        separatorLabel.setText(org.openide.util.NbBundle
            .getMessage(WizardVisualPanel1Excel.class, "WizardVisualPanel1Excel.separatorLabel.text")); // NOI18N

        sheetComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sheetComboBoxItemStateChanged(evt);
            }
        });

        modeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        modeLabel.setText(org.openide.util.NbBundle
            .getMessage(WizardVisualPanel1Excel.class, "WizardVisualPanel1Excel.modeLabel.text")); // NOI18N

        modeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                modeComboBoxItemStateChanged(evt);
            }
        });

        previewLabel.setText(org.openide.util.NbBundle
            .getMessage(WizardVisualPanel1Excel.class, "WizardVisualPanel1Excel.previewLabel.text")); // NOI18N

        previewTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        scroll.setViewportView(previewTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(scroll, javax.swing.GroupLayout.Alignment.LEADING,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                        .addComponent(filePathLabel, javax.swing.GroupLayout.Alignment.LEADING,
                            javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                        .addComponent(pathTextField, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(previewLabel)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(separatorLabel, javax.swing.GroupLayout.DEFAULT_SIZE,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(sheetComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(modeLabel, javax.swing.GroupLayout.DEFAULT_SIZE,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(modeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 123,
                                    javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(filePathLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(pathTextField, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(separatorLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(sheetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(modeLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(modeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(18, 18, 18)
                    .addComponent(previewLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sheetComboBoxItemStateChanged(
        java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sheetComboBoxItemStateChanged
        if (initialized) {
            importer.setSheetIndex(getSelectedSheetIndex());
            refreshPreviewTable();
        }
    }//GEN-LAST:event_sheetComboBoxItemStateChanged

    private void modeComboBoxItemStateChanged(
        java.awt.event.ItemEvent evt) {//GEN-FIRST:event_modeComboBoxItemStateChanged
        if (initialized) {
            importer.setMode(getSelectedMode());
            refreshPreviewTable();
        }
    }//GEN-LAST:event_modeComboBoxItemStateChanged
}
