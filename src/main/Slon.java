package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import text.Paragraph;
import text.Segment;
import text.SegmentComponent;
import tools.Project;

/**
 * The core of the program. Contains the main components and actions.
 * 
 * @author Slava Todorova
 *
 */
public class Slon {
	
	public Project project;
	
	public LinkedList<Paragraph> paragraphs;
	
	public boolean unsavedChanges;
	
	public Slon() {
		this.project = null;
		this.paragraphs = null;
		this.unsavedChanges = false;
	}
	
	/**
	 * Saves the translation in two steps. 
	 * First, the all the paragraph objects are serialized
	 * and stored in the .slon file.
	 * Second, the target text is saved in a plain text (.translated.txt) file.
	 */
	public void saveTranslation(JTable table) {
		/* If there is an unclosed segment, close it. */
		try {
			table.getCellEditor().stopCellEditing();
		} catch (Exception e1) {
			// do nothing
			// it is normal that if nothing is edited, editing can't be stopped
		}
		/* get the new translations */
		ListIterator<Paragraph> iteratorP = this.paragraphs.listIterator();
		Paragraph par = iteratorP.next();
		ListIterator<Segment> iteratorS = par.getSegments().listIterator();
		Segment seg = iteratorS.next();
		DefaultTableModel tbModel = (DefaultTableModel) table.getModel();
		for (int i=0; i < paragraphs.size(); i++) {
			seg.getTarget().setText(tbModel.getValueAt(i, 1).toString());
			seg.setComment(tbModel.getValueAt(i, 2).toString());
			if (! iteratorS.hasNext()) {
				if (! iteratorP.hasNext()) {
					break;
				} else {
					par = iteratorP.next();
					iteratorS = par.getSegments().listIterator();
				}
			}
			seg = iteratorS.next();
		}
		/* serialization */
		try {
			serializeAll(project.translationFile.getAbsolutePath()); // all paragraphs
			// TODO check if the name of the file or the file itself is better
		} catch (IOException e) {
			e.printStackTrace();
		} 
		/* writing to a monolingual target file */
		try {
			writeTarget(project.targetFile.getAbsolutePath()); // write to the .txt target file
			// TODO check if the name of the file or the file itself is better
		} catch (IOException e) {
			e.printStackTrace();
		}

		/* resetting variables */
		unsavedChanges = false;
	}

	/**
	 * Serializes the whole list of paragraphs
	 * 
	 * @param serFileName where the serialized objects should go
	 */
	private void serializeAll(String serFileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(serFileName);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		ListIterator<Paragraph> iterator= paragraphs.listIterator();
		while (iterator.hasNext()) {
			oos.writeObject(iterator.next());
		}
		oos.close();
	}
	
	/**
	 * Writes the target text to a plain text file
	 * 
	 * @param tarFileName the name of the file where the target text should go
	 * @throws IOException
	 */
	private void writeTarget(String tarFileName) throws IOException {
		FileWriter fw = new FileWriter(tarFileName);
		BufferedWriter bw = new BufferedWriter(fw);
		ListIterator<Paragraph> iteratorP = paragraphs.listIterator();
		ListIterator<Segment> iteratorS;
		SegmentComponent target;
		while (iteratorP.hasNext()) {
			LinkedList<Segment> segments = iteratorP.next().getSegments();
			iteratorS = segments.listIterator();
			while (iteratorS.hasNext()) {
				target = iteratorS.next().getTarget();
				bw.write(target.getSeparator());
				bw.write(target.getText());
			}
			bw.write("\n"); // end of paragraph
		}
		bw.flush();
		bw.close();
	}
	
	/**
	 * Loads translation from a .slon file
	 * Or reads a monolingual source file, if no translation is available yet
	 * @param table the translation table
	 * @param f the translation file
	 * @param btnClose the "Close" button
	 * @param closeItem the menu item "Close"
	 */
	public void resumeProject(File projectDir, JTable table) {		
		try {
			project = new Project(projectDir.toPath());
			paragraphs = deserializeAll(project.translationFile.getAbsolutePath());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		showParagraphs(table);
	}

	/**
	 * Loads source for translation from a .txt file
	 * @param table the translation table
	 * @param f the source file
	 * @param btnClose the "Close" button
	 * @param closeItem the menu item "Close"
	 */
	public void startProject(Project proj, JTable table) {
		project = proj;
		try {
			paragraphs = readSource(project.sourceFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		showParagraphs(table);
		saveTranslation(table); // so that the other project files are created
	}

	/**
	 * Reads a monolingual file and divides it into paragraphs.
	 * For now each paragraph has exactly one segment. %TODO Maybe change this.
	 * 
	 * @param filename the location of the file to be read
	 * @return a LinkedList of the paragraphs in the text.
	 */
	private LinkedList<Paragraph> readSource(String srcFileName)
			throws IOException {
		LinkedList<Paragraph> paragraphs = new LinkedList<Paragraph>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(srcFileName));
			String line;
			Paragraph par;
			String sep = "";
			while ((line = in.readLine()) != null) {
				while (line.trim().equals("")) {
					sep += "\n"; // empty paragraph
					line = in.readLine();
				}
				par = new Paragraph(new Segment(new SegmentComponent(line, sep),
						new SegmentComponent("", sep)));
				paragraphs.add(par);
				sep = "";
			}
		} catch (Exception e) {
			// do nothing
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
		return paragraphs;
	}

	/**
	 * Deserializes the whole list of paragraphs
	 * 
	 * @param serFilename where the serialized objects should be read from
	 * @return LinkedList with all the Paragraph objects written in the file
	 */
	private LinkedList<Paragraph> deserializeAll(String serFileName) 
			throws IOException, ClassNotFoundException {
		LinkedList<Paragraph> list = new LinkedList<Paragraph>();

		FileInputStream fis = new FileInputStream(serFileName);
		ObjectInputStream ois = new ObjectInputStream (fis);
		Object obj;
		Paragraph par = null;
		try {
			while (true) {
				obj = ois.readObject();
				if (obj instanceof Paragraph) {
					par = (Paragraph) obj;
					list.add(par);
				}
			}
		} catch (EOFException e) {
		} finally {
			ois.close();
		}
		return list;
	}

	
	/**
	 * Reads the Paragraph objects and displays them on the translation table
	 * @param table the translation table
	 */
	private void showParagraphs(JTable table) {
		DefaultTableModel tblModel = (DefaultTableModel) table.getModel();

		ListIterator<Paragraph> iteratorP = paragraphs.listIterator();
		ListIterator<Segment> iteratorS;
		String sourceText;
		String targetText;
		String comment;

		while (iteratorP.hasNext()) {
			Paragraph par = iteratorP.next();
			iteratorS = par.getSegments().listIterator();
			while (iteratorS.hasNext()) {
				Segment seg = iteratorS.next();
				sourceText = seg.getSource().getText();
				targetText = seg.getTarget().getText();
				comment = seg.getComment();
				tblModel.addRow(new Object[] {sourceText, targetText, comment});
			}
		}

	}
}
