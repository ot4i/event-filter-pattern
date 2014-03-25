/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and other Contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - initial implementation
 *******************************************************************************/

package com.ibm.etools.mft.pattern.messaging.eventfilter.code;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Composite;

import com.ibm.broker.config.appdev.patterns.ui.BasePatternPropertyEditor;
import com.ibm.broker.config.appdev.patterns.ui.PatternPropertyEditorSite;
import com.ibm.etools.mft.ibmnodes.editors.xpath.XPathPropertyEditor;

public class XPathEditor extends BasePatternPropertyEditor implements Observer {

	private XPathPropertyEditor xpathPropertyEditor = new XPathPropertyEditor();
	private String value;
	
	@Override
	public void configureEditor(PatternPropertyEditorSite site, boolean required, String configurationValues) {
		
		super.configureEditor(site, required, configurationValues);
	}

	@Override
	public void createControls(Object parent) {
		
		Composite composite = (Composite) parent;	
		xpathPropertyEditor.createControls(composite);		
		xpathPropertyEditor.setArgument("Root,Body,Properties,LocalEnvironment,DestinationList,ExceptionList,Environment");
		xpathPropertyEditor.setCurrentValue("contains($Body/Data/event/type,\"alert\")");
		xpathPropertyEditor.addObserver(this);
	}

	@Override
	public void setValue(String value) {

		if (value != null && !value.startsWith("<"))
		{			
			this.value = value;
		}		
	}

	@Override
	public String getValue() {
		
		return value;		
	}

	@Override
	public void setEnabled(boolean enabled) {
		
		xpathPropertyEditor.setEnabled(enabled);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		
		if (arg0 == xpathPropertyEditor)
		{
			value = (String)xpathPropertyEditor.getValue();
			getSite().valueChanged();
		}
		
	}	
}
