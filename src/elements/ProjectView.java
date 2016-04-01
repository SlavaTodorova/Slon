/*
 * From https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/uiswing/examples/components/FileChooserDemo2Project/src/components/ImageFileView.java
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
package elements;
 
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.*;
 
/* ImageFileView.java is used by FileChooserDemo2.java. */
public class ProjectView extends FileView {
    Icon projectIcon = UIManager.getIcon("FileChooser.fileIcon");
    Icon folderIcon = UIManager.getIcon("FileChooser.directoryIcon");
 
    public String getName(File f) {
        return null; //let the L&F FileView figure this out
    }
 
    public String getDescription(File f) {
        return null; //let the L&F FileView figure this out
    }
 
    public Boolean isTraversable(File f) {
        return null; //let the L&F FileView figure this out
    }
 
    public String getTypeDescription(File f) {
       if (isProject(f)) {
        	return "Slon Project";
        }
        return "Directory";
    }
 
    public Icon getIcon(File f) {
    	if (isProject(f)) {
        	return projectIcon;
        }
        return folderIcon;
    }
    
	private boolean isProject(File dir) {
		File[] files = dir.listFiles();
		boolean hasSource = false;
		boolean hasTarget = false;
		boolean hasSlon = false;
		for (int i=0; i<files.length; i++) {
			String fileName = files[i].getName();
			switch(fileName) {
			case "source.txt":	hasSource = true;
			break;
			case "target.txt":	hasTarget = true;
			break;
			case "translation.slon":	hasSlon = true;
			break;
			default: break;
			}
		}
		return hasSource && hasTarget && hasSlon;
	}
}
