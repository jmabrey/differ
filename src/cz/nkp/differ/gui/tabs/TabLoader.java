package cz.nkp.differ.gui.tabs;

import java.io.IOException;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This is a static stub class that allows the rest of the application to access the package-local
 * html files stored inside the <code>cz.nkp.differ.gui.tabs</code> package. These html files hold
 * the html stubs that outline the <b>static</b> content of the applications static pages. Do note
 * that it is not possible to insert AJAX or other dynamic content into these files successfully
 * without modification. (Dynamic taken to mean any content the browser would have to break out of 
 * our domain root to access, as this is disallowed under most browser's security modules) 
 * 
 * TODO: Make it possible to override the default content with external non-jar content
 * @author Joshua Mabrey
 * Mar 30, 2012
 */
@SuppressWarnings("serial")
public class TabLoader extends VerticalLayout{
	
	/**
	 * Creates a TabLoader by accessing the file stored within the <code>cz.nkp.differ.gui.tabs</code> package in the form
	 * of "*_tab.html", where * is replaced with the String argument passed to the Constructor. No file path sanitation is
	 * needed, as the JVM shouldn't be able to execute anything from within the package, and it is not possible to break out of
	 * the classloader's current inner path to access arbitrary server data.
	 * @param resource
	 * @throws IOException if the file is not present or readable from the package.
	 */
	public TabLoader(String resource) throws IOException{
		super();//Create this as a VerticalLayout
		String content = new java.util.Scanner(TabLoader.class.getResourceAsStream(resource)).useDelimiter("\\A").next();
		//Special "\\A" reads the whole stream as a single token, easiest way to one line read a file/resource
		this.addComponent(new Label(content,Label.CONTENT_XHTML));//Adds the XHTML content of the file to a label added to this VerticalLayout
	}
}
