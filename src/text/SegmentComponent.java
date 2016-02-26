package text;
/**
 * The class SegmentComponent describes the components (source and target)
 * of a segment.
 */

import java.io.Serializable;

public class SegmentComponent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String language; // null if unknown
	private String text; // the text of the segment
	private String separator; // the separator between this segment and the next

	public SegmentComponent(String text, String separator, String lang) {
		this.text = text;
		this.separator = separator;
		this.language = lang;
	}
	
	public SegmentComponent(String text, String separator) {
		this(text, separator, null);
	}
	
	public SegmentComponent(String text) {
		this(text, "", null);
	}
	
	public SegmentComponent() {
		this("", "", null);
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getSeparator() {
		return this.separator;
	}
	
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	public String getLanguage() {
		return this.language;
	}
	
	public void setLanguage(String lang) {
		this.language = lang;
	}
	
	public String toString() {
		return this.text + this.separator;
	}
}
