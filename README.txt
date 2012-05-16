Developing preservation processes for a trusted digital repository requires the utilization of new methods and technologies, which have helped to accelerate the whole process of control. The current approach at the Digital Preservation Standards Department at The National Library of the Czech Republic is to develop a quality control application for still image file formats capable of performing identification, characterization, validation and visual/mathematical comparison integrated into an operational digital preservation framework. The online application DIFFER is utilizing existing tools (JHOVE, FITS, ExifTool, KDU_expand, DJVUDUMP, Jpylyzer, etc.), which are mainly used separately across a whole spectrum of existing projects. This open source application comes with a well-structured and uniform GUI, which helps the user to understand the relationships between various file format properties, detect visual and non-visual errors and simplifies decision-making. An additional feature called compliance-check is designed to help us check the required specifications of the JPEG2000 file format.

Compilation Information:
 
 You must have Maven installed.
 
To package the Differ application into a .war simply instruct Maven to target the package goal
 
	mvn package
	
To compile the Differ application without packaging

	mvn compile
	
To update the widgetsets

	mvn vaadin:update-widgetset
	
To compile as needed and run on a local Jetty server
	
	mvn jetty:run
	
To stop that server once it is started

	mvn jetty:stop
	
(Alternatively simply pass a terminator character to the console you initiated jetty:run on, such as Ctrl-C or Ctrl-Z)

If you get bind errors when running the Jetty server make sure that no other application is bound to port 8080.