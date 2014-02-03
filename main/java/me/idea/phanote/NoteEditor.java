package me.idea.phanote;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import me.idea.phanote.provider.NoteBase;

public class NoteEditor extends FragmentActivity implements
        ConfirmDialogFragment.ConfirmDialogListener {
    private static final String TAG = "NoteEditor";
    private static final String[] PROJECTION = {NoteBase.Note._ID,
            NoteBase.Note.COLUMN_NAME_TITLE, NoteBase.Note.COLUMN_NAME_BODY, NoteBase.Note.COLUMN_NAME_STATUS};
    private static final int NEW_NOTE = 0X100;
    private static final int EDIT_NOTE = 0X101;
    private int mStatus;
    private int mNoteStatus = 0;
    private TextView mTitle;
    private EditText mBody;
    private Uri mUri;
    private Menu mMenu;
    private DialogFragment mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        mUri = intent.getData();

        mDialog = new ConfirmDialogFragment(NoteEditor.this, R.string.delete_note);
        setContentView(R.layout.note_editor);
        mBody = (EditText) findViewById(R.id.note_body);
        mTitle = (TextView) findViewById(R.id.note_title);

        if (intent.getAction().equals(Intent.ACTION_INSERT)) {
            newNote();
        } else {
            editNote();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_editor_menu, menu);

        mMenu = menu;

        if (getNoteStatus() == NEW_NOTE) {
            MenuItem item = menu.findItem(R.id.menu_active);
            if (item != null) {
                item.setVisible(false);
            }
        }

        if (isActivite()) {
            MenuItem item = menu.findItem(R.id.menu_active);
            if (item != null)
                item.setVisible(false);
        } else {
            MenuItem item = menu.findItem(R.id.menu_disactive);
            if (item != null)
                item.setVisible(false);
        }

        MenuItem item = menu.findItem(R.id.menu_share);
        if (item != null) {
            ShareActionProvider mShareActionProvider = (ShareActionProvider) item.getActionProvider();
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent());
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, ClipNote.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            case R.id.menu_save:

                String text = mBody.getText().toString();
                String title = mTitle.getText().toString();

                if (mStatus == EDIT_NOTE) {
                    updateNote(title, text);
                } else {
                    saveNote(title, text);
                }

                finish();
                return true;

            case R.id.menu_delete:

                if (mStatus == EDIT_NOTE) {
                    mDialog.show(getSupportFragmentManager(), "ConfirmDialogFragment");
                }
                return true;

            case R.id.menu_active:
                activeNote();

                mMenu.findItem(R.id.menu_disactive).setVisible(true);
                mMenu.findItem(R.id.menu_active).setVisible(false);

                return true;

            case R.id.menu_disactive:
                disactiveNote();
                mMenu.findItem(R.id.menu_disactive).setVisible(false);
                mMenu.findItem(R.id.menu_active).setVisible(true);

                return true;

            default:
                return true;
        }
    }

    private void saveNote(String title, String text) {
        getContentResolver().insert(mUri, createValues(title, text));
    }

    private final void updateNote(String title, String text) {
        getContentResolver().update(mUri, createValues(title, text), null, null);
    }

    private final void activeNote() {
        ContentValues values = new ContentValues();
        values.put(NoteBase.Note.COLUMN_NAME_STATUS, 1);

        getContentResolver().update(mUri, values, null, null);
    }

    private final void disactiveNote() {
        ContentValues values = new ContentValues();
        values.put(NoteBase.Note.COLUMN_NAME_STATUS, 0);

        getContentResolver().update(mUri, values, null, null);
    }

    private final void deleteNote() {
        getContentResolver().delete(mUri, null, null);
        mBody.setText("");
    }

    private ContentValues createValues(String title, String text) {
        ContentValues values = new ContentValues();

        if (title == null || title.isEmpty()) {
            title = getString(R.string.untitled);
        }

        values.put(NoteBase.Note.COLUMN_NAME_TITLE, title);
        values.put(NoteBase.Note.COLUMN_NAME_BODY, text);
        values.put(NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE,
                System.currentTimeMillis());

        return values;
    }

    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share from Phanote");
        intent.putExtra(Intent.EXTRA_TEXT, mBody.getText().toString());
        return intent;
    }

    private void newNote() {
        setNoteStatus(NEW_NOTE);
        setTitle(getString(R.string.untitled));
    }

    private void editNote() {
        setNoteStatus(EDIT_NOTE);

        Cursor cursor = getContentResolver().query(mUri, PROJECTION, null,
                null, NoteBase.Note.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();

        int titleIndex = cursor.getColumnIndex(NoteBase.Note.COLUMN_NAME_TITLE);
        String title = cursor.getString(titleIndex);
        String text = String.format(getString(R.string.note_edit_title), title);
        setTitle(text);
        mTitle.setTextKeepState(title);

        int colNoteIndex = cursor
                .getColumnIndex(NoteBase.Note.COLUMN_NAME_BODY);
        String note = cursor.getString(colNoteIndex);
        mBody.setTextKeepState(note);

        mNoteStatus = cursor.getInt(cursor.getColumnIndex(NoteBase.Note.COLUMN_NAME_STATUS));
    }

    private void setNoteStatus(int status) {
        mStatus = status;
    }

    private int getNoteStatus() {
        return mStatus;
    }

    private boolean isActivite() {
        return mNoteStatus == 1;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        deleteNote();
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

}