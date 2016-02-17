
public class Indentation {
	
	public final String INDENT = "\t";
	
	public Indentation() {
	}
	
	public String indent(String text) {
		String indentedText = "";
		String[] lines = text.split("\n");
		for (int i=0; i < lines.length; i++) {
			indentedText += INDENT + lines[i].replace(INDENT, ""); // remove any non initial indent strings
			if (i < lines.length-1) {
				indentedText += "\n";
			}
		}
		return indentedText;
	}

}

