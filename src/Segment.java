/*
 * Class Segment
 * Designed to represent a segment of a bilingual corpus.
 */

public class Segment implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	// source and target
	private SegmentComponent source;
	private SegmentComponent target;
	
	// Constructors	
	public Segment(SegmentComponent src, SegmentComponent trg) {
		this.source = src;
		this.target = trg;
	}
	
	public Segment(SegmentComponent src) {
		this(src, new SegmentComponent());
	}
	
	public Segment() {
		this(new SegmentComponent(), new SegmentComponent());
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
	
	public String toString() {
		return this.source.toString() + "|" + this.target.toString();
	}
	
}
