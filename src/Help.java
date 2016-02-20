
public class Help {
	
	private String basic = "To start a translation, open a source text file in \".txt\" format.\n\n"
			+ "Type your translation in the second, central column.\n\n"
			+ "After typing a translation the segment (the table row) should be closed"
			+ " in order for the changes to take effect. Use Enter for segment closing.\n\n"
			+ "To start editing the next segment, hit Enter again."
			+ "Alternatively you can navigate up and down with your keyboard's Up and Down keys."
			+ "Or use the mouse with a single click on the segment that you wand to (open to) edit.\n\n"
			+ "If you need to add comments, use the third, the right hand side column."
			+ "The coments won't appear in the final translation.\n\n"
			+ "To save your translation, hit the save buton."
			+ "This action will produce a new text file, containing your translated text only"
			+ "And a \".slon\" file which will contain also the alignment to the source text and the comments\n\n"
			+ "To resume your translation you can load either the same source file as the first time, or the produced \".slon\" file.";
	
	public Help() {
	}
	
	public String getBasic() {
		return basic;
	}
}
