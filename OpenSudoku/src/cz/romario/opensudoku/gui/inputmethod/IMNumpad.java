/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package cz.romario.opensudoku.gui.inputmethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.CellCollection;
import cz.romario.opensudoku.game.CellGroup;
import cz.romario.opensudoku.game.CellNote;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.game.CellCollection.OnChangeListener;
import cz.romario.opensudoku.gui.HintsQueue;
import cz.romario.opensudoku.gui.SudokuBoardView;
import cz.romario.opensudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;

public class IMNumpad extends InputMethod {

	private boolean moveCellSelectionOnPress = true;
	private boolean mHighlightCompletedValues = true;
	private boolean mShowNumberTotals = false;

	private static final int MODE_EDIT_VALUE = 0;
	private static final int MODE_EDIT_NOTE = 1;

	private Cell mSelectedCell;
	private CellNote mNote;
	private CellGroup mSector;
	private CellGroup mRow;
	private CellGroup mColumn;
	private ArrayList<Integer> notelist;
	private ImageButton mSwitchNumNoteButton;

	private int mEditMode = MODE_EDIT_VALUE;

	private Map<Integer, Button> mNumberButtons;
	private Map<Integer, Button> mNoteButtons;

	public boolean isMoveCellSelectionOnPress() {
		return moveCellSelectionOnPress;
	}

	public void setMoveCellSelectionOnPress(boolean moveCellSelectionOnPress) {
		this.moveCellSelectionOnPress = moveCellSelectionOnPress;
	}

	public boolean getHighlightCompletedValues() {
		return mHighlightCompletedValues;
	}

	/**
	 * If set to true, buttons for numbers, which occur in
	 * {@link CellCollection} more than {@link CellCollection#SUDOKU_SIZE}
	 * -times, will be highlighted.
	 * 
	 * @param highlightCompletedValues
	 */
	public void setHighlightCompletedValues(boolean highlightCompletedValues) {
		mHighlightCompletedValues = highlightCompletedValues;
	}

	public boolean getShowNumberTotals() {
		return mShowNumberTotals;
	}

	public void setShowNumberTotals(boolean showNumberTotals) {
		mShowNumberTotals = showNumberTotals;
	}

	@Override
	protected void initialize(Context context, IMControlPanel controlPanel,
			SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
		super.initialize(context, controlPanel, game, board, hintsQueue);

		game.getCells().addOnChangeListener(mOnCellsChangeListener);
	}

	@Override
	protected View createControlPanelView() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View controlPanel = inflater.inflate(R.layout.im_numpad, null);

		mNumberButtons = new HashMap<Integer, Button>();
		mNumberButtons
				.put(1, (Button) controlPanel.findViewById(R.id.button_1));
		mNumberButtons
				.put(2, (Button) controlPanel.findViewById(R.id.button_2));
		mNumberButtons
				.put(3, (Button) controlPanel.findViewById(R.id.button_3));
		mNumberButtons
				.put(4, (Button) controlPanel.findViewById(R.id.button_4));
		mNumberButtons
				.put(5, (Button) controlPanel.findViewById(R.id.button_5));
		mNumberButtons
				.put(6, (Button) controlPanel.findViewById(R.id.button_6));
		mNumberButtons
				.put(7, (Button) controlPanel.findViewById(R.id.button_7));
		mNumberButtons
				.put(8, (Button) controlPanel.findViewById(R.id.button_8));
		mNumberButtons
				.put(9, (Button) controlPanel.findViewById(R.id.button_9));
		mNumberButtons.put(0,
				(Button) controlPanel.findViewById(R.id.button_clear));
		mNoteButtons = new HashMap<Integer, Button>();
		mNoteButtons.put(0, (Button) controlPanel.findViewById(R.id.button_S));
		mNoteButtons.put(1, (Button) controlPanel.findViewById(R.id.button_R));
		mNoteButtons.put(2, (Button) controlPanel.findViewById(R.id.button_C));
		mNoteButtons.put(3, (Button) controlPanel.findViewById(R.id.button_Se));
		for (Integer num : mNumberButtons.keySet()) {
			Button b = mNumberButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(mNumberButtonClick);
		}
		for (Integer num : mNoteButtons.keySet()) {
			Button b = mNoteButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(mNoteButtonClicked);
		}// ÉèÖÃ±Ê¼Ç¼àÌýÆ÷
		mSwitchNumNoteButton = (ImageButton) controlPanel
				.findViewById(R.id.switch_num_note);
		mSwitchNumNoteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE
						: MODE_EDIT_VALUE;
				update();
			}

		});

		return controlPanel;

	}

	@Override
	public int getNameResID() {
		return R.string.numpad;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_numpad_hint;
	}

	@Override
	public String getAbbrName() {
		return mContext.getString(R.string.numpad_abbr);
	}

	@Override
	protected void onActivated() {
		update();

		mSelectedCell = mBoard.getSelectedCell();
	}

	@Override
	protected void onCellSelected(Cell cell) {
		mSelectedCell = cell;
	}

	private OnClickListener mNumberButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int selNumber = (Integer) v.getTag();
			Cell selCell = mSelectedCell;

			if (selCell != null) {
				switch (mEditMode) {
				case MODE_EDIT_NOTE:
					if (selNumber == 0) {
						mGame.setCellNote(selCell, CellNote.EMPTY);
					} else if (selNumber > 0 && selNumber <= 9) {
						mGame.setCellNote(selCell, selCell.getNote()
								.toggleNumber(selNumber));
					}
					break;
				case MODE_EDIT_VALUE:
					if (selNumber >= 0 && selNumber <= 9) {
						mGame.setCellValue(selCell, selNumber);
						if (isMoveCellSelectionOnPress()) {
							mBoard.moveCellSelectionRight();
						}
					}
					break;
				}
			}
		}

	};

	private OnClickListener mNoteButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int selNote = (Integer) v.getTag();
			Cell selCell = mSelectedCell;
			mSector = selCell.getSector();
			mRow = selCell.getRow();
			mColumn = selCell.getColumn();
			if (selCell != null) {
				switch (selNote) {
				case 0:
					singleNote(selCell);
					break;
				case 1:
					rowNote();
					break;
				case 2:
					columnNote();
					break;
				case 3:
					sectorNote();
					break;
				}
			}
		}
	};

	private OnChangeListener mOnCellsChangeListener = new OnChangeListener() {

		@Override
		public void onChange() {
			if (mActive) {
				update();
			}
		}
	};

	private void singleNote(Cell cell) {
		mNote = new CellNote();
		mSector = cell.getSector();
		mRow = cell.getRow();
		mColumn = cell.getColumn();
		initNote();
		ArrayList<Integer> containNum=new ArrayList<Integer>();
		for (int i = 0; i < notelist.size(); i++) {
			if (mSector.contains(notelist.get(i)))
				if(containNum.indexOf(notelist.get(i))==-1)
					containNum.add(notelist.get(i));
		}
		for (int i = 0; i < notelist.size(); i++) {
			if (mRow.contains(notelist.get(i)))
				if(containNum.indexOf(notelist.get(i))==-1)
					containNum.add(notelist.get(i));
		}
		for (int i = 0; i < notelist.size(); i++) {
			if (mColumn.contains(notelist.get(i)))
				if(containNum.indexOf(notelist.get(i))==-1)
					containNum.add(notelist.get(i));
		}
		for(int i=0;i<containNum.size();i++){
			if(notelist.indexOf(containNum.get(i))!=-1)
				notelist.remove(notelist.indexOf(containNum.get(i)));
		}
		createNote();
		cell.setNote(mNote);
	}

	private void rowNote() {
		Cell[] mCells = mRow.getCellGroup();
		for (int i = 0; i < CellCollection.SUDOKU_SIZE; i++) {
			if (mCells[i].getValue() == 0) {
				singleNote(mCells[i]);
			}
		}
	}

	private void columnNote() {
		Cell[] mCells = mColumn.getCellGroup();
		for (int i = 0; i < CellCollection.SUDOKU_SIZE; i++) {
			if (mCells[i].getValue() == 0) {
				singleNote(mCells[i]);
			}
		}
	}

	private void sectorNote() {
		Cell[] mCells = mSector.getCellGroup();
		for (int i = 0; i < CellCollection.SUDOKU_SIZE; i++) {
			if (mCells[i].getValue() == 0) {
				singleNote(mCells[i]);
			}
		}
	}

	private void initNote() {
		notelist = new ArrayList<Integer>();
		for (int i = 1; i <= 9; i++) {
			notelist.add(i);
		}
	}

	private void createNote() {
		for (int i = 0; i < notelist.size(); i++) {
			mNote = mNote.toggleNumber(notelist.get(i));
		}
	}

	private void update() {
		switch (mEditMode) {
		case MODE_EDIT_NOTE:
			mSwitchNumNoteButton.setImageResource(R.drawable.pencil);
			break;
		case MODE_EDIT_VALUE:
			mSwitchNumNoteButton.setImageResource(R.drawable.pencil_disabled);
			break;
		}

		Map<Integer, Integer> valuesUseCount = null;
		if (mHighlightCompletedValues || mShowNumberTotals)
			valuesUseCount = mGame.getCells().getValuesUseCount();

		if (mHighlightCompletedValues) {
			for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
				boolean highlightValue = entry.getValue() >= CellCollection.SUDOKU_SIZE;
				Button b = mNumberButtons.get(entry.getKey());
				if (highlightValue) {
					b.setBackgroundResource(R.drawable.btn_completed_bg);
				} else {
					b.setBackgroundResource(R.drawable.btn_default_bg);
				}
			}
		}

		if (mShowNumberTotals) {
			for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
				Button b = mNumberButtons.get(entry.getKey());
				b.setText(entry.getKey() + " (" + entry.getValue() + ")");
			}
		}
	}

	@Override
	protected void onSaveState(StateBundle outState) {
		outState.putInt("editMode", mEditMode);
	}

	@Override
	protected void onRestoreState(StateBundle savedState) {
		mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
		if (isInputMethodViewCreated()) {
			update();
		}
	}
}
