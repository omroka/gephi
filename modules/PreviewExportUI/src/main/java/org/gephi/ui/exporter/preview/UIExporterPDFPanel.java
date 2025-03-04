/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

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

Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.ui.exporter.preview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.lib.validation.ValidationClient;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author Mathieu Bastian
 */
public class UIExporterPDFPanel extends javax.swing.JPanel implements ValidationClient {

    private static final double INCH = 72.0;
    private static final double MM = 2.8346456692895527;
    private final String customSizeString;
    private final NumberFormat sizeFormatter;
    private final NumberFormat marginFormatter;
    private boolean millimeter = true;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bottomMarginTextField;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JLabel heightUnitLabel;
    private javax.swing.JLabel labelBackground;
    private javax.swing.JLabel labelBottom;
    private javax.swing.JLabel labelHeight;
    private javax.swing.JLabel labelLeft;
    private javax.swing.JLabel labelMargins;
    private javax.swing.JLabel labelOrientation;
    private javax.swing.JLabel labelPageSize;
    private javax.swing.JLabel labelRight;
    private javax.swing.JLabel labelTop;
    private javax.swing.JLabel labelUnit;
    private javax.swing.JLabel labelWidth;
    private javax.swing.JRadioButton landscapeRadio;
    private javax.swing.JTextField leftMarginTextField;
    private javax.swing.ButtonGroup orientationButtonGroup;
    private javax.swing.JComboBox pageSizeCombo;
    private javax.swing.JRadioButton portraitRadio;
    private javax.swing.JTextField rightMargintextField;
    private javax.swing.JTextField topMarginTextField;
    private javax.swing.JCheckBox transparentBackgroundCheckbox;
    private org.jdesktop.swingx.JXHyperlink unitLink;
    private javax.swing.JTextField widthTextField;
    private javax.swing.JLabel widthUnitLabel;
    // End of variables declaration//GEN-END:variables

    public UIExporterPDFPanel() {
        initComponents();

        sizeFormatter = NumberFormat.getNumberInstance();
        sizeFormatter.setMaximumFractionDigits(3);
        marginFormatter = NumberFormat.getNumberInstance();
        marginFormatter.setMaximumFractionDigits(1);

        //Page size model - http://en.wikipedia.org/wiki/Paper_size
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(new PageSizeItem(PDRectangle.A0, "A0", 841, 1189, 33.1, 46.8));
        comboBoxModel.addElement(new PageSizeItem(PDRectangle.A1, "A1", 594, 841, 23.4, 33.1));
        comboBoxModel.addElement(new PageSizeItem(PDRectangle.A2, "A2", 420, 594, 16.5, 23.4));
        comboBoxModel.addElement(new PageSizeItem(PDRectangle.A3, "A3", 297, 420, 11.7, 16.5));
        comboBoxModel.addElement(new PageSizeItem(PDRectangle.A4, "A4", 210, 297, 8.3, 11.7));
        comboBoxModel.addElement(new PageSizeItem(PDRectangle.A5, "A5", 148, 210, 5.8, 8.3));
        comboBoxModel.addElement(new PageSizeItem(PDRectangle.LEGAL, "Legal", 216, 356, 8.5, 14));
        comboBoxModel.addElement(new PageSizeItem(PDRectangle.LETTER, "Letter", 216, 279, 8.5, 11));

        customSizeString = NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.pageSize.custom");
        comboBoxModel.addElement(customSizeString);
        pageSizeCombo.setModel(comboBoxModel);

        loadPreferences();

        initEvents();
        refreshUnit(false);
    }

    public static ValidationPanel createValidationPanel(UIExporterPDFPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();

        innerPanel.validate(group);

        return validationPanel;
    }

    private void loadPreferences() {
        boolean defaultMM = NbPreferences.forModule(UIExporterPDF.class).getBoolean("Default_Millimeter", false);
        millimeter = NbPreferences.forModule(UIExporterPDF.class).getBoolean("Millimeter", defaultMM);
    }

    private void savePreferences() {
        NbPreferences.forModule(UIExporterPDF.class).putBoolean("Millimeter", millimeter);
    }

    private void initEvents() {
        pageSizeCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                Object selectedItem = pageSizeCombo.getSelectedItem();
                if (selectedItem != customSizeString) {
                    PageSizeItem pageSize = (PageSizeItem) selectedItem;
                    setPageSize(pageSize);
                }
            }
        });

        widthTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updatePageSize();
            }
        });

        heightTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updatePageSize();
            }
        });
        unitLink.setAction(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                millimeter = !millimeter;
                refreshUnit(true);
            }
        });
    }

    @Override
    public void validate(ValidationGroup group) {
        //Size
        group.add(widthTextField, Validators.REQUIRE_NON_EMPTY_STRING,
            new PositiveSizeValidator(this));
        group.add(heightTextField, Validators.REQUIRE_NON_EMPTY_STRING,
            new PositiveSizeValidator(this));

        //Margins
        group.add(topMarginTextField, Validators.REQUIRE_NON_EMPTY_STRING,
            Validators.REQUIRE_VALID_NUMBER);
        group.add(bottomMarginTextField, Validators.REQUIRE_NON_EMPTY_STRING,
            Validators.REQUIRE_VALID_NUMBER);
        group.add(leftMarginTextField, Validators.REQUIRE_NON_EMPTY_STRING,
            Validators.REQUIRE_VALID_NUMBER);
        group.add(rightMargintextField, Validators.REQUIRE_NON_EMPTY_STRING,
            Validators.REQUIRE_VALID_NUMBER);
    }

    public void setup(PDFExporter pdfExporter) {
        DefaultComboBoxModel comboBoxModel = (DefaultComboBoxModel) pageSizeCombo.getModel();
        PageSizeItem pageSize = new PageSizeItem(pdfExporter.getPageSize());
        int index = 0;
        if ((index = comboBoxModel.getIndexOf(pageSize)) == -1) {
            comboBoxModel.setSelectedItem(customSizeString);
        } else {
            pageSize = (PageSizeItem) comboBoxModel.getElementAt(index);
            comboBoxModel.setSelectedItem(pageSize);
        }

        setPageSize(pageSize);
        setMargins(pdfExporter.getMarginTop(), pdfExporter.getMarginBottom(), pdfExporter.getMarginLeft(),
            pdfExporter.getMarginRight());
        setOrientation(pdfExporter.isLandscape());
        transparentBackgroundCheckbox.setSelected(pdfExporter.isTransparentBackground());
    }

    public void unsetup(PDFExporter pdfExporter) {
        if (pageSizeCombo.getSelectedItem() == customSizeString) {
            double width = pdfExporter.getPageSize().getWidth();
            double height = pdfExporter.getPageSize().getHeight();
            try {
                width = sizeFormatter.parse(widthTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            try {
                height = sizeFormatter.parse(heightTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }

            if (millimeter) {
                width *= MM;
                height *= MM;
            } else {
                width *= INCH;
                height *= INCH;
            }
            float w = (float) width;
            float h = (float) height;
            PDRectangle rect = new PDRectangle(w, h);
            pdfExporter.setPageSize(rect);
        } else {
            pdfExporter.setPageSize(((PageSizeItem) pageSizeCombo.getSelectedItem()).getPageSize());
        }

        pdfExporter.setLandscape(landscapeRadio.isSelected());
        pdfExporter.setTransparentBackground(transparentBackgroundCheckbox.isSelected());

        double top = pdfExporter.getMarginTop();
        double bottom = pdfExporter.getMarginBottom();
        double left = pdfExporter.getMarginLeft();
        double right = pdfExporter.getMarginRight();
        try {
            top = marginFormatter.parse(topMarginTextField.getText()).doubleValue();
        } catch (ParseException ex) {
        }
        try {
            bottom = marginFormatter.parse(bottomMarginTextField.getText()).doubleValue();
        } catch (ParseException ex) {
        }
        try {
            left = marginFormatter.parse(leftMarginTextField.getText()).doubleValue();
        } catch (ParseException ex) {
        }
        try {
            right = marginFormatter.parse(rightMargintextField.getText()).doubleValue();
        } catch (ParseException ex) {
        }
        if (millimeter) {
            top *= MM;
            bottom *= MM;
            left *= MM;
            right *= MM;
        } else {
            top *= INCH;
            bottom *= INCH;
            left *= INCH;
            right *= INCH;
        }
        pdfExporter.setMarginTop((float) top);
        pdfExporter.setMarginBottom((float) bottom);
        pdfExporter.setMarginLeft((float) left);
        pdfExporter.setMarginRight((float) right);

        savePreferences();
    }

    private void updatePageSize() {
        if (pageSizeCombo.getSelectedItem() != customSizeString && !widthTextField.getText().isEmpty() &&
            !heightTextField.getText().isEmpty()) {
            DefaultComboBoxModel comboBoxModel = (DefaultComboBoxModel) pageSizeCombo.getModel();
            PageSizeItem item = getItem(widthTextField.getText(), heightTextField.getText());
            if (item == null) {
                comboBoxModel.setSelectedItem(customSizeString);
            } else {
                comboBoxModel.setSelectedItem(item);
            }
        }
    }

    private void setPageSize(PageSizeItem pageSize) {
        double pageWidth = 0;
        double pageHeight = 0;
        if (millimeter) {
            pageWidth = pageSize.mmWidth;
            pageHeight = pageSize.mmHeight;
        } else {
            pageWidth = pageSize.inWidth;
            pageHeight = pageSize.inHeight;
        }
        widthTextField.setText(sizeFormatter.format(pageWidth));
        heightTextField.setText(sizeFormatter.format(pageHeight));
    }

    private void setOrientation(boolean landscape) {
        portraitRadio.setSelected(!landscape);
        landscapeRadio.setSelected(landscape);
    }

    private void setMargins(float top, float bottom, float left, float right) {
        if (millimeter) {
            top /= MM;
            bottom /= MM;
            left /= MM;
            right /= MM;
        } else {
            top /= INCH;
            bottom /= INCH;
            left /= INCH;
            right /= INCH;
        }
        topMarginTextField.setText(marginFormatter.format(top));
        bottomMarginTextField.setText(marginFormatter.format(bottom));
        leftMarginTextField.setText(marginFormatter.format(left));
        rightMargintextField.setText(marginFormatter.format(right));
    }

    private PageSizeItem getItem(String width, String height) {
        DefaultComboBoxModel comboBoxModel = (DefaultComboBoxModel) pageSizeCombo.getModel();
        for (int i = 0; i < comboBoxModel.getSize(); i++) {
            Object o = comboBoxModel.getElementAt(i);
            if (o instanceof PageSizeItem) {
                PageSizeItem pageSize = (PageSizeItem) o;
                double pageWidth = 0;
                double pageHeight = 0;
                if (millimeter) {
                    pageWidth = pageSize.mmWidth;
                    pageHeight = pageSize.mmHeight;
                } else {
                    pageWidth = pageSize.inWidth;
                    pageHeight = pageSize.inHeight;
                }
                String wStr = sizeFormatter.format(pageWidth);
                String hStr = sizeFormatter.format(pageHeight);
                if (wStr.equals(width) && hStr.equals(height)) {
                    return ((PageSizeItem) o);
                }
            }
        }
        return null;
    }

    private void refreshUnit(boolean convert) {

        unitLink.setText(
            millimeter ? NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.unitLink.millimeter") :
                NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.unitLink.inch"));
        widthUnitLabel.setText(
            millimeter ? NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelUnit.millimeter") :
                NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelUnit.inch"));
        heightUnitLabel.setText(widthUnitLabel.getText());
        if (convert) {
            if (pageSizeCombo.getSelectedItem() != customSizeString) {
                setPageSize((PageSizeItem) pageSizeCombo.getSelectedItem());
            } else {
                double width = 0;
                double height = 0;
                try {
                    width = sizeFormatter.parse(widthTextField.getText()).doubleValue();
                } catch (ParseException ex) {
                }
                try {
                    height = sizeFormatter.parse(heightTextField.getText()).doubleValue();
                } catch (ParseException ex) {
                }

                if (!millimeter) {
                    width *= MM / INCH;
                    height *= MM / INCH;
                } else {
                    width *= INCH / MM;
                    height *= INCH / MM;
                }
                widthTextField.setText(sizeFormatter.format(width));
                heightTextField.setText(sizeFormatter.format(height));
            }
            updatePageSize();
            double top = 0.;
            double bottom = 0.;
            double left = 0.;
            double right = 0.;
            try {
                top = marginFormatter.parse(topMarginTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            try {
                bottom = marginFormatter.parse(bottomMarginTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            try {
                left = marginFormatter.parse(leftMarginTextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            try {
                right = marginFormatter.parse(rightMargintextField.getText()).doubleValue();
            } catch (ParseException ex) {
            }
            if (!millimeter) {
                top *= MM / INCH;
                bottom *= MM / INCH;
                left *= MM / INCH;
                right *= MM / INCH;
            } else {
                top *= INCH / MM;
                bottom *= INCH / MM;
                left *= INCH / MM;
                right *= INCH / MM;
            }
            topMarginTextField.setText(marginFormatter.format(top));
            bottomMarginTextField.setText(marginFormatter.format(bottom));
            leftMarginTextField.setText(marginFormatter.format(left));
            rightMargintextField.setText(marginFormatter.format(right));
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        orientationButtonGroup = new javax.swing.ButtonGroup();
        labelPageSize = new javax.swing.JLabel();
        pageSizeCombo = new javax.swing.JComboBox();
        labelWidth = new javax.swing.JLabel();
        widthTextField = new javax.swing.JTextField();
        labelHeight = new javax.swing.JLabel();
        heightTextField = new javax.swing.JTextField();
        widthUnitLabel = new javax.swing.JLabel();
        heightUnitLabel = new javax.swing.JLabel();
        labelOrientation = new javax.swing.JLabel();
        portraitRadio = new javax.swing.JRadioButton();
        landscapeRadio = new javax.swing.JRadioButton();
        labelMargins = new javax.swing.JLabel();
        labelTop = new javax.swing.JLabel();
        topMarginTextField = new javax.swing.JTextField();
        labelBottom = new javax.swing.JLabel();
        bottomMarginTextField = new javax.swing.JTextField();
        labelLeft = new javax.swing.JLabel();
        labelRight = new javax.swing.JLabel();
        leftMarginTextField = new javax.swing.JTextField();
        rightMargintextField = new javax.swing.JTextField();
        labelUnit = new javax.swing.JLabel();
        unitLink = new org.jdesktop.swingx.JXHyperlink();
        transparentBackgroundCheckbox = new javax.swing.JCheckBox();
        labelBackground = new javax.swing.JLabel();

        labelPageSize.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelPageSize.text")); // NOI18N

        pageSizeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        labelWidth.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelWidth.text")); // NOI18N

        widthTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        widthTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.widthTextField.text")); // NOI18N

        labelHeight.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelHeight.text")); // NOI18N

        heightTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        heightTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.heightTextField.text")); // NOI18N

        widthUnitLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.widthUnitLabel.text")); // NOI18N

        heightUnitLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.heightUnitLabel.text")); // NOI18N

        labelOrientation.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelOrientation.text")); // NOI18N

        orientationButtonGroup.add(portraitRadio);
        portraitRadio.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.portraitRadio.text")); // NOI18N

        orientationButtonGroup.add(landscapeRadio);
        landscapeRadio.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.landscapeRadio.text")); // NOI18N

        labelMargins.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelMargins.text")); // NOI18N

        labelTop.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelTop.text")); // NOI18N

        topMarginTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.topMarginTextField.text")); // NOI18N

        labelBottom.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelBottom.text")); // NOI18N

        bottomMarginTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.bottomMarginTextField.text")); // NOI18N

        labelLeft.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelLeft.text")); // NOI18N

        labelRight.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelRight.text")); // NOI18N

        leftMarginTextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.leftMarginTextField.text")); // NOI18N

        rightMargintextField.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.rightMargintextField.text")); // NOI18N

        labelUnit.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelUnit.text")); // NOI18N

        unitLink.setText(""); // NOI18N
        unitLink.setToolTipText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.unitLink.toolTipText")); // NOI18N
        unitLink.setFocusPainted(false);

        transparentBackgroundCheckbox.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.transparentBackgroundCheckbox.text")); // NOI18N

        labelBackground.setText(org.openide.util.NbBundle.getMessage(UIExporterPDFPanel.class, "UIExporterPDFPanel.labelBackground.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelPageSize)
                            .addComponent(labelOrientation)
                            .addComponent(labelMargins)
                            .addComponent(labelBackground))
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pageSizeCombo, 0, 217, Short.MAX_VALUE)
                            .addComponent(transparentBackgroundCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(landscapeRadio, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(portraitRadio, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(labelHeight)
                                                    .addComponent(labelWidth))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(heightTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                        .addGap(10, 10, 10)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(widthUnitLabel)
                                            .addComponent(heightUnitLabel)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(labelTop)
                                                .addGap(26, 26, 26)
                                                .addComponent(topMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(labelLeft))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(labelBottom)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(bottomMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(labelRight)))
                                        .addGap(21, 21, 21)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(leftMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(rightMargintextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelUnit)
                        .addGap(62, 62, 62)
                        .addComponent(unitLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelUnit)
                    .addComponent(unitLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPageSize)
                    .addComponent(pageSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(widthUnitLabel)
                    .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelWidth))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(heightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightUnitLabel)
                    .addComponent(labelHeight))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelOrientation)
                    .addComponent(portraitRadio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(landscapeRadio)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelMargins)
                    .addComponent(labelTop)
                    .addComponent(topMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelLeft)
                    .addComponent(leftMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bottomMarginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelBottom)
                    .addComponent(labelRight)
                    .addComponent(rightMargintextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(transparentBackgroundCheckbox)
                    .addComponent(labelBackground))
                .addContainerGap(19, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private static class PageSizeItem {

        private final PDRectangle pageSize;
        private final double inWidth;
        private final double inHeight;
        private final double mmWidth;
        private final double mmHeight;
        private String name = "";

        public PageSizeItem(PDRectangle pageSize) {
            this.pageSize = pageSize;
            this.inHeight = pageSize.getHeight() / INCH;
            this.inWidth = pageSize.getWidth() / INCH;
            this.mmHeight = pageSize.getHeight() / MM;
            this.mmWidth = pageSize.getWidth() / MM;
        }

        public PageSizeItem(PDRectangle pageSize, String name, double mmWidth, double mmHeight, double inWidth,
                            double inHeight) {
            this.pageSize = pageSize;
            this.name = name;
            this.inHeight = inHeight;
            this.inWidth = inWidth;
            this.mmHeight = mmHeight;
            this.mmWidth = mmWidth;
        }

        public PDRectangle getPageSize() {
            return pageSize;
        }

        public double getInHeight() {
            return inHeight;
        }

        public double getInWidth() {
            return inWidth;
        }

        public double getMmHeight() {
            return mmHeight;
        }

        public double getMmWidth() {
            return mmWidth;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PageSizeItem other = (PageSizeItem) obj;
            return this.pageSize == other.pageSize || (this.pageSize != null && this.pageSize.equals(other.pageSize));
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + (this.pageSize != null ? this.pageSize.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class PositiveSizeValidator implements Validator<String> {

        private final UIExporterPDFPanel panel;

        public PositiveSizeValidator(UIExporterPDFPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            boolean result = false;
            try {
                double i = panel.sizeFormatter.parse(panel.widthTextField.getText()).doubleValue();
                result = i > 0;
            } catch (ParseException ex) {
            }
            if (!result) {
                String message = NbBundle.getMessage(getClass(),
                    "PositiveSizeValidator.NEGATIVE", model);
                problems.add(message);
            }
            return result;
        }
    }
}
