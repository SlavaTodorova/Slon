import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;
import java.awt.Component;

import javax.swing.border.EmptyBorder;

import java.awt.Cursor;

public class SlonGui {

	private JFrame frame;
	private JTable table;

	private JButton btnSave;

	private File sourceFile;
	private File translationFile;
	private LinkedList<Paragraph> paragraphs;

	private boolean unsavedChanges; // if there are unsaved changes

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)  throws IOException, ClassNotFoundException {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Set cross-platform Java L&F (also called "Metal")
					UIManager.setLookAndFeel(
							UIManager.getSystemLookAndFeelClassName());
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
		// frame.setBounds(250, 150, 550, 400);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		sourceFile = null;
		translationFile = null;
		paragraphs = null;
		unsavedChanges = false;

		JPanel controlPanel = new JPanel((LayoutManager) new BorderLayout());
		controlPanel.setBackground(new Color(70, 130, 180));
		JPanel actionsPanel = new JPanel((LayoutManager) new FlowLayout(FlowLayout.LEFT));
		actionsPanel.setBackground(new Color(70, 130, 180));
		controlPanel.add(actionsPanel, BorderLayout.WEST);
		frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
		btnSave = new JButton("Save");
		btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnSave.setForeground(new Color(0, 0, 0));
		btnSave.setBackground(UIManager.getColor("Table.selectionBackground"));
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// write updated translation
				saveTranslation();
			}
		});
		btnSave.setEnabled(false);
		actionsPanel.add(btnSave);

		JButton btnChooseSource = new JButton("Choose source");
		btnChooseSource.setForeground(new Color(0, 0, 0));
		btnChooseSource.setBackground(UIManager.getColor("Table.selectionBackground"));
		btnChooseSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// reset the instance variables and all that
				// TODO here is a good place for an alert asking for saving the current translations
				if (unsavedChanges) {
					showSaveOptionDialog();
				}
				clean();

				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File(System.getProperty("user.home")));

				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					getCorrectFile(chooser.getSelectedFile(), chooser);
					if (translationFile != null) {
						// read translation in progress
						loadOldTranslation(translationFile);
					} else if (sourceFile != null) {
						// read source for new translation
						loadNewTranslation(sourceFile);
					}
				}
			}

		});
		actionsPanel.add(btnChooseSource);

		JPanel helpPanel = new JPanel((LayoutManager) new FlowLayout(FlowLayout.RIGHT));
		helpPanel.setBackground(new Color(70, 130, 180));
		controlPanel.add(helpPanel, BorderLayout.EAST);
		JButton btnHelp = new JButton("Help");
		btnHelp.setForeground(new Color(0, 0, 0));
		btnHelp.setBackground(UIManager.getColor("Table.selectionBackground"));
		btnHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Help help = new Help();
				JTextArea helpTextArea = new JTextArea(20, 60);
				helpTextArea.setText(help.getBasic());
				helpTextArea.setLineWrap(true);
				helpTextArea.setWrapStyleWord(true);
				helpTextArea.setEditable(false);
				helpTextArea.setOpaque(false);
				//JScrollPane scrollPane = new JScrollPane(helpTextArea);
				JOptionPane.showMessageDialog(null, helpTextArea, "SLON Help Page",
					    JOptionPane.INFORMATION_MESSAGE);
			}
		});
		helpPanel.add(btnHelp);


		//		//TODO add the opt
		//		JButton btnChooseTarget = new JButton("Choose target");
		//		btnChooseTarget.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent arg0) {
		//				JFileChooser chooser = new JFileChooser();
		//				chooser.setCurrentDirectory(new File(System.getProperty("user.home")+"/work/TransIt/Slon"));
		//			}
		//
		//		});
		//		btnChooseTarget.setEnabled(false);
		//		controlPanel.add(btnChooseTarget);

		String[] columnNames = {"Source", "Target", "Comments"};
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
		table = new MultiLineCellTable(tbModel);
		table.setShowHorizontalLines(false);
		table.setDefaultRenderer(String.class, new MultiLineTableCellRenderer());
		table.setDefaultEditor(String.class, new MultiLineTableCellEditor());
		InputMap input = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actions = table.getActionMap();
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		input.put(enter, "go-to-next");
		actions.put("go-to-next", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if (row < table.getRowCount()-1) {
					row += 1;
				}
				table.setRowSelectionInterval(row, row);
			}

		});
		table.getModel().addTableModelListener(new TableModelListener() {	
			@Override
			public void tableChanged(TableModelEvent e) {
				int col = e.getColumn();
				if (col > 0) {
					unsavedChanges = true;
					btnSave.setEnabled(true);
				}
			}
		});
		JScrollPane scroll = new JScrollPane(table);		
		scroll.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		scroll.setBackground(new Color(255, 255, 255));
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
		frame.getContentPane().add(scroll, BorderLayout.CENTER);

		WindowListener exitListener = new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (unsavedChanges) {
					showSaveOptionDialog();
					System.exit(0);
				} else {
					System.exit(0);
				}
			}
		};
		frame.addWindowListener(exitListener);

		frame.pack();
	}

	/**
	 * Loads translation from a .slon file
	 * Or reads a monolingual source file, if no translation is available yet
	 * 
	 */
	private void loadOldTranslation(File f) {		
		try {
			paragraphs = deserializeAll(f.getName());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		showParagraphs();
	}

	/**
	 * Loads source for translation from a .txt file
	 *
	 */
	private void loadNewTranslation(File f) {		
		try {
			paragraphs = readSource(f.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		showParagraphs();
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
		try {
			table.getCellEditor().stopCellEditing();
		} catch (Exception e) {
			// do nothing it is OK if nothing is edited, so cell editing can't be stopped
		}
		// get the new translations
		ListIterator<Paragraph> iteratorP = paragraphs.listIterator();
		Paragraph par = iteratorP.next();
		ListIterator<Segment> iteratorS = par.getSegments().listIterator();
		Segment seg = iteratorS.next();
		DefaultTableModel tbModel = (DefaultTableModel) table.getModel();
		for (int i=0; i < paragraphs.size(); i++) {
			seg.setTarget(new SegmentComponent(tbModel.getValueAt(i, 1).toString()));
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
		unsavedChanges = false;
		btnSave.setEnabled(false);
	}


	/**
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

	/**
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

	/**
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
		} else if (n == 1) {
			try {
				table.getCellEditor().stopCellEditing();
			} catch (Exception e) {
				// do nothing it is OK if nothing is edited, so cell editing can't be stopped
			}
		}
	}

	private void showParagraphs() {
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

	private void getCorrectFile(File f, JFileChooser chooser) {
		String fileName = f.getName();
		while (!fileName.endsWith(".txt") && !fileName.endsWith(".slon")) {
			JOptionPane.showMessageDialog(null, "Please load a \".txt\" or a \".slon\" file.");
			int result = chooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
				fileName = f.getName();
				getCorrectFile(f, chooser);
			}
		}
		if (fileName.endsWith(".txt")) {
			sourceFile = f;
			File possFile = new File(fileName.substring(0, fileName.length()-3)+"slon");
			if (possFile.exists()) {
				translationFile = possFile;
			}
		} else { // ends with ".slon"
			translationFile = f;
			File possFile = new File(fileName.substring(0, fileName.length()-4)+"txt");
			if (possFile.exists()) {
				sourceFile = possFile;
			}
		}
		//		// TODO See if it is meaningful to allow loading of targets as sources
		//		if (fileName.endsWith(".translated.txt")) {
		//			Object[] options = {"Load as source", "Load as target"};
		//			int n = JOptionPane.showOptionDialog(null,
		//					"You have selected a file containing translation of another file.\n" +
		//					"Would you like to open this file as source or as target text of your translation?",
		//					"Choosing if to load a target of a previous translation as source.",
		//					JOptionPane.YES_NO_OPTION,
		//					JOptionPane.QUESTION_MESSAGE,
		//					null,
		//					options,
		//					options[1]);
		//			if (n == 0) {
		//				return f; // as source
		//			} else if (n == 1) {
		//				fileName = fileName.substring(0, fileName.length()-15) + ".txt";
		//				return new File(fileName);
		//			}
		//		}
	}
}
