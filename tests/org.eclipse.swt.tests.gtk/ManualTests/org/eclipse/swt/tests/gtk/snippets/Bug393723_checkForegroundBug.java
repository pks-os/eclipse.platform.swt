/*******************************************************************************
 * Copyright (c) 2018 Red Hat and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tests.gtk.snippets;


import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class Bug393723_checkForegroundBug {
	
public static void main(String[] args) {
    final Display display = new Display();
    Shell shell = new Shell(display);
    shell.setBounds(10, 10, 200, 200);

    Composite comp = new Composite(shell, SWT.NONE);
    System.out.println("shell back=" + shell.getBackground());
    System.out.println("shell fore=" + shell.getForeground());
    System.out.println("back=" + comp.getBackground());
    System.out.println("fore=" + comp.getForeground());
    shell.setForeground(new Color(display, 240, 240, 240));
    comp.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
    System.out.println("after");
    System.out.println("shell back=" + shell.getBackground());
    System.out.println("shell fore=" + shell.getForeground());
    System.out.println("back=" + comp.getBackground());
    System.out.println("fore=" + comp.getForeground());
    
    shell.open();
    while (!shell.isDisposed()) {
        if (!display.readAndDispatch()) display.sleep();
    }
    display.dispose();
}

}