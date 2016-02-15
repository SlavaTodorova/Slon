import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class SlonGui {

	private JFrame frame;
	private JTable table;
	
	private File sourceFile;
	private LinkedList<Paragraph> paragraphs;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)  throws IOException, ClassNotFoundException {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SlonGui window = new SlonGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SlonGui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("SLON: automated help for translation and tranlsation reviewing");
		frame.setBounds(250, 150, 550, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		sourceFile = null;
		paragraphs = null;

		JPanel controlPanel = new JPanel();
		frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
		
		String[] columnNames = {"Source", "Target"};
		Object[][] data = {{}};
		DefaultTableModel tbModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;
			public Class<String> getColumnClass(int columnIndex) {
		        return String.class;
		    }
			public boolean isCellEditable(int row, int column) {
				if (column == 0) {
					return false;
				} else {
					return true;
				}
			}
	      };
		table = new JTable(tbModel);
		table.setDefaultRenderer(String.class, new MultiLineTableCellRenderer());
		JScrollPane scroll = new JScrollPane(table);
		frame.getContentPane().add(scroll, BorderLayout.CENTER);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (paragraphs != null && sourceFile != null) {
					// write updated translation
					saveTranslation();
				} else {
					JOptionPane.showMessageDialog(null, "There is no translation to be saved.");
				}
			}
		});
		controlPanel.add(btnSave);

		JButton btnChooseFile = new JButton("Choose file");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// reset the instance variables and all that
				// TODO here is a good place for an alert asking for saving the current translations
				if (sourceFile != null && paragraphs != null) {
					showSaveOptionDialog();
				}
				clean();
				
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File(System.getProperty("user.home")+"/work/TransIt/Slon"));
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					sourceFile = chooser.getSelectedFile();
					System.out.println("Selected file: " + sourceFile.getAbsolutePath());
					// read translation in progress
					loadTranslation(sourceFile);
				}
			}

					});
		controlPanel.add(btnChooseFile);
		
	}


	/*
	 * Checks if the input file is .txt.
	 * 
	 */
	private void loadTranslation(File srcFile) {		
		String srcFileName = srcFile.getName();
		if (srcFile.isFile() && srcFileName.endsWith(".txt")) {
			String serFileName = getSerFileName();
			File serFile = new File(serFileName);
			if (! serFile.exists()) {
				try {
					paragraphs = readSource(srcFileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					paragraphs = deserializeAll(getSerFileName());
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
			ListIterator<Paragraph> iteratorP = paragraphs.listIterator();
			while (iteratorP.hasNext()) {
				showParagraph(iteratorP.next());
			}
		}
	}

	private String getSerFileName() {
		String fullName = sourceFile.getName();
		String stem = fullName.substring(0, fullName.length()-3); // without "txt"
		String serObjFile = stem + "slon";
		return serObjFile;
	}

	private String getTarFileName() {
		String fullName = sourceFile.getName();
		String stem = fullName.substring(0, fullName.length()-3); // without "txt"
		String targetFile = stem + "translated.txt";
		return targetFile;
	}

	private void saveTranslation() {
		// get the new translations
		ListIterator<Paragraph> iteratorP = paragraphs.listIterator();
		Paragraph par = iteratorP.next();
		ListIterator<Segment> iteratorS = par.getSegments().listIterator();
		Segment seg = iteratorS.next();
		for (int i=0; i < paragraphs.size(); i++) {
			seg.setTarget(new SegmentComponent(table.getModel().getValueAt(i, 1).toString()));
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
		// serialization
		try {
			serializeAll(getSerFileName()); // serialize the paragraphs
		} catch (IOException e) {
			e.printStackTrace();
		} 
		// writing to a monolingual target file
		try {
			writeTarget(getTarFileName()); // write the translated parts to the .txt target file
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}


	/*
	 * Reads a monolingual file and divides it into paragraphs.
	 * For now Each paragraph has exactly one segment. %TODO Change this!
	 * 
	 * @param filename the location of the file to be read
	 * @return a LinkedList of the paragraphs in the text.
	 */
	private LinkedList<Paragraph> readSource(String srcFileName) throws IOException {
		LinkedList<Paragraph> paragraphs = new LinkedList<Paragraph>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(srcFileName));
			String line;
			Paragraph par;
			while ((line = in.readLine()) != null) {
				par = new Paragraph(new Segment(new SegmentComponent(line)));
				paragraphs.add(par);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return paragraphs;
	}

	private void writeTarget(String tarFileName) throws IOException {
		FileWriter fw = new FileWriter(tarFileName);
		BufferedWriter bw = new BufferedWriter(fw);
		ListIterator<Paragraph> iteratorP = paragraphs.listIterator();
		ListIterator<Segment> iteratorS;
		while (iteratorP.hasNext()) {
			LinkedList<Segment> segments = iteratorP.next().getSegments();
			iteratorS = segments.listIterator();
			while (iteratorS.hasNext()) {
				bw.write(iteratorS.next().getTarget().getText()); // TODO handle separators
			}
			bw.write("\n"); // end of paragraph
		}
		bw.flush();
		bw.close();
	}

	/*
	 * Serializes the whole list of paragraphs
	 * 
	 * @param list LinkedList of Paragraphs that should be serialized
	 * @param fileName where the serialized objects should go
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

	/*
	 * Deserialized the whole list of paragraphs
	 * 
	 * @param filename where the serialized objects should be read from
	 * @return LinkedList with all the Paragraph objects written in the file
	 */
	private LinkedList<Paragraph> deserializeAll(String serFileName) throws IOException, ClassNotFoundException {
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

	private void showSaveOptionDialog() {
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
			saveTranslation();
			JOptionPane.showMessageDialog(null, 
					"Current translation saved successfully!\n" +
					"Proceed with loading of new source file.");
		}
	}
	
	private void showParagraph(Paragraph par) {
		DefaultTableModel tblModel = (DefaultTableModel) table.getModel();
		
		ListIterator<Segment> iterator = par.getSegments().listIterator();
		
		String sourceText;
		String targetText;
		while (iterator.hasNext()) {
			Segment seg = iterator.next();
			sourceText = seg.getSource().getText();
			System.out.println(seg.getSource().getText());
			// parPanel.add(sourceText);
			targetText = seg.getTarget().getText();
			// parPanel.add(targetText);
			tblModel.addRow(new Object[] {sourceText, targetText});
		}
	}

	private void clean() {
		sourceFile = null;
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
	}
}
