package text;
/**
 * Represents a paragraph of the source text together with its translation.
 * The translated counterpart can have more than one new lines, i.e. 
 * more than one actual paragraphs. 
 * The source text is always exactly one paragraph.
 * 
 * @author Slava Todorova
 */
import java.util.LinkedList;
import java.util.ListIterator;

public class Paragraph implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	// the segments that constitute the paragraph
	private LinkedList<Segment> segments;
	
	// Constructors
	public Paragraph(LinkedList<Segment> segments) {
		this.segments = segments;
	}
	
	public Paragraph(Segment segment) {
		this();
		this.segments.add(segment);
	}
	
	public Paragraph() {
		this(new LinkedList<Segment>());
	}
	
	// Getter
	public LinkedList<Segment> getSegments() {
		return this.segments;
	}
	
	// Real methods
	
	/*
	 * Adding a segment to the end of the paragraph
	 */
	public void addSegment(Segment aSegment) {
		this.segments.add(aSegment);
	}
	
	
	/*
	 * Removing a segment from the paragraph
	 */
	public void removeSegment(Segment aSegment) {
		this.segments.remove(aSegment);
	}
	
	/*
	 * Merging segments
	 */
	public void merge(Segment firstSegment, Segment lastSegment) {
		ListIterator<Segment> iterator = this.segments.listIterator(
										this.segments.indexOf(firstSegment));
		String mergedSource = "";
		String mergedTarget = "";
		String newSeparator = "";
		Segment current = null;
		String currentSeparator = "";
		while (iterator.hasNext()) {
			if (! current.equals(lastSegment)) {
				current = iterator.next();
				mergedSource += currentSeparator 
											+ current.getSource().getText();
				mergedTarget += " " + current.getTarget().getText(); 
				// let the translator correct the separator
				currentSeparator = current.getSource().getSeparator();
			}
		}
		newSeparator = currentSeparator; // the separator of the last segment
		// put the merged content into the first Segment Object
		SegmentComponent src = new SegmentComponent(mergedSource, newSeparator);
		SegmentComponent trg = new SegmentComponent(mergedTarget);
		firstSegment.setSource(src);
		firstSegment.setTarget(trg);
		// remove the old singular segments
		while (iterator.hasPrevious()) {
			if (! current.equals(firstSegment)) { // except the first one
				this.segments.remove(current);
			}
			current = iterator.previous();
		}
	}
	
	public String toString() {
		String src = "";
		String trg = "";
		ListIterator<Segment> iterator = this.segments.listIterator(0);
		while (iterator.hasNext()) {
			Segment seg = iterator.next();
			src += seg.getSource().toString(); 
			trg += seg.getTarget().toString();
		}
		
		return src + "|" + trg;
	}
}
