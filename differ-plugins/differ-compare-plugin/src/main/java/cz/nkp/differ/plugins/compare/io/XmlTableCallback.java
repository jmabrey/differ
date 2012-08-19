package cz.nkp.differ.plugins.compare.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

import cz.nkp.differ.plugins.tools.CommandHelper.CommandMessageCallback;

public class XmlTableCallback extends CommandMessageCallback{
	

	protected List<XmlTableEntry> entries = null; 
	protected Table xmlTable;
	private static Logger LOGGER = Logger.getRootLogger();
	private String[] tagNames;
	
	//Parent Constructor
	public XmlTableCallback(Logger logger,String[] tagNames){
		LOGGER = logger;
		this.tagNames = tagNames;
		entries = Collections.synchronizedList(new ArrayList<XmlTableEntry>());
		setupTable();
	}
	
	public XmlTableCallback(Logger logger,String[] tagNames,XmlTableCallback child){
		LOGGER = logger;
		this.tagNames = tagNames;
		entries = child.entries;
		xmlTable = child.xmlTable;
	}
	
	private void setupTable(){
		xmlTable = new Table("Metadata");
		//Table setup
		xmlTable.addContainerProperty("Source", String.class,"Null");
		xmlTable.addContainerProperty("Property", String.class,"Null");
		xmlTable.addContainerProperty("Value", String.class,"Null");
		xmlTable.setImmediate(true);
		xmlTable.setPageLength(3);
		xmlTable.setWidth(300f, Component.UNITS_PIXELS);
	}
	
	private final void addTagToList(String sourceName, Document doc, int tagPosition,String tagName){				
			Element tag = (Element) doc.getElementsByTagName(tagName).item(tagPosition);
			
			String tagText = "Unable to find value";
			
			if(tag != null){
				tagText = tag.getTextContent();
			}
			
			entries.add(new XmlTableEntry(sourceName,tagName,tagText));
	}
	
	public Table getTable(){
		return xmlTable;
	}
	
	private void modifyTable(){
		xmlTable.removeAllItems();
		long id = 0;
		for(XmlTableEntry entry : entries.toArray(new XmlTableEntry[0])){
			xmlTable.addItem(new Object[]{entry.sourceName,entry.tagName,entry.tagValue}, id++);
		}
	}
	
	@Override
	public void messageGenerated(String source,String message) {
			LOGGER.trace(message);
			
			//Load XML from message
			Document doc = null;
			
			try{
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				doc = docBuilder.parse(new ByteArrayInputStream(message.getBytes("UTF-8")));
				doc.normalizeDocument();
			} catch (SAXException e) {
				LOGGER.error(e);
			} catch (IOException e) {
				LOGGER.error(e);
			} catch (ParserConfigurationException e) {
				LOGGER.error(e);
			}
			
			if(doc == null){
				LOGGER.error("XML Document was null!");
				return;
			}
			
			for(String tagName: tagNames){
				addTagToList(source,doc,0,tagName);
			}
			
			modifyTable();			
	}	
};

class XmlTableEntry{
	
	public XmlTableEntry(String sourceName,String tagName,String tagValue){
		this.sourceName = sourceName;
		this.tagName = tagName;
		this.tagValue = tagValue;
	}
	
	String sourceName,tagName,tagValue;
}
