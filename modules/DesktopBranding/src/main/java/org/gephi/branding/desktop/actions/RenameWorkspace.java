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

package org.gephi.branding.desktop.actions;

import java.awt.event.ActionEvent;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.WorkspaceInformation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * @author Mathieu Bastian
 */
public class RenameWorkspace extends SystemAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = "";
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        name = pc.getCurrentWorkspace().getLookup().lookup(WorkspaceInformation.class).getName();
        DialogDescriptor.InputLine dd = new DialogDescriptor.InputLine("",
            NbBundle.getMessage(RenameWorkspace.class, "RenameWorkspace.dialog.title"));
        dd.setInputText(name);
        if (DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.OK_OPTION) &&
            !dd.getInputText().isEmpty()) {
            Lookup.getDefault().lookup(ProjectControllerUI.class).renameWorkspace(dd.getInputText());
        }
    }

    @Override
    public boolean isEnabled() {
        return Lookup.getDefault().lookup(ProjectControllerUI.class).canRenameWorkspace();
    }

    @Override
    protected String iconResource() {
        return "DesktopBranding/renameWorkspace.png";
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RenameWorkspace.class, "CTL_RenameWorkspace");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
