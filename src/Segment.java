/*
 * Class Segment
 * Designed to represent a segment of a bilingual corpus.
 */

public class Segment implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	// source and target
	private SegmentComponent source;
	private SegmentComponent target;
	private String comment;
	
	// Constructors	
	public Segment(SegmentComponent src, SegmentComponent trg, String comment) {
		this.source = src;
		this.target = trg;
		this.comment = comment;
	}
	
	public Segment(SegmentComponent src, SegmentComponent trg) {
		this();
		this.source = src;
		this.target = trg;
	}
	
	public Segment(SegmentComponent src) {
		this();
		this.source = src;
	}
	
	public Segment() {
		this(new SegmentComponent(), new SegmentComponent(), "");
	}
	
	// Getters and setters
	public SegmentComponent getSource() {
		return this.source;
	}
	
	public void setSource(SegmentComponent src) {
		this.source = src;
	}
	
	public SegmentComponent getTarget() {
		return this.target;
	}
	
	public void setTarget(SegmentComponent trg) {
		this.target = trg;
	}
	
	public String getComment() {
		return this.comment;
	}
	
	public void setComment(String text) {
		this.comment = text;
	}
	
	/*-----To String------*/
	
	public String toString() {
		return this.source.toString() + "|" + this.target.toString();
	}
	
}
