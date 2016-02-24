/**
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * @author Santhosh Kumar T
 * @email santhosh@fiorano.com
 *
 * Modified by Slava Todorova from 
 * http://www.jroller.com/santhosh/entry/multiline_in_table_cell_editing1
 */
  
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;

public class MultiLineTableCellEditor
		extends AbstractCellEditor implements TableCellEditor, ActionListener{
	private static final long serialVersionUID = 1L;

	public Indentation indentation = new Indentation();

	ResizableTextArea textArea = new ResizableTextArea() {
		private static final long serialVersionUID = 1L;
		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
		}
	};

	public MultiLineTableCellEditor(){
		textArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		textArea.registerKeyboardAction(this
				, KeyStroke.getKeyStroke("ENTER")
				, JComponent.WHEN_FOCUSED);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		/* Ctrl+Enter should produce a new line */
		InputMap input = textArea.getInputMap();
		ActionMap actions = textArea.getActionMap();
		KeyStroke ctrlEnter = KeyStroke.getKeyStroke(
				KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK);
		input.put(ctrlEnter, "new-line");
		actions.put("new-line", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.insert(
						"\n"+indentation.INDENT, textArea.getCaretPosition());
			}
		});
	}

	public Object getCellEditorValue(){
		return textArea.getText();
	}

	/*------------------------[ clickCountToStart ]--------------------------*/

	protected int clickCountToStart = 1;

	public int getClickCountToStart(){
		return clickCountToStart;
	}

	public void setClickCountToStart(int clickCountToStart){
		this.clickCountToStart = clickCountToStart;
	}

	public boolean isCellEditable(EventObject e){
		return !(e instanceof MouseEvent)
				|| ((MouseEvent)e).getClickCount()>=clickCountToStart;
	}

	/*----------------------------[ ActionListener ]------------------------*/

	public void actionPerformed(ActionEvent ae){
		stopCellEditing();
	}

	/*---------------------------[ TableCellEditor ]------------------------*/

	public Component getTableCellEditorComponent(
			JTable table, Object value,
			boolean isSelected,int row, int column){
		String text;
		if (value != null) {
			if (column == 2) {
				/* for comments, don't indent */
				text = value.toString();
			} else {
				/* for source and target columns
				 * add indentation to every first line
				 */
				text = indentation.indent(value.toString());
			}
		} else {
			text = ""; // TODO Check if INDENT is needed.
		}
		textArea.setText(text);
		return textArea;
	}



}