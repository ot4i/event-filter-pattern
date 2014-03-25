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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Vector;

import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.broker.config.appdev.StickyNote;
import com.ibm.broker.config.appdev.nodes.FileOutputNode;
import com.ibm.broker.config.appdev.nodes.JMSInputNode;
import com.ibm.broker.config.appdev.nodes.RouteNode;
import com.ibm.broker.config.appdev.patterns.GeneratePatternInstanceTransform;
import com.ibm.broker.config.appdev.patterns.PatternInstanceManager;

public class EventFilterGenerator implements GeneratePatternInstanceTransform {

	private static final String MSGFLOW = "eventFilter.msgflow";
	private static final String FLOW_PROJECT = "EventFilter";
	private static final String LOG_PROJECT = FLOW_PROJECT+"_logs";
	private static final String CLIENT_PROJECT = FLOW_PROJECT+"_client";
	private static final String ROUTE_NODE = "Route";
	private static final String JMSINPUT_NODE = "EventSubscriber";
	private static final String MATCH_TERMINAL = "Match";
	private static final String DOT_BINDINGS_FILENAME = ".bindings";
	private static final String CLIENT_FILENAME = "PacemakerClient.html";
	private static final String TMP_CLIENT_FILENAME = "PacemakerClient.html.tmp";
	private static final String WORKSPACE_SUB = "$WORKSPACE";
	private static final String PATTERNNAME_SUB = "$PATTERNNAME";
	
	@Override
	public void onGeneratePatternInstance(PatternInstanceManager patternInstanceManager) {
		
			setXPathOnRouteNode(patternInstanceManager);			
			
			try {
				generateBindings(patternInstanceManager);
			} catch (IOException e) {
				
				//e.printStackTrace();
			}
			
			try {
				setBindingsOnJMSInputNode(patternInstanceManager);
			} catch (MalformedURLException e) {
				
				//e.printStackTrace();
			}

			try {
				updateClientHTML(patternInstanceManager);
			} catch (IOException e) {
				
				//e.printStackTrace();
			}
			
			try {
				updateLogFolder(patternInstanceManager);
			} catch (IOException e) {
				
				//e.printStackTrace();
			}
			
			generateIMACommands(patternInstanceManager);
			
	}
	
	private void setXPathOnRouteNode(PatternInstanceManager patternInstanceManager) {
		
		MessageFlow msgFlow = patternInstanceManager.getMessageFlow(FLOW_PROJECT, MSGFLOW);
		RouteNode routeNode = (RouteNode)msgFlow.getNodeByName(ROUTE_NODE);
		
		RouteNode.FilterTable table = routeNode.getFilterTable();
		Vector<RouteNode.FilterTableRow> rows = table.getRows();
		
		RouteNode.FilterTableRow row = null;
		
		if (rows.size() == 0)
		{
			row = table.createRow();
			rows.add(row);
			
		} else {
			
			row = rows.get(0);
		}
		
		row.setFilterPattern(patternInstanceManager.getParameterValue("pp8"));
		row.setRoutingOutputTerminal(MATCH_TERMINAL);		
	}

	private void generateBindings(PatternInstanceManager patternInstanceManager) throws IOException {

		String port = patternInstanceManager.getParameterValue("pp7");
		String server = patternInstanceManager.getParameterValue("pp5");
		String topic = patternInstanceManager.getParameterValue("pp11");
				
		String projectLocation = patternInstanceManager.getWorkspaceLocation()+"/"+patternInstanceManager.getPatternInstanceName()+"_"+FLOW_PROJECT;
		
		File dotBindings = new File(projectLocation+"/"+DOT_BINDINGS_FILENAME);
		dotBindings.delete();

		dotBindings.createNewFile();
		PrintWriter pw = new PrintWriter(new FileWriter(dotBindings));
		
		pw.println("connFactory1/ClassName=com.ibm.ima.jms.impl.ImaConnectionFactory");
		pw.println("connFactory1/FactoryName=com.ibm.ima.jms.impl.ImaConnectionFactory");
		pw.println("connFactory1/RefAddr/0/Encoding=String");
		pw.println("connFactory1/RefAddr/0/Type=Port");
		pw.println("connFactory1/RefAddr/0/Content="+port);
		pw.println("connFactory1/RefAddr/1/Encoding=String");
		pw.println("connFactory1/RefAddr/1/Type=Server");
		pw.println("connFactory1/RefAddr/1/Content="+server);
		pw.println("connFactory1/RefAddr/2/Encoding=String");
		pw.println("connFactory1/RefAddr/2/Content=common");
		pw.println("connFactory1/RefAddr/2/Type=ObjectType");
		pw.println("EventTopic/ClassName=com.ibm.ima.jms.impl.ImaTopic");
		pw.println("EventTopic/FactoryName=com.ibm.ima.jms.impl.ImaTopic");
		pw.println("EventTopic/RefAddr/0/Encoding=String");
		pw.println("EventTopic/RefAddr/0/Type=Name");
		pw.println("EventTopic/RefAddr/0/Content="+topic.replace("#", "\\#"));
		pw.flush();
		pw.close();		
	}
		
	
	private void setBindingsOnJMSInputNode(PatternInstanceManager patternInstanceManager) throws MalformedURLException {
	
		String projectLocation = patternInstanceManager.getWorkspaceLocation()+"/"+patternInstanceManager.getPatternInstanceName()+"_"+FLOW_PROJECT;
		File dotBindings = new File(projectLocation+"/"+DOT_BINDINGS_FILENAME);
		String jndiBindingFile = dotBindings.toURI().toURL().toString();
		String jndiBindingLocation = jndiBindingFile.substring(0, jndiBindingFile.lastIndexOf("/"));
		
		MessageFlow msgFlow = patternInstanceManager.getMessageFlow(FLOW_PROJECT, MSGFLOW);
		JMSInputNode routeNode = (JMSInputNode)msgFlow.getNodeByName(JMSINPUT_NODE);
		routeNode.setLocationJndiBindings(jndiBindingLocation);
	}
	
	
	private void updateClientHTML(PatternInstanceManager patternInstanceManager) throws IOException {
		
		String port = patternInstanceManager.getParameterValue("pp7");
		String server = patternInstanceManager.getParameterValue("pp5");
		String topic = patternInstanceManager.getParameterValue("pp11");
		
		String projectLocation = patternInstanceManager.getWorkspaceLocation()+"/"+patternInstanceManager.getPatternInstanceName()+"_"+CLIENT_PROJECT;
		File client = new File(projectLocation+"/"+CLIENT_FILENAME);
		File tmpClient = new File(projectLocation+"/"+TMP_CLIENT_FILENAME);
		
		BufferedReader reader = new BufferedReader(new FileReader(client));
	    PrintWriter writer = new PrintWriter(new FileWriter(tmpClient));
	    
	    String line = reader.readLine();
	    
	    while (line != null){
	    	
	    	if (line.indexOf("127.0.0.1") != -1)
	    		writer.println(line.replace("127.0.0.1", server));
	    	else if (line.indexOf("8080") != -1)
	    		writer.println(line.replace("8080", port));
	    	else if (line.indexOf("/PACEMAKER/") != -1) {
	    		//If the user defined topic contains a # or + then replace it with generated value
	    		String topicDef = topic.replace("+", "\"+form.clientId.value+\"");
	    		topicDef = topicDef.replace("#", "\"+form.clientId.value+\"");	    		
	    		topicDef = "form.topicName.value = \""+topicDef+"\";";
	    		writer.println(topicDef);
	    	} else
	    		writer.println(line);
	    	
	    	line = reader.readLine();
	    }
	    
	    reader.close();
	    writer.flush();
	    writer.close();
	    
	    client.delete();
	    tmpClient.renameTo(client);
	}

	private void updateLogFolder(PatternInstanceManager patternInstanceManager) throws IOException {
		
		//String folder = patternInstanceManager.getParameterValue("pp1");
		
		//replace $WORKSPACE with the workspace directory
		//folder = folder.replace(WORKSPACE_SUB, patternInstanceManager.getWorkspaceLocation());
		
		//replace $PATTERNNAME with the pattern name
		//folder = folder.replace(PATTERNNAME_SUB, patternInstanceManager.getPatternInstanceName());
			
		String folder = patternInstanceManager.getWorkspaceLocation()+"/"+patternInstanceManager.getPatternInstanceName()+"_"+LOG_PROJECT;
		File outputProject = new File(folder);
		
		MessageFlow msgFlow = patternInstanceManager.getMessageFlow(FLOW_PROJECT, MSGFLOW);
		FileOutputNode fon = (FileOutputNode)msgFlow.getNodeByName("Log events");			
		fon.setOutputDirectory(outputProject.getCanonicalPath());
	}
	
	private void generateIMACommands(PatternInstanceManager patternInstanceManager) {
		
		String port = patternInstanceManager.getParameterValue("pp7");
		String server = patternInstanceManager.getParameterValue("pp5");
		
		MessageFlow msgFlow = patternInstanceManager.getMessageFlow(FLOW_PROJECT, MSGFLOW);
		Vector<StickyNote> notes = msgFlow.getStickyNotes();
		
		if (notes.size() > 0)
		{
			//Build the IMA commands to run
			StringBuffer sb = new StringBuffer();
			sb.append("*** Run the following command to configure the JMSProvider for IBM MessageSight, then restart the broker ***\n\n");
			sb.append("mqsichangeproperties <broker_name> -c JMSProviders -o IBM_MessageSight -n jarsURL -v <ima_client_jars_url>\n\n\n");
			sb.append("*** Run the following commands on IBM MessageSight to configure the required endpoint ***\n\n");
			sb.append("imaserver create MessageHub \"Name=brokerPatternHub\" \"Description=A hub for use with the broker pattern\"\n\n");
			sb.append("imaserver create ConnectionPolicy \"Name=brokerPatternConnectionPolicy\" \"Protocol=JMS,MQTT\"\n\n");
			sb.append("imaserver create MessagingPolicy \"Name=brokerPatternMsgPolicy\" \"DestinationType=Topic\" \"Destination=*\" \"ActionList=Publish,Subscribe\" \"Protocol=MQTT,JMS\"\n\n");
			sb.append("imaserver create Endpoint \"Name=brokerPatternEndpoint\" \"Port="+port+"\" \"Protocol=JMS,MQTT\" \"Interface="+server+"\" \"ConnectionPolicies=brokerPatternConnectionPolicy\" \"MessagingPolicies=brokerPatternMsgPolicy\" \"MaxMessageSize=1KB\" \"MessageHub=brokerPatternHub\" \"Enabled=True\"");
			
			StickyNote note = notes.get(0);
			note.setNoteText(sb.toString());
		}
	}
}
