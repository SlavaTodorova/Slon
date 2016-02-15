import java.applet.Applet;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class MultiLineCellTable extends JTable {
	private static final long serialVersionUID = 1L;

	/*---------------------------------[ Constructors ]---------------------------------*/

	public MultiLineCellTable(){
	}

	public MultiLineCellTable(TableModel dm){
		super(dm);
	}

	public MultiLineCellTable(TableModel dm, TableColumnModel cm){
		super(dm, cm);
	}

	public MultiLineCellTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm){
		super(dm, cm, sm);
	}

	public MultiLineCellTable(int numRows, int numColumns){
		super(numRows, numColumns);
	}

	public MultiLineCellTable(Vector<?> rowData, Vector<?> columnNames){
		super(rowData, columnNames);
	}

	public MultiLineCellTable(final Object[][] rowData, final Object[] columnNames){
		super(rowData, columnNames);
	}

	/*-------------------------------------------------[ Cell Editing ]---------------------------------------------------*/

	public boolean editCellAt(int row, int column, EventObject e) {
		if(cellEditor!=null && !cellEditor.stopCellEditing()){
			return false;
		}

		if(row<0 || row>=getRowCount() ||
				column<0 || column>=getColumnCount()){
			return false;
		}

		if(!isCellEditable(row, column))
			return false;

		if(editorRemover==null){
			KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			editorRemover = new CellEditorRemover(fm);
			fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);
		}

		TableCellEditor editor = getCellEditor(row, column);
		if(editor!=null && editor.isCellEditable(e)){
			editorComp = prepareEditor(editor, row, column);
			if(editorComp==null){
				removeEditor();
				return false;
			}

			Rectangle cellRect = getCellRect(row, column, false);
			if(editor instanceof MultiLineTableCellEditor){
				Dimension prefSize = editorComp.getPreferredSize();
				editorComp.setBounds(cellRect.x, cellRect.y, Math.max(cellRect.width, prefSize.width), Math.max(cellRect.height, prefSize.height));
			} else {
				editorComp.setBounds(cellRect);
			}


			add(editorComp);
			editorComp.validate();

			setCellEditor(editor);
			setEditingRow(row);
			setEditingColumn(column);
			editor.addCellEditorListener(this);

			return true;
		}
		return false;
	}

	/*-------------------------------------------------[ Editor Remover ]---------------------------------------------------*/

	private PropertyChangeListener editorRemover = null;

	class CellEditorRemover implements PropertyChangeListener {
		KeyboardFocusManager focusManager;

		public CellEditorRemover(KeyboardFocusManager fm) {
			this.focusManager = fm;
		}

		public void propertyChange(PropertyChangeEvent ev) {
			if (!isEditing() || getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE) {
				return;
			}

			Component c = focusManager.getPermanentFocusOwner();
			while (c != null) {
				if (c == MultiLineCellTable.this) {
					// focus remains inside the table
					return;
				} else if ((c instanceof Window) ||
						(c instanceof Applet && c.getParent() == null)) {
					if (c == SwingUtilities.getRoot(MultiLineCellTable.this)) {
						if (!getCellEditor().stopCellEditing()) {
							getCellEditor().cancelCellEditing();
						}
					}
					break;
				}
				c = c.getParent();
			}
		}
	}

	public void removeNotify(){
		KeyboardFocusManager.getCurrentKeyboardFocusManager().
		removePropertyChangeListener("permanentFocusOwner", editorRemover);
		editorRemover = null;
		super.removeNotify();
	}

	public void removeEditor(){
		KeyboardFocusManager.getCurrentKeyboardFocusManager().
		removePropertyChangeListener("permanentFocusOwner", editorRemover);

		editorRemover = null;
		TableCellEditor editor = getCellEditor();
		if(editor!=null){
			editor.removeCellEditorListener(this);
			Rectangle cellRect = getCellRect(editingRow, editingColumn, false);
			if(editorComp!=null){
				cellRect = cellRect.union(editorComp.getBounds());
				remove(editorComp);
			}

			setCellEditor(null);
			setEditingColumn(-1);
			setEditingRow(-1);
			editorComp = null;

			repaint(cellRect);
		}
	}

	public boolean getScrollableTracksViewportHeight(){
		if(getParent() instanceof JViewport)
			return getParent().getHeight()>getPreferredSize().height;
			else
				return false;
	}
}