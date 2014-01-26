package me.idea.phanote;

import me.idea.phanote.provider.NoteBase;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TitleEditor extends Activity implements OnClickListener {
	private static final String[] PROJECTION = { NoteBase.Note._ID,
			NoteBase.Note.COLUMN_NAME_TITLE };
	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;

	private Uri mUri;
	private EditText mText;
	private int mState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		if (intent.getAction().equals(Intent.ACTION_INSERT)) {
			mState = STATE_INSERT;
		} else if (intent.getAction().equals(Intent.ACTION_EDIT)) {
			mState = STATE_EDIT;
		}

		mUri = intent.getData();

		setContentView(R.layout.title_editor);

		mText = (EditText) findViewById(R.id.note_title);
		Button button = (Button) findViewById(R.id.title_ok);
		button.setOnClickListener(this);

		Cursor cursor = getContentResolver().query(mUri, PROJECTION, null,
				null, NoteBase.Note.DEFAULT_SORT_ORDER);

		if (mState == STATE_EDIT) {
			cursor.moveToFirst();
			int columnTitleIndex = cursor
					.getColumnIndex(NoteBase.Note.COLUMN_NAME_TITLE);
			mText.setText(cursor.getString(columnTitleIndex));
		}
	}

	private void setTitle(String title) {
		ContentValues values = new ContentValues();
		values.put(NoteBase.Note.COLUMN_NAME_TITLE, title);

		if (mState == STATE_INSERT) {
			Intent intent = new Intent(this, NoteEditor.class);
			intent.setData(getContentResolver().insert(mUri, values));
			startActivity(intent);
		} else if (mState == STATE_EDIT) {
			getContentResolver().update(mUri, values, null, null);
		}
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_ok:
			String text = mText.getText().toString();
			if (text.equals("")) {
				Toast.makeText(this, "Note title can't blank?",
						Toast.LENGTH_SHORT).show();
			} else {
				setTitle(text);
			}
		}
	}
}
