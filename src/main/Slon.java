package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import text.Paragraph;
import text.Segment;
import text.SegmentComponent;
import tools.Utils;

/**
 * The core of the program. Contains the main components and actions.
 * 
 * @author Slava Todorova
 *
 */
public class Slon {
	
	private LinkedList<Paragraph> paragraphs;
	private File translationFile;
	private File sourceFile;
	
	public boolean unsavedChanges;
	
	public Slon() {
		this.paragraphs = null;
		this.translationFile = null;
		this.sourceFile = null;
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
			if (translationFile != null) {
				serializeAll(translationFile.getAbsolutePath()); // all paragraphs
			} else {
				serializeAll(getTranslationFileName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		/* writing to a monolingual target file */
		try {
			writeTarget(getTarFileName()); // write to the .txt target file
		} catch (IOException e) {
			e.printStackTrace();
		}

		/* resetting some variables */
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
	 * Gets a name for the translation file, once the source file is known.
	 * I.e. removes the "txt" extension and appends "slon"
	 * 
	 * @return a name for the target file
	 */
	private String getTranslationFileName() {
		String fullName = sourceFile.getAbsolutePath();
		String stem = fullName.substring(0, fullName.length()-3); // - "txt"
		String translationFileName = stem + "slon";
		return translationFileName;
	}
	
	/**
	 * Gets a name for the target file, once the translation file is known.
	 * I.e. removes the "slon" extension and appends "translated.txt"
	 * 
	 * @return a name for the target file
	 */
	private String getTarFileName() {
		String fullName;
		String stem;
		if (translationFile != null) {
			fullName = translationFile.getAbsolutePath();
			stem = fullName.substring(0, fullName.length()-4); // - "slon"
		} else {
			fullName = sourceFile.getAbsolutePath();
			stem = fullName.substring(0, fullName.length()-3); // - "txt"
		}
		String targetFile = stem + "translated.txt";
		return targetFile;
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
	 * Choose a file to start/resume a translation
	 * @param table the translation table
	 * @param btnSave the "Save" button
	 * @param saveItem the menu item "Save"
	 * @param btnClose the "Close" button
	 * @param closeItem the menu item "Close"
	 */
	public void chooseFileToOpen(JTable table, 
			JButton btnSave, JMenuItem saveItem, 
			JButton btnClose, JMenuItem closeItem) {
		closeCurrentTranslation(table, btnSave, saveItem, btnClose, closeItem);	

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(
				new File(System.getProperty("user.home")));
		chooser.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				// TODO Auto-generated method stub
				return ".txt and .slon";
			}

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				String extension = Utils.getExtension(f);
				if (extension != null) {
					if (extension.equals(Utils.txt) ||
							extension.equals(Utils.slon)) {
						return true;
					} else {
						return false;
					}
				}

				return false;
			}
		});


		int result = chooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			getCorrectFile(chooser.getSelectedFile(), chooser);
			if (translationFile != null) {
				// read translation in progress
				loadOldTranslation(table, translationFile, btnClose, closeItem);
			} else if (sourceFile != null) {
				// read source for new translation
				loadNewTranslation(table, sourceFile, btnClose, closeItem);
			}
		}
	}
	
	/**
	 * Close current translation
	 * @param table the translation table
	 * @param btnSave the "Save" button
	 * @param saveItem the menu item "Save"
	 * @param btnClose the "Close" button
	 * @param closeItem the menu item "Close"
	 */
	public void closeCurrentTranslation(JTable table, 
			JButton btnSave, JMenuItem saveItem, 
			JButton btnClose, JMenuItem closeItem) {
		/* close open paragraphs */
		try {
			table.getCellEditor().stopCellEditing();
		} catch (Exception e) {
			// do nothing, sometimes there wasn't any open segment
		}

		/* reset the instance variables and all that */
		if (unsavedChanges) {
			showSaveOptionDialog(table, btnSave, saveItem);
		}
		clean(table, btnClose, closeItem);
	}

	/**
	 * Shows a dialog that gives the user the option to save the translation
	 * 	 * @param table the translation table
	 * @param btnSave the "Save" button
	 * @param saveItem the menu item "Save"
	 */
	public void showSaveOptionDialog(
			JTable table, JButton btnSave, JMenuItem saveItem) {
		Object[] options = {"Save", "Don't save"};
		int n = JOptionPane.showOptionDialog(null,
				"Would you like to save your current translation?",
				"Safe switching between source files.",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);
		if (n == 0) {
			saveTranslation(table);
			btnSave.setEnabled(false);
			saveItem.setEnabled(false);
		} else if (n == 1) {
			try {
				table.getCellEditor().stopCellEditing();
			} catch (Exception e) {
				// do nothing - if nothing is edited,
				// it is OK that cell editing can't be stopped
			}
			btnSave.setEnabled(false);
			saveItem.setEnabled(false);
			unsavedChanges = false;
		}
	}
	
	/**
	 * Loads translation from a .slon file
	 * Or reads a monolingual source file, if no translation is available yet
	 * @param table the translation table
	 * @param f the translation file
	 * @param btnClose the "Close" button
	 * @param closeItem the menu item "Close"
	 */
	private void loadOldTranslation(
			JTable table, File f, JButton btnClose, JMenuItem closeItem) {		
		try {
			paragraphs = deserializeAll(f.getAbsolutePath());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		showParagraphs(table);
		btnClose.setEnabled(true);
		closeItem.setEnabled(true);
	}

	/**
	 * Loads source for translation from a .txt file
	 * @param table the translation table
	 * @param f the source file
	 * @param btnClose the "Close" button
	 * @param closeItem the menu item "Close"
	 */
	private void loadNewTranslation(
			JTable table, File f, JButton btnClose, JMenuItem closeItem) {		
		try {
			paragraphs = readSource(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		showParagraphs(table);
		btnClose.setEnabled(true);
		closeItem.setEnabled(true);
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

	/**
	 * Cleans the translation table and the list of Paragraphs.
	 * @param table the translation table
	 * @param btnClose the "Close" button
	 * @param closeItem the menu item "Close"
	 */
	private void clean(JTable table, JButton btnClose, JMenuItem closeItem) {
		sourceFile = null;
		translationFile = null;
		DefaultTableModel tbModel = (DefaultTableModel) table.getModel();
		if (paragraphs != null) {
			for (int i = paragraphs.size()-1; i >= 0; i--) {
				tbModel.removeRow(i); // clean the displayed table
			}
		} else {
			while (table.getRowCount() > 0) {
				tbModel.removeRow(0);
			}
		}
		paragraphs = null;
		btnClose.setEnabled(false);
		closeItem.setEnabled(false);
	}
	
	/**
	 * Figures out if the user has loaded a plain text source file
	 * or a .slon file with a translation in progress
	 * and determines the correct names of the sourceFile and translationFile
	 * 
	 * @param f the file that the user has chosen
	 * @param chooser the file chooser that the user chose the file with
	 * (to make the most of a single object) 
	 */
	private void getCorrectFile(File f, JFileChooser chooser) {
		String fileName = f.getAbsolutePath();

		if (! f.exists()) {
			JOptionPane.showMessageDialog(null, "File not found.");
			rechoose(chooser);
			return;
		}

		if (Utils.getExtension(f).equals(Utils.txt)) {
			sourceFile = f;
			File theTranslationFile = new File(
					fileName.substring(0, fileName.length()-3)+"slon");
			if (theTranslationFile.exists()) {
				translationFile = theTranslationFile;
			}
		} else if (Utils.getExtension(f).equals(Utils.slon)){
			translationFile = f;
			File eventualSourceFile = new File(
					fileName.substring(0, fileName.length()-4)+"txt");
			if (eventualSourceFile.exists()) {
				sourceFile = eventualSourceFile;
			} else {
				System.out.println("Initial source file in plain text format "
						+ "not found. But the translation is safe.");
				sourceFile = null;
			}
		} else {
			JOptionPane.showMessageDialog(null, "Invalid input file format!\n"
					+ "Only files with extensions \".txt\" and \".slon\" "
					+ "are accepted.");
			rechoose(chooser);
		}
	}
	
	/**
	 * Opens new file chooser dialog and checks the selected file
	 * @param chooser the JFileChooser to choose the new file with
	 */
	private void rechoose(JFileChooser chooser) {
		int chooserResult = chooser.showOpenDialog(null);
		if (chooserResult == JFileChooser.APPROVE_OPTION) {
			getCorrectFile(chooser.getSelectedFile(), chooser);
		}
	}
}
