package cz.nkp.differ.gui.windows;

import java.io.IOException;

import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cz.nkp.differ.gui.components.ProjectHeaderPanel;
import cz.nkp.differ.gui.tabs.DifferProgramTab;
import cz.nkp.differ.gui.tabs.TabLoader;
import cz.nkp.differ.util.GeneralHelperFunctions;

/**
 * 
 * @author Joshua Mabrey
 * Mar 30, 2012
 */
@SuppressWarnings("serial")
public class MainDifferWindow extends Window{
		 
	public MainDifferWindow() {
		super("NDK Image Data Validator");//Sets the title of the application
		
		menuTabs = new TabSheet();
		
		/*
		 * Adding the dynamic content tabs
		 */
		MainDifferWindow.createDynamicContentTab(new DifferProgramTab(),"DIFFER App", menuTabs);
		/*
		 * Adding the static content tabs
		 */
		MainDifferWindow.createStaticContentTab("about_tab.html","About", menuTabs);
		MainDifferWindow.createStaticContentTab("doc_tab.html","Documents", menuTabs);
		MainDifferWindow.createStaticContentTab("faq_tab.html","FAQ", menuTabs);
		MainDifferWindow.createStaticContentTab("tos_tab.html","TOS", menuTabs);
		MainDifferWindow.createStaticContentTab("help_tab.html","Help", menuTabs);
		
		/*
		 * Add the actual completed UI components to the root
		 */
		addComponent(new ProjectHeaderPanel());//Component that represents the top-page header
		addComponent(menuTabs);//The application view tabs
	}
	
	/**
	 * Loads a Static tab with {@link #cz.nkp.differ.gui.tabs.TabLoader} and adds it to the 
	 * given <code>TabSheet</code>
	 * @param source String identifying the proper load resource as recognized by <code>TabLoader</code>
	 * @param caption String that the tab should have as its name in the TabSheet
	 * @param parent TabSheet to add the new tab to
	 */
	private static void createStaticContentTab(String source, String caption,TabSheet parent){
		
		GeneralHelperFunctions.errorIfContainsNull(source,caption,parent);//Check for null arguments
		
		try {
		    VerticalLayout tab = new TabLoader(source);
			tab.setMargin(true);
			tab.setCaption(caption);
			parent.addTab(tab);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Loads a dynamic tab as a <code>Layout</code> which is then added to the given TabSheet
	 * given <code>TabSheet</code>
	 * @param source Layout to add to the tab, the Layout is responsible for implementing any and interactions by itself
	 * @param caption String that the tab should have as its name in the TabSheet
	 * @param parent TabSheet to add the new tab to
	 */
	private static void createDynamicContentTab(Layout source, String caption,TabSheet parent){
		
		GeneralHelperFunctions.errorIfContainsNull(source,caption,parent);//Check for null arguments
				
		source.setCaption(caption);
		source.setMargin(true);
		parent.addTab(source);
	}
	
	private TabSheet menuTabs;
}
