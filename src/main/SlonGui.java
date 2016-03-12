package main;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;

import java.awt.Color;

import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import table.MultiLineCellTable;
import table.MultiLineTableCellEditor;
import table.MultiLineTableCellRenderer;
import table.TableCellListener;
import tools.Help;

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
				slon.chooseFileToOpen(
						table, btnSave, saveItem, btnClose, closeItem);
			}
		});
		return btnChooseSource;
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
				slon.chooseFileToOpen(
						table, btnSave, saveItem, btnClose, closeItem);
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
				slon.closeCurrentTranslation(
						table, btnSave, saveItem, btnClose, closeItem);
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
					slon.showSaveOptionDialog(table, btnSave, saveItem);
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

		/* Edit menu */
		JMenu editMenu = createMenuEdit();
		menu.add(editMenu);

		/* View menu */
		JMenu viewMenu = createMenuView();
		menu.add(viewMenu);

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
			@Override
			public void actionPerformed(ActionEvent arg0) {
				slon.chooseFileToOpen(
						table, btnSave, saveItem, btnClose, closeItem);
			}
		});
		projectMenu.add(newItem);

		openItem = new JMenuItem("Open");
		openItem.setIcon(
				UIManager.getIcon(OPEN_ICON_NAME));
		openItem.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				slon.chooseFileToOpen(
						table, btnSave, saveItem, btnClose, closeItem);
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
				slon.closeCurrentTranslation(
						table, btnSave, saveItem, btnClose, closeItem);				
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
}
