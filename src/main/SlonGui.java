package main;


import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.MenuItem;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import table.MultiLineCellTable;
import table.MultiLineTableCellEditor;
import table.MultiLineTableCellRenderer;
import table.TableCellListener;
import text.Paragraph;
import text.Segment;
import text.SegmentComponent;
import tools.Help;
import tools.Utils;

import java.awt.Cursor;

public class SlonGui {

	private JFrame frame;
	private static JTable table;

	private static JButton btnSave;
	private static JButton btnChooseSource;

	private File sourceFile; // .txt
	private File translationFile; // .slon
	private LinkedList<Paragraph> paragraphs;

	public Color mainColor; // TODO make it dependent on the user preferences

	private boolean unsavedChanges; // true if there are unsaved changes

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
			throws IOException, ClassNotFoundException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setChosenLookAndFeel("Nimbus");
				SlonGui window = new SlonGui();
				window.frame.setVisible(true);
			}
		});
	}

	/**
	 * Creates the application.
	 */
	public SlonGui() {

		sourceFile = null;
		translationFile = null;
		paragraphs = null;
		unsavedChanges = false;

		mainColor = new Color(57,105,138); // TODO allow for user preferences

		frame = createFrame();

		/* Menubar */
		JMenuBar menu = createMenu();

		/* Control Panel */
		JPanel controlPanel = new JPanel((LayoutManager) new BorderLayout());
		controlPanel.setBackground(mainColor);

		/* Menu Bar */
		controlPanel.add(menu, BorderLayout.NORTH);

		/* Icon Panel */
		JPanel actionsPanel = new JPanel(
				(LayoutManager) new FlowLayout(FlowLayout.LEFT));
		actionsPanel.setBackground(mainColor);
		controlPanel.add(actionsPanel, BorderLayout.EAST);
		frame.getContentPane().add(controlPanel, BorderLayout.NORTH);

		btnSave = createButtonSave();
		actionsPanel.add(btnSave);
		// actionsPanel.add(btnSave);

		btnChooseSource = createButtonChooseSource();
		actionsPanel.add(btnChooseSource);
		// actionsPanel.add(btnChooseSource);

		/* Help Panel */ 
		//		JPanel helpPanel = new JPanel(
		//				(LayoutManager) new FlowLayout(FlowLayout.RIGHT));
		//		helpPanel.setBackground(mainColor);
		//		controlPanel.add(helpPanel, BorderLayout.EAST);

		JButton btnHelp = createButtonHelp();
		actionsPanel.add(btnHelp);
		//helpPanel.add(btnHelp);

		/* Table */
		frame.getContentPane().add(createTable(), BorderLayout.CENTER);

		/* Exit Listener */
		frame.addWindowListener(createExitListener());

		/* What does this one do? */ 
		frame.pack();

	}


	/**
	 * Loads translation from a .slon file
	 * Or reads a monolingual source file, if no translation is available yet
	 */
	private void loadOldTranslation(File f) {		
		try {
			paragraphs = deserializeAll(f.getAbsolutePath());
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
			paragraphs = readSource(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		showParagraphs();
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
	 * Saves the translation in two steps. 
	 * First, the all the paragraph objects are serialized
	 * and stored in the .slon file.
	 * Second, the target text is saved in a plain text (.translated.txt) file.
	 */
	private void saveTranslation() {
		/* If there is an unclosed segment, close it. */
		try {
			table.getCellEditor().stopCellEditing();
		} catch (Exception e) {
			// do nothing
			// it is normal that if nothing is edited, editing can't be stopped
		}
		/* get the new translations */
		ListIterator<Paragraph> iteratorP = paragraphs.listIterator();
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
		btnSave.setEnabled(false);
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

	/*
	 * Shows a dialog that gives the user the option to save the translation
	 */
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
				// do nothing - if nothing is edited,
				// it is OK that cell editing can't be stopped
			}
			btnSave.setEnabled(false);
			unsavedChanges = false;
		}
	}

	/**
	 * Reads the Paragraph objects and displays them on the translation table
	 */
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

	/**
	 * Cleans the translation table and the list of Paragraphs.
	 */
	private void clean() {
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
	 * Creates the main frame
	 * 
	 * @return the frame
	 */
	private JFrame createFrame() {
		JFrame theFrame = new JFrame("SLON: Very Good Translation Editor");
		theFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		return theFrame;
	}

	/**
	 * Creates the "Save" button
	 * 
	 * @return the "Save" button
	 */
	private JButton createButtonSave() {
		JButton save = new JButton(UIManager.getIcon("FileView.floppyDriveIcon"));
		save.setAlignmentX(Component.CENTER_ALIGNMENT);
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// write updated translation
				saveTranslation();
			}
		});
		save.setEnabled(false);
		return save;
	}

	/**
	 * Creates the "Choose source" button
	 * which allows the user to load a source text file
	 * 
	 * @return "Choose source" button 
	 */
	private JButton createButtonChooseSource() {
		JButton btnChooseSource = new JButton(
				UIManager.getIcon("FileView.directoryIcon"));
		btnChooseSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooseFileToOpen();
			}
		});
		return btnChooseSource;
	}

	/**
	 * Creates the "Help" button
	 * 
	 * @return "Help" button
	 */
	private JButton createButtonHelp() {
		JButton btnHelp = new JButton("?");
		btnHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Help help = new Help();

				/* Create the tabbed panel */
				JTabbedPane tabbedPane = new JTabbedPane();
				tabbedPane.setOpaque(false);

				JPanel panel1 = new JPanel();
				panel1.setOpaque(false);
				panel1.add(makeTextArea(help.about));

				tabbedPane.addTab("About", null, panel1,
						"About this editor");
				tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

				JPanel panel2 = new JPanel();
				panel2.add(makeTextArea(help.startNew));

				tabbedPane.addTab("Start a translation", null, panel2,
						"How to start a new translation");
				tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

				JPanel panel3 = new JPanel();
				panel3.add(makeTextArea(help.resumeOld));

				tabbedPane.addTab("Resume a translation", null, panel3,
						"How to resume a translation in progress");
				tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

				JPanel panel4 = new JPanel();
				panel4.add(makeTextArea(help.save));

				tabbedPane.addTab("Save a translation", null, panel4,
						"How to save the current translation");
				tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);

				JOptionPane.showMessageDialog(
						null, tabbedPane, "SLON Help Page",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		return btnHelp;
	}

	/**
	 * Creates the table
	 * 
	 * @return a scroll pane, containing the table
	 */
	private JScrollPane createTable() {
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
				}
				return true;
			}
		};
		table = new MultiLineCellTable(tbModel);
		table.setShowHorizontalLines(false);
		table.setDefaultRenderer(
				String.class, new MultiLineTableCellRenderer());
		table.setDefaultEditor(String.class, new MultiLineTableCellEditor());
		/* Center text in header */
		centerTableHeader();
		/* Navigation from one cell to the next by pressing Enter*/
		InputMap input = table.getInputMap(
				JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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
				boolean noEdit = true;
				try {
					table.getCellEditor().stopCellEditing();
					noEdit = false;
				} catch (Exception e1) {
					// do nothing, sometimes there just was no edit
				}
				if (noEdit) {
					table.setRowSelectionInterval(row, row);
				}
			}

		});

		/* Listener for changes in the table content */ 
		Action action = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e)
			{
				TableCellListener tcl = (TableCellListener)e.getSource();
				if (tcl.getColumn() > 0) {
					if (! tcl.getOldValue().equals(tcl.getNewValue())) {
						unsavedChanges = true;
						btnSave.setEnabled(true);
					}
				}
			}
		};
		TableCellListener tcl = new TableCellListener(table, action);
		table.addPropertyChangeListener(tcl);

		/* Put the table in a scroll pane */ 
		JScrollPane scroll = new JScrollPane(table);
		scroll.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		scroll.setBackground(new Color(255, 255, 255));
		scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
		return scroll;
	}

	/**  
	 * Creates a window listener so that on close the user is asked
	 * if they want to save the unsaved changes.
	 * 
	 * @return exit listener
	 */
	private WindowListener createExitListener() {
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
		return exitListener;
	}

	/** 
	 * Creates simple JTextAreas for the tabs in the help page
	 * @param text The text to be displayed
	 * @return the JTextArea with the text inside
	 */
	private JTextArea makeTextArea(String text) {
		JTextArea area = new JTextArea(15, 50);
		area.setText(text);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setEditable(false);
		area.setOpaque(false);
		return area;
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

	/**
	 * Sets the look and feel of the application to Nimbus
	 */
	private static void setNimbusLookAndFeel() {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					updateUIElements();
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			// If Nimbus is not available, set to System look and feel
			setNativeLookAndFeel();
		}
	}

	/**
	 * Sets the look and feel of the application to GTK+
	 */
	private static void setGTKLookAndFeel() {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("GTK+".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					updateUIElements();
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			// If Nimbus is not available, set to System look and feel
			setNativeLookAndFeel();
		}
	}

	/**
	 * Sets the look and feel of the application to GTK+
	 */
	private static void setChosenLookAndFeel(String lf) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (lf.equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					updateUIElements();
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			// If Nimbus is not available, set to System look and feel
			setNativeLookAndFeel();
		}
	}

	/**
	 * Sets the look and feel of the application to the System's native
	 */
	private static void setNativeLookAndFeel() {
		try {
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
			updateUIElements();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private JMenuBar createMenu() {
		JMenuBar menu = new JMenuBar();

		/* Project menu */
		JMenu projectMenu = new JMenu("Project");
		JMenuItem newItem = new JMenuItem("New");
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooseFileToOpen();
			}
		});
		projectMenu.add(newItem);

		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseFileToOpen();
			}
		});
		projectMenu.add(openItem);

		projectMenu.addSeparator();

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				saveTranslation();
			}
		});
		projectMenu.add(saveItem);

		projectMenu.addSeparator();

		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeCurrentTranslation();				
			}
		});
		projectMenu.add(closeItem);

		menu.add(projectMenu); 

		/* Edit menu */
		JMenu editMenu = new JMenu("Edit"); // TODO make it do something
		JMenuItem undoItem = new JMenuItem("Undo");
		editMenu.add(undoItem);
		JMenuItem redoItem = new JMenuItem("Redo");
		editMenu.add(redoItem);
		menu.add(editMenu);

		/* View menu */
		final JMenu viewMenu = new JMenu("View");
		JRadioButton nativeLFItem = 
				new JRadioButton("Native", false);
		nativeLFItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setNativeLookAndFeel();
				SwingUtilities.updateComponentTreeUI(frame);
				javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
				frame.pack();
			}
		});
		JRadioButton nimbusLFItem = 
				new JRadioButton("Nimbus", true); // TODO check if poss.
		nimbusLFItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setChosenLookAndFeel("Nimbus");
				SwingUtilities.updateComponentTreeUI(frame);
				javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
				frame.pack();
			}
		});
		viewMenu.add(nativeLFItem);
		viewMenu.add(nimbusLFItem);
		ButtonGroup views = new ButtonGroup();
		views.add(nativeLFItem);
		views.add(nimbusLFItem);
		menu.add(viewMenu);

		/* Help Menu */
		JMenu helpMenu = new JMenu("Help");
		menu.add(helpMenu);

		return menu;
	}

	/**
	 * Choose a file to start/resume a translation
	 */
	private void chooseFileToOpen() {
		closeCurrentTranslation();	

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
				loadOldTranslation(translationFile);
			} else if (sourceFile != null) {
				// read source for new translation
				loadNewTranslation(sourceFile);
			}
		}
	}

	/**
	 * Close current translation
	 */
	private void closeCurrentTranslation() {
		/* close open paragraphs */
		try {
			table.getCellEditor().stopCellEditing();
		} catch (Exception e) {
			// do nothing, sometimes there wasn't any open segment
		}

		/* reset the instance variables and all that */
		if (unsavedChanges) {
			showSaveOptionDialog();
		}
		clean();
	}

	private static void updateUIElements() {
		try {
			btnSave.setIcon(
					UIManager.getIcon("FileView.floppyDriveIcon"));
			btnChooseSource.setIcon(
					UIManager.getIcon("FileView.directoryIcon"));
			centerTableHeader();
		} catch (Exception e) {
			// do nothing if there are is no table or no buttons yet
		}
	}

	/**
	 * Center the text in the header
	 */
	private static void centerTableHeader() {
		DefaultTableCellRenderer headerRenderer = 
				(DefaultTableCellRenderer) 
				table.getTableHeader().getDefaultRenderer();
		headerRenderer.setHorizontalAlignment(JLabel.CENTER);
		// TODO Make it work also after switching to native
	}
}
