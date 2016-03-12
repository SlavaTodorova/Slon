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
			+ "<p>E-mail: todorova.slava@gmail.com</p><br/></html>"
//			+ "License: "
//			+ "Creative Commons Attribution-NonCommercial-NoDerivatives 4.0"
//			+ " International Public License "
//			+ "(https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode)"
			+ "";
	
	public String startNew = "<html><p>To start a translation, open a source"
			+ " text file in \".txt\" format.</p><br/>"
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
	
	public String resumeOld = "<html>To resume your translation you can load "
			+ "either the same source file as the first time, "
			+ "or the produced \".slon\" file.</html>";
	
	public String save = "<html>To save your translation, hit the save buton.<br/><br/>"
			+ "This action will produce in the same directory "
			+ "as your source file<br/>"
			+ "(1) a new text file, "
			+ "called \"YourSourceFileName.translated.txt\" "
			+ "containing your translated text only,<br/>"
			+ "and<br/>"
			+ "(2) a \".slon\" file which will contain also the alignment "
			+ "to the source text and the comments you have made.<br/><br/>"
			+ "The plain text file with your translation can be opened"
			+ " with any text editor suited to open \".txt\" files.<br/>"
			+ "The \".slon\" file can only be opened with this program.<br/><br/>"
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
