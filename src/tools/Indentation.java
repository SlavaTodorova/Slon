package tools;
/**
 * Enables indentation and indentation removal
 * 
 * @author velislava
 */
public class Indentation {
	
	public final String INDENT = "\t";
	
	public Indentation() {
	}
	
	/**
	 * Indents a text by adding an indent at the beginning of each line
	 */
	public String indent(String text) {
		String indentedText = "";
		String[] lines = text.split("\n");
		for (int i=0; i < lines.length; i++) {			
			indentedText += INDENT + lines[i].replace(INDENT, ""); // all
									// non initial indented strings are removed
			if (i < lines.length-1) {
				indentedText += "\n";
			}
		}
		return indentedText;
	}
	
	/**
	 * Removes indentation from a text
	 * @param text text to be cleaned from indents
	 * @return unindented text
	 */
	public String removeIndentation(String text) {
		return text.replace(INDENT, "");
	}

}

