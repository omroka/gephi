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

package org.gephi.ui.filters.plugin.attribute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.filters.plugin.attribute.AttributeEqualBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.Exceptions;

/**
 * @author Mathieu Bastian
 */
public class EqualStringPanel extends javax.swing.JPanel implements ActionListener {

    private AttributeEqualBuilder.EqualStringFilter filter;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelPattern;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox regexCheckbox;
    private javax.swing.JTextField textField;
    // End of variables declaration//GEN-END:variables

    public EqualStringPanel() {
        initComponents();

        okButton.addActionListener(this);
    }

    public static ValidationPanel createValidationPanel(final EqualStringPanel innerPanel) {
        final ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();
        validationPanel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                innerPanel.okButton.setEnabled(!validationPanel.isProblem());
            }
        });
        //Node field
        group.add(innerPanel.textField, new RegexValidator(innerPanel));

        return validationPanel;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        FilterProperty pattern = filter.getProperties()[1];
        FilterProperty useRegex = filter.getProperties()[2];
        try {
            if (pattern.getValue() == null || !pattern.getValue().equals(textField.getText())) {
                pattern.setValue(textField.getText());
            }
            if (useRegex.getValue() == null || !useRegex.getValue().equals(regexCheckbox.isSelected())) {
                useRegex.setValue(regexCheckbox.isSelected());
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    public void setup(AttributeEqualBuilder.EqualStringFilter filter) {
        this.filter = filter;
        this.setToolTipText(filter.getName() + " '" + filter.getColumn().getTitle() + "'");
        FilterProperty pattern = filter.getProperties()[1];
        FilterProperty useRegex = filter.getProperties()[2];
        try {
            textField.setText((String) pattern.getValue());
            regexCheckbox.setSelected((Boolean) useRegex.getValue());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
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
        java.awt.GridBagConstraints gridBagConstraints;

        labelPattern = new javax.swing.JLabel();
        textField = new javax.swing.JTextField();
        regexCheckbox = new javax.swing.JCheckBox();
        okButton = new javax.swing.JButton();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        labelPattern.setText(org.openide.util.NbBundle
            .getMessage(EqualStringPanel.class, "EqualStringPanel.labelPattern.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        add(labelPattern, gridBagConstraints);

        textField.setText(
            org.openide.util.NbBundle.getMessage(EqualStringPanel.class, "EqualStringPanel.textField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(textField, gridBagConstraints);

        regexCheckbox.setText(org.openide.util.NbBundle
            .getMessage(EqualStringPanel.class, "EqualStringPanel.regexCheckbox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(regexCheckbox, gridBagConstraints);

        okButton.setText(
            org.openide.util.NbBundle.getMessage(EqualStringPanel.class, "EqualStringPanel.okButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        add(okButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private static class RegexValidator implements Validator<String> {

        private final EqualStringPanel panel;

        public RegexValidator(EqualStringPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            boolean result = true;
            if (panel.regexCheckbox.isSelected()) {
                try {
                    Pattern p = Pattern.compile(model);
                } catch (Exception e) {
                    result = false;
                }
                if (!result) {
                    String message = "Invalid regex";
                    problems.add(message);
                }
            }
            return result;
        }
    }
}
