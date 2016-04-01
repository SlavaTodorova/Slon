package tools;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Class for the translation project
 * 
 * @author Slava Todorova
 */
public class Project {

	private Path path; // the project folder
	
	public File sourceFile;
	public File targetFile;
	public File translationFile;
	
	public Project (Path path, File sourceFile) {
		this.path = path;
		this.sourceFile = sourceFile;
		this.targetFile = new File(path.toFile(), "target.txt");
		this.translationFile = new File(path.toFile(), "translation.slon");
	}
	
	public Project (Path path) {
		this.path = path;
		this.sourceFile = new File(path.toFile(), "source.txt");
		this.targetFile = new File(path.toFile(), "target.txt");
		this.translationFile = new File(path.toFile(), "translation.slon");
	}
	
	public Project () {
		this.path = null;
		this.sourceFile = null;
		this.targetFile = null;
		this.translationFile = null;
	}
}
