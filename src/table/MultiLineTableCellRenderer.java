package table;
/**
 * Modified from 
 * http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
 */

import java.awt.Color;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import tools.Indentation;

/**
 * Multiline Table Cell Renderer.
 */
public class MultiLineTableCellRenderer
							extends JTextArea implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	private ArrayList<ArrayList<Integer>> rowColHeight = 
										new ArrayList<ArrayList<Integer>>();

	private Indentation indentation = new Indentation();
	
	// TODO make it the same as in SlonGui;
	private Color mainColor = new Color(57,105,138); 

	public MultiLineTableCellRenderer() {
		setLineWrap(true);
		setWrapStyleWord(true);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		// The coments column
		if (column == 2) {
			if (isSelected) {
				setForeground(Color.GRAY);
				// the background shouldn't be like the source column's
				setBackground(UIManager.getColor("Table.focusCellBackground")); 
			} else {
				setForeground(Color.GRAY);
				setBackground(Color.WHITE);
			}
			setFont(table.getFont());
			if (hasFocus) {
				setBorder(
						UIManager.getBorder("Table.focusCellHighlightBorder"));
				if (table.isCellEditable(row, column)) {
					setForeground(Color.GRAY);
					setBackground(
							UIManager.getColor("Table.focusCellBackground"));
				}
			} else {
				setBorder(new EmptyBorder(1, 2, 1, 2));
			}
			if (value != null) {
				setText(value.toString());
			} else {
				setText("");
			}
			/* the source and target columns */
		} else {
			if (isSelected) {
				setForeground(table.getForeground());
				setBackground(UIManager.getColor("Table.focusCellBackground"));
			} else {
				setForeground(mainColor);
				setBackground(Color.WHITE);
			}
			setFont(table.getFont());
			if (hasFocus) {
				setBorder(
						UIManager.getBorder("Table.focusCellHighlightBorder"));
				if (table.isCellEditable(row, column)) {
					setForeground(mainColor);
					setBackground(
							UIManager.getColor("Table.focusCellBackground"));
				}
			} else {
				setBorder(new EmptyBorder(1, 2, 1, 2));
			}
			if (value != null) {
				setText(indentation.indent(value.toString()));
			} else {
				setText("");
			}
		}
		adjustRowHeight(table, row, column);
		return this;
	}

	/**
	 * Calculates the new preferred height for a given row,
	 * and sets the height on the table.
	 */
	private void adjustRowHeight(JTable table, int row, int column) {
		/* The trick to get this to work properly is to set the width 
		 * of the column to the textarea. The reason for this is that 
		 * getPreferredSize(), without a width tries to place all the text 
		 * in one line. By setting the size with the with of the column,
		 * getPreferredSize() returns the proper height 
		 * which the row should have in order to make room for the text.
		 */
		int cWidth = table.
				getTableHeader().getColumnModel().getColumn(column).getWidth();
		setSize(new Dimension(cWidth, 1000));
		int prefH = getPreferredSize().height;
		while (rowColHeight.size() <= row) {
			rowColHeight.add(new ArrayList<Integer>(column));
		}
		ArrayList<Integer> colHeights = rowColHeight.get(row);
		while (colHeights.size() <= column) {
			colHeights.add(0);
		}
		colHeights.set(column, prefH);
		int maxH = prefH;
		for (Integer colHeight : colHeights) {
			if (colHeight > maxH) {
				maxH = colHeight;
			}
		}
		if (table.getRowHeight(row) != maxH) {
			table.setRowHeight(row, maxH);
		}
	}
}