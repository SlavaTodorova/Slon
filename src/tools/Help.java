package tools;
/**
 * This class manages the help information
 * 
 * @author Slava Todorova
 *
 */
public class Help {
	
	public String about = "<html><p>SLON: Very Good Translation Editor, Version 16.04</p>"
			+ "<p>Release on: April 1, 2016</p><br/>"
			+ "<p>Author: Slava Todorova</p>"
			+ "<p>E-mail: todorova.slava@gmail.com</p><br/></html>";
	
	public String startNew = "<html><br/>"
			+ "<p>To start a translation, you need to create a project.</p>"
			+ "<p>Use the New Project button at the top right corner"
			+ "or go to Project&rarr;New</p><br/>"
			+ "<p>Type your translation in the second, central column.</p><br/>"
			+ "<p>After typing a translation, the segment (the table cell) "
			+ "should be closed in order for the changes to take effect. "
			+ "Use Enter for segment closing.</p><br/>"
			+ "<p>To start editing the next segment, hit Enter again. "
			+ "Alternatively you can navigate up and down with your keyboard's "
			+ "Up and Down keys. "
			+ "Or use the mouse with a single click on the segment "
			+ "that you want to edit.</p><br/>"
			+ "<p>If you need to add comments, use the third column."
			+ "The coments won't appear in the final translation.</p></html>";
	
	public String resumeOld = "<html>To resume your translation you have to open "
			+ "an already existing project.<br/>.<br/>"
			+ "Use the Open Project button "
			+ "in the upper right corner or go to Project&rarr;Open.</html>";
	
	public String save = "<html>To save your translation, hit the save buton.<br/>.<br/>"
			+ "The target file, containing only the translation, will be named"
			+ "\"target.txt\" and will be directly in the project folder.<br/>.<br/>"
			+ "Caution! There is no autosave in this version of the editor!</html>";
	
	public String license = "<html><a rel=\"license\" "
			+ "href=\"http://creativecommons.org/licenses/by-nc-nd/4.0/\">"
			+ "<img alt=\"Oops! You don't seem to have Internet connection...\" "
			+ "style=\"border-width:0\" "
			+ "src=\"https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png\" />"
			+ "</a><br /><span xmlns:dct=\"http://purl.org/dc/terms/\" "
			+ "href=\"http://purl.org/dc/dcmitype/InteractiveResource\" "
			+ "property=\"dct:title\" rel=\"dct:type\">"
			+ "SLON: Very Good Translation Editor</span> "
			+ "by <span xmlns:cc=\"http://creativecommons.org/ns#\" "
			+ "property=\"cc:attributionName\">Slava Todorova</span> "
			+ "<br/>is licensed under a <a rel=\"license\" "
			+ "href=\"http://creativecommons.org/licenses/by-nc-nd/4.0/\">"
			+ "Creative Commons Attribution-NonCommercial-NoDerivatives "
			+ "4.0 International License</a>.</html>";
	
	public Help() {
	}
	
}
