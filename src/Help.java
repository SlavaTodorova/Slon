/**
 * This class manages the help information
 * 
 * @author Slava Todorova
 *
 */
public class Help {
	
	public String about = "SLON: Very Good Translation Editor, Version 16.03\n"
			+ "Release on: March 1, 2016\n\n"
			+ "Author: Slava Todorova\n"
			+ "E-mail: todorova.slava@gmail.com\n\n"
			+ "Licence: "
			+ "Creative Commons Attribution-NonCommercial-NoDerivatives 4.0"
			+ " International Public License "
			+ "(https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode)";
	
	public String startNew = "To start a translation, open a source text file "
			+ "in \".txt\" format.\n\n"
			+ "Type your translation in the second, central column.\n\n"
			+ "After typing a translation, the segment (the table cell) "
			+ "should be closed in order for the changes to take effect. "
			+ "Use Enter for segment closing.\n\n"
			+ "To start editing the next segment, hit Enter again. "
			+ "Alternatively you can navigate up and down with your keyboard's "
			+ "Up and Down keys. "
			+ "Or use the mouse with a single click on the segment "
			+ "that you want to edit.\n\n"
			+ "If you need to add comments, use the third column."
			+ "The coments won't appear in the final translation.\n";
	
	public String resumeOld = "To resume your translation you can load\n"
			+ "either the same source file as the first time,\n"
			+ "or the produced \".slon\" file.";
	
	public String save = "To save your translation, hit the save buton.\n\n"
			+ "This action will produce in the same directory "
			+ "as your source file\n"
			+ "(1) a new text file, "
			+ "called \"YourSourceFileName.translated.txt\" "
			+ "containing your translated text only,\n"
			+ "and\n"
			+ "(2) a \".slon\" file which will contain also the alignment "
			+ "to the source text and the comments you have made.\n\n"
			+ "The plain text file with your translation can be opened"
			+ " with any text editor suited to open \".txt\" files.\n"
			+ "The \".slon\" file can only be opened with this program.\n\n"
			+ "Caution! There is no autosave in this version of the editor!";
	
	public Help() {
	}
	
}
