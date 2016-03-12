package tools;

import java.io.File;
import java.util.List;

/**
 * Class for the translation project
 * 
 * @author Slava Todorova
 */
public class Project {

	private String location; // the project folder
	
	public File sourceFiles;
	public File targetFiles;
	public File translationFile;
	
	public Project (String location) {
		this.location = location;
	}
}
