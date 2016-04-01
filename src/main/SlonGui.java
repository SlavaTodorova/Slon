package main;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import javax.swing.JButton;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import elements.ProjectView;
import table.MultiLineCellTable;
import table.MultiLineTableCellEditor;
import table.MultiLineTableCellRenderer;
import table.TableCellListener;
import tools.Help;
import tools.Project;
import tools.Utils;

import java.awt.Cursor;

public class SlonGui {

	private JFrame frame;
	private static JTable table;

	private Slon slon;

	private static JButton btnSave;
	private static JButton btnClose;
	private static JButton btnOpenProject;
	private static JButton btnNewProject;

	private static JMenuItem saveItem;
	private static JMenuItem newItem;
	private static JMenuItem openItem;
	private static JMenuItem closeItem;

	private Color mainColor; // TODO make it dependent on the user preferences

	private final static String NEW_ICON_NAME = "FileChooser.newFolderIcon";
	private final static String OPEN_ICON_NAME = "Tree.openIcon";
	private final static String SAVE_ICON_NAME = "FileView.floppyDriveIcon";
	private final static String CLOSE_ICON_NAME = "FileView.directoryIcon";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
			throws IOException, ClassNotFoundException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setChosenLookAndFeel("Nimbus"); // if not available - native
				/* Some adjustments to the look and feel */
				UIManager.put("control", Color.white); // for nimbus
				UIManager.put("OptionPane.background",  Color.white); // native
				UIManager.put("Panel.background",  Color.white); // native
				/* Launch the application */
				SlonGui window = new SlonGui();
				window.frame.setVisible(true);
			}
		});
	}

	/**
	 * Creates the application.
	 */
	public SlonGui() {

		/* The core of the program */
		slon = new Slon();

		/* Color theme */
		// TODO move it to some other class
		mainColor = new Color(57,105,138); // TODO allow for user preferences

		frame = createFrame();

		/* Table */
		frame.getContentPane().add(createTable(), BorderLayout.CENTER);

		/* Menubar */
		JMenuBar menuBar = createMenuBar();

		/* Control Panel */
		JPanel controlPanel = new JPanel((LayoutManager) new BorderLayout());
		controlPanel.setBackground(mainColor);

		/* Menu Bar */
		controlPanel.add(menuBar, BorderLayout.NORTH);

		/* Icon Panel */
		JPanel iconsPanel = new JPanel(
				(LayoutManager) new FlowLayout(FlowLayout.LEFT));
		iconsPanel.setBackground(mainColor);
		controlPanel.add(iconsPanel, BorderLayout.EAST);
		frame.getContentPane().add(controlPanel, BorderLayout.NORTH);

		btnOpenProject = createButtonOpen();
		iconsPanel.add(btnOpenProject);

		btnNewProject = createButtonNew();
		iconsPanel.add(btnNewProject);

		btnSave = createButtonSave();
		iconsPanel.add(btnSave);

		btnClose = createButtonClose();
		iconsPanel.add(btnClose);

		/* Exit Listener */
		frame.addWindowListener(createExitListener());

		/* What does this one do? */ 
		frame.pack();

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
	 * Creates the "Open" button
	 * which allows the user to load a source text file
	 * 
	 * @return "Open" button 
	 */
	private JButton createButtonOpen() {
		JButton btnChooseSource = new JButton(
				UIManager.getIcon(OPEN_ICON_NAME));
		btnChooseSource.setToolTipText("Create new project");
		btnChooseSource.setToolTipText("Open an existing project");
		btnChooseSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openProject(new File(System.getProperty("user.home")));
				btnClose.setEnabled(true);
				closeItem.setEnabled(true);
			}
		});
		return btnChooseSource;
	}

	private void openProject(File currentDir) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(currentDir); // TODO Preferences!
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setFileView(new ProjectView());
		int result = chooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedDir = chooser.getSelectedFile();
			if (isProject(selectedDir)) {
				closeCurrentTranslation();
				slon.resumeProject(selectedDir, table);
			} else {
				openProject(selectedDir);
			}
		}
	}

	private boolean isProject(File dir) {
		File[] files = dir.listFiles();
		boolean hasSource = false;
		boolean hasTarget = false;
		boolean hasSlon = false;
		for (int i=0; i<files.length; i++) {
			String fileName = files[i].getName();
			switch(fileName) {
			case "source.txt":	hasSource = true;
			break;
			case "target.txt":	hasTarget = true;
			break;
			case "translation.slon":	hasSlon = true;
			break;
			default: break; // do nothing
			}
		}
		return hasSource && hasTarget && hasSlon;
	}
	/**
	 * Close current translation
	 * TODO move this method to the GUI file
	 * @param table the translation table
	 * @param btnSave the "Save" button
	 * @param saveItem the menu item "Save"
	 * @param btnClose the "Close" button
	 * @param closeItem the menu item "Close"
	 */
	private void closeCurrentTranslation() {
		/* close open paragraphs */
		try {
			table.getCellEditor().stopCellEditing();
		} catch (Exception e) {
			// do nothing, sometimes there wasn't any open segment
		}

		/* reset the instance variables and all that */
		if (slon.unsavedChanges) {
			showSaveOptionDialog();
		}
		clean();
	}

	/**
	 * Shows a dialog that gives the user the option to save the translation
	 * TODO move to the GUI file
	 * @param table the translation table
	 * @param btnSave the "Save" button
	 * @param saveItem the menu item "Save"
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
			slon.saveTranslation(table);
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
			slon.unsavedChanges = false;
		}
	}

	/**
	 * Cleans the translation table and the list of Paragraphs.
	 * @param table the translation table
	 * @param btnClose the "Close" button
	 * @param closeItem the menu item "Close"
	 */
	private void clean() { // TODO maka a "clean" method in the Slon class
		slon.project = new Project();
		DefaultTableModel tbModel = (DefaultTableModel) table.getModel();
		if (slon.paragraphs != null) {
			for (int i = slon.paragraphs.size()-1; i >= 0; i--) {
				tbModel.removeRow(i); // clean the displayed table
			}
		} else {
			while (table.getRowCount() > 0) {
				tbModel.removeRow(0);
			}
		}
		slon.paragraphs = null;
		btnClose.setEnabled(false);
		closeItem.setEnabled(false);
	}

	/**
	 * Creates the "New project" button
	 * which allows the user to load a source text file
	 * 
	 * @return "New project" button 
	 */
	private JButton createButtonNew() {
		JButton btnChooseSource = new JButton(
				UIManager.getIcon(NEW_ICON_NAME));
		btnChooseSource.setToolTipText("Create new project");
		btnChooseSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Project theProject = null;
				try {
					theProject = createProject(
							System.getProperty("user.home"), "", "", false, "", true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (theProject != null) {
						closeCurrentTranslation();
						slon.startProject(theProject, table);
						btnClose.setEnabled(true);
						closeItem.setEnabled(true);
					}
				}
			}
		});
		return btnChooseSource;
	}

	/**
	 * Creates the "Save" button
	 * which allows the user to save their current translation
	 * 
	 * @return "Save" button 
	 */
	private JButton createButtonSave() {
		JButton btn = new JButton(
				UIManager.getIcon(SAVE_ICON_NAME));
		btn.setToolTipText("Save current project");
		btn.setEnabled(false);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				slon.saveTranslation(table); // write updated translation
				btnSave.setEnabled(false);
				saveItem.setEnabled(false);
			}
		});
		return btn;
	}

	/**
	 * Creates the "Close project" button
	 * which closes the current translation
	 * 
	 * @return "Close project" button 
	 */
	private JButton createButtonClose() {
		JButton btn = new JButton(
				UIManager.getIcon(CLOSE_ICON_NAME));
		btn.setToolTipText("Close current project");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeCurrentTranslation();
			}
		});
		btn.setEnabled(false);
		return btn;
	}

	/**
	 * Creates the table and puts it into a scroll pane
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
		// remove the default row in the model
		while (tbModel.getRowCount() > 0) {
			tbModel.removeRow(0);
		}
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
						slon.unsavedChanges = true;
						btnSave.setEnabled(true);
						saveItem.setEnabled(true);
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
				if (slon.unsavedChanges) {
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
	 * Sets the look and feel of the application
	 * @param lf the look and feel chosen by the user
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
			// If the chosen L&F is not available, set to System L&F
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the menu bar
	 * @return the menu bar
	 */
	private JMenuBar createMenuBar() {

		JMenuBar menu = new JMenuBar();

		/* Project menu */
		JMenu projectMenu = createMenuProject();
		menu.add(projectMenu); 

//		/* Edit menu */
//		JMenu editMenu = createMenuEdit();
//		menu.add(editMenu);
//
//		/* View menu */
//		JMenu viewMenu = createMenuView();
//		menu.add(viewMenu);

		/* Help Menu */
		JMenu helpMenu = createMenuHelp();
		menu.add(helpMenu);


		return menu;
	}

	/**
	 * Creates the Help menu (about, how to and license)
	 * @return the help menu
	 */
	private JMenu createMenuHelp() {

		JMenu helpMenu = new JMenu("Help");

		/* About */
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Help help = new Help();
				JLabel lable = new JLabel(help.about);
				JOptionPane.showMessageDialog(
						null, lable, "SLON Help Page",
						JOptionPane.INFORMATION_MESSAGE);		
			}
		});
		helpMenu.add(about);

		/* How to */
		JMenuItem howTo = new JMenuItem("How to");
		howTo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Help help = new Help();
				/* Create the tabbed panel */
				JTabbedPane tabbedPane = new JTabbedPane();

				JLabel labelNew = new JLabel(help.startNew);
				labelNew.setPreferredSize(new Dimension(
						frame.getPreferredSize().width/3, 
						frame.getPreferredSize().height/3));

				tabbedPane.addTab("How to start", null, labelNew,
						"Creating a new translation project");
				tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

				JLabel labelOpen = new JLabel(help.resumeOld);

				tabbedPane.addTab("Resume a translation", null, labelOpen,
						"Opening an existing project");
				tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

				JLabel labelSave = new JLabel(help.save);

				tabbedPane.addTab("Save a translation", null, labelSave,
						"Saving the current translation");
				tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

				JOptionPane.showMessageDialog(
						null, tabbedPane, "SLON Help Page",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		helpMenu.add(howTo);

		/* License */
		JMenuItem license = new JMenuItem("License");
		license.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Help help = new Help();
				final JEditorPane pane;
				pane = new JEditorPane("text/html", help.license);
				JOptionPane.showMessageDialog(
						null, pane, "SLON Help Page",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		helpMenu.add(license);

		return helpMenu;
	}

	/**
	 * Creates the View menu (Nimbus and Native)
	 * @return the view menu
	 */
	private JMenu createMenuView() {
		JMenu viewMenu = new JMenu("View");
		JRadioButtonMenuItem nativeLFItem = 
				new JRadioButtonMenuItem("Native", false);
		nativeLFItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setNativeLookAndFeel();
				SwingUtilities.updateComponentTreeUI(frame);
				javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
				frame.pack();
			}
		});
		JRadioButtonMenuItem nimbusLFItem = 
				new JRadioButtonMenuItem("Nimbus", true); // TODO check if poss.
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
		return viewMenu;
	}

	/**
	 * Creates edit menu (undo and redo)
	 * 
	 * @return the edit menu
	 */
	private JMenu createMenuEdit() {
		JMenu editMenu = new JMenu("Edit"); // TODO make it do something
		JMenuItem undoItem = new JMenuItem("Undo");
		editMenu.add(undoItem);
		JMenuItem redoItem = new JMenuItem("Redo");
		editMenu.add(redoItem);
		return editMenu;
	}

	/**
	 * Creates the project menu (new, open, save and close)
	 * @return the project menu
	 */
	private JMenu createMenuProject() {
		JMenu projectMenu = new JMenu("Project");
		newItem = new JMenuItem("New");
		newItem.setIcon(
				UIManager.getIcon(NEW_ICON_NAME));
		newItem.addActionListener(new ActionListener() { 
			// this action listener is common for two objects, 
			// TODO let them share it!
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Project theProject = null;
				try {
					theProject = createProject(
							System.getProperty("user.home"), "", "", false, "", true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (theProject != null) {
						closeCurrentTranslation();
						slon.startProject(theProject, table);
						btnClose.setEnabled(true);
						closeItem.setEnabled(true);
					}
				}
			}
		});
		projectMenu.add(newItem);

		openItem = new JMenuItem("Open");
		openItem.setIcon(
				UIManager.getIcon(OPEN_ICON_NAME));
		openItem.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				openProject(new File(System.getProperty("user.home")));
				btnClose.setEnabled(true);
				closeItem.setEnabled(true);
			}
		});
		projectMenu.add(openItem);

		projectMenu.addSeparator();

		saveItem = new JMenuItem("Save");
		saveItem.setIcon(
				UIManager.getIcon(SAVE_ICON_NAME));
		saveItem.setEnabled(false);
		saveItem.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				slon.saveTranslation(table);
				btnSave.setEnabled(false);
				saveItem.setEnabled(false);
			}
		});
		projectMenu.add(saveItem);

		projectMenu.addSeparator();

		closeItem = new JMenuItem("Close");
		closeItem.setIcon(
				UIManager.getIcon(CLOSE_ICON_NAME));
		closeItem.setEnabled(false);
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeCurrentTranslation();				
			}
		});
		projectMenu.add(closeItem);
		return projectMenu;
	}

	/**
	 * Updates the icons of the buttons to the once of the current L&F
	 */
	private static void updateUIElements() {
		try {
			btnSave.setIcon(
					UIManager.getIcon(SAVE_ICON_NAME));
			btnOpenProject.setIcon(
					UIManager.getIcon(OPEN_ICON_NAME));
			btnClose.setIcon(
					UIManager.getIcon(CLOSE_ICON_NAME));
			btnNewProject.setIcon(
					UIManager.getIcon(NEW_ICON_NAME));
			centerTableHeader();
			saveItem.setIcon(UIManager.getIcon(SAVE_ICON_NAME));
			openItem.setIcon(UIManager.getIcon(OPEN_ICON_NAME));
			closeItem.setIcon(UIManager.getIcon(CLOSE_ICON_NAME));
			newItem.setIcon(UIManager.getIcon(NEW_ICON_NAME));
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

	/**
	 * Create a new project
	 * TODO Мнооого трябва да се чисти тук...
	 * @throws IOException 
	 */
	private Project createProject(
			String location, String name, String source, 
			Boolean editingModeSelected, String target, Boolean firstTime)
					throws IOException {

		final Border defaultFieldBorder = new JTextField().getBorder();

		/* Show panel */

		JPanel options = new JPanel();
		options.setLayout(new GridLayout(10, 1));

		/* Location */

		final JLabel locationWarning = new JLabel(
				"Please, type in a valid folder name, or browse for one.");
		locationWarning.setForeground(Color.red);
		locationWarning.setVisible(false);

		options.add(locationWarning);

		JPanel locationPanel = new JPanel(new BorderLayout());
		JLabel locationLabel = new JLabel(
				makeBold("Select a location for your project:"));
		locationPanel.add(locationLabel, BorderLayout.NORTH);

		final JTextField locationField = new JTextField(location);
		//TODO User Preferences

		if (! firstTime) {
			checkFileName(locationField, locationWarning, defaultFieldBorder);
		}

		locationField.setForeground(Color.gray);
		locationField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				// do nothing
			}
			@Override
			public void focusGained(FocusEvent arg0) {
				locationField.setForeground(Color.black);
			}
		});



		JButton locationButton = new JButton("Browse");
		locationButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				locationWarning.setVisible(false);
				locationField.setBorder(defaultFieldBorder);
				try {
					locationField.setText(chooseProjectLocation().getAbsolutePath());
				} catch (Exception e1) {
					// no source file specified
					locationField.setText("");
				}
				checkFileName(
						locationField, locationWarning, defaultFieldBorder);
			}
		});
		locationPanel.add(locationField, BorderLayout.CENTER);
		locationPanel.add(locationButton, BorderLayout.EAST);
		options.add(locationPanel);

		/* Name */

		final JLabel nameWarning = new JLabel(
				"The project name can contain "
						+ "upper case and lowercase latin letters, and digits."); 
		// TODO check for special signs
		nameWarning.setForeground(Color.red);
		nameWarning.setVisible(false);
		options.add(nameWarning);

		JPanel namePanel = new JPanel(new BorderLayout());
		JLabel nameLabel = new JLabel(
				makeBold("Type a name for your project:"));
		namePanel.add(nameLabel, BorderLayout.NORTH);

		final JTextField nameField = new JTextField(name);

		if (! firstTime) {
			checkProjectName(location, nameField, nameWarning, defaultFieldBorder);
		}

		namePanel.add(nameField, BorderLayout.CENTER);
		options.add(namePanel);

		/* Source */

		final JLabel sourceWarning = new JLabel(
				"Please, type in a valid file name, or browse for one.");
		sourceWarning.setForeground(Color.red);
		sourceWarning.setVisible(false);
		options.add(sourceWarning);

		JPanel sourcePanel = new JPanel(new BorderLayout());
		JLabel sourceLabel = 
				new JLabel(makeBold("Choose a source file to translate:"));
		sourcePanel.add(sourceLabel, BorderLayout.NORTH);
		final JTextField sourceField = new JTextField(source);

		if (! firstTime) {
			checkFileName(sourceField, sourceWarning, defaultFieldBorder);
		}

		JButton sourceButton = new JButton("Browse");
		sourceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sourceWarning.setVisible(false);
				sourceField.setBorder(defaultFieldBorder);
				try {
					sourceField.setText(chooseSourceFile().getAbsolutePath());
				} catch (Exception e) {
					// no source file specified
					sourceField.setText("");
				}
				checkFileName(sourceField, sourceWarning, defaultFieldBorder);
			}
		});
		sourcePanel.add(sourceField, BorderLayout.CENTER);
		sourcePanel.add(sourceButton, BorderLayout.EAST);

		options.add(sourcePanel);

		/* Target Button and Text */
		final JButton targetButton = new JButton("Browse");
		final JLabel targetLabel = 
				new JLabel(makeBold("Choose a target file to edit:"));
		final JTextField targetField = new JTextField();

		/* Modes */
		JPanel modePanel = new JPanel(new BorderLayout());
		JLabel modeLabel = 
				new JLabel(makeBold("Choose the mode of the project:"));
		modePanel.add(modeLabel, BorderLayout.NORTH);
		JRadioButton translationMode = new JRadioButton("translation");
		translationMode.setSelected(! editingModeSelected);
		translationMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				targetButton.setEnabled(false);
				targetLabel.setForeground(Color.gray);
				targetField.setFocusable(false);
			}
		});
		JRadioButton editingMode = new JRadioButton(
				"translation editing (not available yet)");
		editingMode.setSelected(editingModeSelected);
		editingMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				targetButton.setEnabled(true);
				targetLabel.setForeground(Color.black);
				targetField.setFocusable(true);
			}
		});
		// TODO Make the following enabled and create the functionality!
		editingMode.setEnabled(false); 
		ButtonGroup modes = new ButtonGroup();
		modes.add(translationMode);
		modes.add(editingMode);
		modePanel.add(translationMode, BorderLayout.WEST);
		modePanel.add(editingMode, BorderLayout.CENTER);
		options.add(modePanel);

		/* Target */

		final JLabel targetWarning = new JLabel(
				"Please, type in a valid file name.");
		targetWarning.setForeground(Color.red);
		targetWarning.setVisible(false);

		targetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				targetWarning.setVisible(false);
				targetField.setBorder(defaultFieldBorder);
				try {
					targetField.setText(chooseSourceFile().getAbsolutePath());
				} catch (Exception e) {
					// no source file specified
					targetField.setText("");
				}
				checkFileName(targetField, targetWarning, defaultFieldBorder);
			}
		});

		options.add(targetWarning);

		targetButton.setEnabled(false);
		targetField.setFocusable(false);

		if (! firstTime && editingMode.isSelected()) {
			checkFileName(targetField, targetWarning, defaultFieldBorder);
		}

		JPanel targetPanel = new JPanel(new BorderLayout());
		targetLabel.setForeground(Color.gray);
		targetPanel.add(targetLabel, BorderLayout.NORTH);

		targetPanel.add(targetField, BorderLayout.CENTER);
		targetPanel.add(targetButton, BorderLayout.EAST);

		options.add(targetPanel);
		options.add(Box.createRigidArea(new Dimension(0,15)));;

		Object message = (Object) options;

		int option = JOptionPane.showConfirmDialog(null, message, 
				"SLON: Create new project", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION)
		{
			/* Checks */ 
			location = locationField.getText().trim();
			name = nameField.getText().trim();
			source = sourceField.getText().trim();
			target = targetField.getText().trim(); // TODO make it usable

			if (! new File(location).exists() 
					|| ! isAcceptableName(name) 
					|| existsProject(location, name)
					|| ! new File(source).exists()) {
				return createProject(location, name, source, 
						editingMode.isSelected(), target, false);
			}

			/* Create the project */
			Path projectPath = FileSystems.getDefault().getPath(location, name);
			Files.createDirectory(projectPath);

			Path sourcePath = FileSystems.getDefault().getPath(source);
			Path coppiedSourceFilePath = FileSystems.getDefault().getPath(
					projectPath.toString(), "source.txt");
			Files.copy(sourcePath, coppiedSourceFilePath);
			
			return new Project(projectPath, coppiedSourceFilePath.toFile());
		}
		return null;
	}

	private boolean isAcceptableName(String projectName) {
		if (projectName.equals("")) {
			return false;
		}
		return ! Pattern.matches(".*[^a-zA-Z0-9].*", projectName);		
	}

	private boolean existsProject(String location, String projectName) {
		String path = location 
				+ System.getProperty("file.separator") + projectName;
		return new File(path).exists();
	}

	private void checkFileName(
			JTextField field, JLabel warning, Border defaultBorder) {
		if (new File(field.getText()).exists()) {
			warning.setVisible(false);
			field.setBorder(defaultBorder);
		} else {
			warning.setVisible(true);
			field.setBorder(BorderFactory.createLineBorder(Color.red));
		}
	}

	private void checkProjectName(String location, 
			JTextField field, JLabel warning, Border defaultBorder) {
		String name = field.getText().trim();
		if (isAcceptableName(name) && ! existsProject(location, name)) {
			warning.setVisible(false);
			field.setBorder(defaultBorder);
		} else {
			warning.setVisible(true);
			field.setBorder(BorderFactory.createLineBorder(Color.red));
		}
	}

	private File chooseProjectLocation() {

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(
				new File(System.getProperty("user.home"))); // TODO Preferences!
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = chooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	private File chooseSourceFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(
				new File(System.getProperty("user.home"))); // TODO Preferences!
		chooser.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				// TODO Auto-generated method stub
				return ".txt";
			}

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				String extension = Utils.getExtension(f);
				if (extension != null) {
					if (extension.equals(Utils.txt)) {
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
			return chooser.getSelectedFile();
		}
		return null;
	}

	private String makeBold(String text) {
		return "<html><b>" + text + "</b></html>";
	}

}
