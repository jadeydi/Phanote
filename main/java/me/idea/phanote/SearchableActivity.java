package me.idea.phanote;

import me.idea.phanote.provider.NoteBase;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

public class SearchableActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String[] PROJECTION = new String[] {
			NoteBase.Note._ID, NoteBase.Note.COLUMN_NAME_TITLE,
			NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE };
	private static final Uri CONTENT_URI = NoteBase.Note.CONTENT_URI;
	private static final String KEY = "query";

	private SimpleCursorAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] dataColumns = { NoteBase.Snippet.COLUMN_NAME_TITLE };
		int[] viewIds = { R.id.content_item };
		mAdapter = new SimpleCursorAdapter(this, R.layout.content_item, null,
				dataColumns, viewIds,
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		setListAdapter(mAdapter);
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			Bundle bundle = new Bundle();
			bundle.putString(KEY, query);
			getLoaderManager().initLoader(0, bundle, this);
		} else {
			getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.searchable, menu);

		// set action bar search info
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_searchable).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);
		Intent intent = new Intent(this, NoteEditor.class);
		intent.setData(uri);
		intent.setAction(Intent.ACTION_EDIT);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (bundle != null) {
			String selection = "title LIKE ?";
			String[] selectionArgs = { "%" + bundle.getString(KEY) + "%" };
			
			return new CursorLoader(this, NoteBase.Note.CONTENT_URI, PROJECTION,
					selection, selectionArgs, NoteBase.Note.DEFAULT_SORT_ORDER);
		} else {
			return new CursorLoader(this, NoteBase.Note.CONTENT_URI, PROJECTION,
					null, null, NoteBase.Note.DEFAULT_SORT_ORDER);
		}
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