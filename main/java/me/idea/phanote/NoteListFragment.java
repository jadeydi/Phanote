package me.idea.phanote;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;

import me.idea.phanote.provider.NoteBase;

public class NoteListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = "NoteListFragment";
	private static final String[] PROJECTION = new String[] {
			NoteBase.Note._ID, NoteBase.Note.COLUMN_NAME_TITLE,
			NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE, NoteBase.Note.COLUMN_NAME_STATUS };
	private static final Uri CONTENT_URI = NoteBase.Note.CONTENT_URI;

	private NoteCursorAdapter mAdapter;
	private Context mContext;

	private long mPos = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getActivity().setTitle("Notes");
		mContext = getActivity();

		getListView().setOnCreateContextMenuListener(this);

		mAdapter = new NoteCursorAdapter(mContext, null,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		setListAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this);

	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_note_menu, menu);
    }

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_note:
                Intent intent = new Intent(mContext, NoteEditor.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.setData(NoteBase.Note.CONTENT_URI);
                startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		editNote(id);
	}

	private void editNote(long id) {
		Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);
		Intent intent = new Intent(mContext, NoteEditor.class);
		intent.setData(uri);
		intent.setAction(Intent.ACTION_EDIT);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new CursorLoader(mContext, CONTENT_URI, PROJECTION, null, null,
				NoteBase.Note.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

}