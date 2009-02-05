/*******************************************************************************
 * Copyright (c) 2009 Casey Marshall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshall - initial API and implementation
 *    Laurent Petit - maintenance and evolution
 *******************************************************************************/
package clojuredev.launching;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

public class ClojureTabGroup extends AbstractLaunchConfigurationTabGroup {

    /**
     * @see ILaunchConfigurationTabGroup#createTabs(ILaunchConfigurationDialog,
     *      String)
     */
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
//                 new JavaMainTab(),
                new ClojureMainTab(), 
                new JavaArgumentsTab(), 
                new JavaJRETab(),
                new JavaClasspathTab(),
                // new SourceLookupTab(),
                new EnvironmentTab(), 
                new CommonTab() 
        };
        setTabs(tabs);
    }
}
