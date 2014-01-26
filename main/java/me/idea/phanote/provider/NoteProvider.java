package me.idea.phanote.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

import me.idea.phanote.R;

public class NoteProvider extends ContentProvider {
//	private static final String TAG = "ContentProvider";

	private static final String DATABASE_NAME = "phanote.db";

	// odd number if for snippet table; even number if for note table

	private static final UriMatcher sUriMatcher;
	private static final int NOTES = 1;
	private static final int NOTE_ID = 2;
	private static final int ACTIVITED_NOTES = 3;
	private static final int SNIPPETS = 10;
	private static final int SNIPPET_ID = 11;

	private static HashMap<String, String> mNoteProjectionMap;
	private static HashMap<String, String> mSnippetProjectionMap;

	private DatabaseHelper mOpenHelper;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(NoteBase.AUTHORITY, "notes", NOTES);
		sUriMatcher.addURI(NoteBase.AUTHORITY, "notes/activities", ACTIVITED_NOTES);
		sUriMatcher.addURI(NoteBase.AUTHORITY, "notes/#", NOTE_ID);
		sUriMatcher.addURI(NoteBase.AUTHORITY, "snippets", SNIPPETS);
		sUriMatcher.addURI(NoteBase.AUTHORITY, "snippets/#", SNIPPET_ID);

		mNoteProjectionMap = new HashMap<String, String>();
		mNoteProjectionMap.put(NoteBase.Note._ID, NoteBase.Note._ID);
		mNoteProjectionMap.put(NoteBase.Note.COLUMN_NAME_TITLE,
				NoteBase.Note.COLUMN_NAME_TITLE);
		mNoteProjectionMap.put(NoteBase.Note.COLUMN_NAME_BODY,
				NoteBase.Note.COLUMN_NAME_BODY);
		mNoteProjectionMap.put(NoteBase.Note.COLUMN_NAME_STATUS,
				NoteBase.Note.COLUMN_NAME_STATUS);
		mNoteProjectionMap.put(NoteBase.Note.COLUMN_NAME_CREATE_DATE,
				NoteBase.Note.COLUMN_NAME_CREATE_DATE);
		mNoteProjectionMap.put(NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE,
				NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE);

		mSnippetProjectionMap = new HashMap<String, String>();
		mSnippetProjectionMap.put(NoteBase.Snippet._ID, NoteBase.Snippet._ID);
		mSnippetProjectionMap.put(NoteBase.Snippet.COLUMN_NAME_TITLE,
				NoteBase.Snippet.COLUMN_NAME_TITLE);
		mSnippetProjectionMap.put(NoteBase.Snippet.COLUMN_NAME_CREATE_DATE,
				NoteBase.Snippet.COLUMN_NAME_CREATE_DATE);
		mSnippetProjectionMap.put(
				NoteBase.Snippet.COLUMN_NAME_MODIFICATION_DATE,
				NoteBase.Snippet.COLUMN_NAME_MODIFICATION_DATE);
	}

	static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String CREATE_NOTE_TABLE = "CREATE TABLE "
				+ NoteBase.Note.TABLE_NAME + " (" + NoteBase.Note._ID
				+ " INTEGER PRIMARY KEY," + NoteBase.Note.COLUMN_NAME_TITLE
				+ " VARCHAR," + NoteBase.Note.COLUMN_NAME_BODY + " TEXT,"
				+ NoteBase.Note.COLUMN_NAME_STATUS +" INTEGER DEFAULT 0,"
				+ NoteBase.Note.COLUMN_NAME_CREATE_DATE + " INTEGER,"
				+ NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE + " INTEGER"
				+ ");";

		private static final String CREATE_SNIPPET_TABLE = "CREATE TABLE "
				+ NoteBase.Snippet.TABLE_NAME + " (" + NoteBase.Note._ID
				+ " INTEGER PRIMARY KEY," + NoteBase.Snippet.COLUMN_NAME_TITLE
				+ " VARCHAR," + NoteBase.Snippet.COLUMN_NAME_CREATE_DATE
				+ " INTEGER," + NoteBase.Snippet.COLUMN_NAME_MODIFICATION_DATE
				+ " INTEGER" + ");";

		private static final String SNIPPET_TABLE_DELETE = "DROP TABLE IF EXISTS "
				+ NoteBase.Snippet.TABLE_NAME;

		private static final String NOTE_TABLE_DELETE = "DROP TABLE IF EXISTS "
				+ NoteBase.Note.TABLE_NAME;

        // 2013-10-20 v11 14-1-17 v13
	    private static final int DATABASE_VERSION = 13;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_NOTE_TABLE);
			db.execSQL(CREATE_SNIPPET_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		    db.execSQL("ALTER TABLE notes ADD COLUMN status INTEGER DEFAULT 0;");
		}
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		int type = sUriMatcher.match(uri);

		switch (type) {
		case NOTES:
			qb.setTables(NoteBase.Note.TABLE_NAME);
			qb.setProjectionMap(mNoteProjectionMap);
			break;

		case NOTE_ID:
			qb.setTables(NoteBase.Note.TABLE_NAME);
			qb.setProjectionMap(mNoteProjectionMap);
			qb.appendWhere(NoteBase.Note._ID + "="
					+ uri.getPathSegments().get(NoteBase.POSITION_OF_COLUMN_ID));
			break;

	    case ACTIVITED_NOTES:
            qb.setTables(NoteBase.Note.TABLE_NAME);
            qb.setProjectionMap(mNoteProjectionMap);
            qb.appendWhere(NoteBase.Note.COLUMN_NAME_STATUS + "= 1" );
            break;

		case SNIPPETS:
			qb.setTables(NoteBase.Snippet.TABLE_NAME);
			qb.setProjectionMap(mSnippetProjectionMap);
			break;
		case SNIPPET_ID:
			qb.setTables(NoteBase.Snippet.TABLE_NAME);
			qb.setProjectionMap(mSnippetProjectionMap);
			qb.appendWhere(NoteBase.Snippet._ID + "="
					+ uri.getPathSegments().get(
							NoteBase.POSITION_OF_COLUMN_ID));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		StringBuffer orderBy = new StringBuffer();
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy.append(NoteBase.Note.DEFAULT_SORT_ORDER);
		} else {
			orderBy = new StringBuffer(sortOrder);
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs,
				null, null, orderBy.toString());
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case NOTES:
			return NoteBase.Note.CONTENT_TYPE;
		case NOTE_ID:
			return NoteBase.Note.CONTENT_ITEM_TYPE;
		case SNIPPETS:
			return NoteBase.Snippet.CONTENT_TYPE;
		case SNIPPET_ID:
			return NoteBase.Snippet.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		int type = sUriMatcher.match(uri);
		if (type != NOTES && type != SNIPPETS)
			throw new IllegalArgumentException("Unknown URI" + uri);

		ContentValues values;
		if (initialValues == null) {
			values = new ContentValues();
		} else {
			values = initialValues;
		}

		Long now = System.currentTimeMillis();

		// initial insert values
		values.put(NoteBase.COLUMN_NAME_CREATE_DATE, now);
		values.put(NoteBase.COLUMN_NAME_MODIFICATION_DATE, now);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String table;
		Uri baseUri;

		if (type == NOTES) {
			table = NoteBase.Note.TABLE_NAME;
			baseUri = NoteBase.Note.CONTENT_ID_URI_BASE;
			if (((String) values.get(NoteBase.Note.COLUMN_NAME_TITLE)).trim() == "") {
                values.put(NoteBase.Note.COLUMN_NAME_TITLE, getContext().getString(R.string.untitled));
            }

		} else {
			table = NoteBase.Snippet.TABLE_NAME;
			baseUri = NoteBase.Snippet.CONTENT_ID_URI_BASE;
		}

		long rowId = db.insert(table, null, values);

		if (rowId > 0) {
			Uri nUri = ContentUris.withAppendedId(baseUri, rowId);
			getContext().getContentResolver().notifyChange(nUri, null);

			return nUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}


	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String finalWhere;

		int count;

		switch (sUriMatcher.match(uri)) {
		case NOTES:
			count = db.update(NoteBase.Note.TABLE_NAME, values, where,
					whereArgs);
			break;
		case NOTE_ID:
			finalWhere = NoteBase.Note._ID + "="
					+ uri.getPathSegments().get(NoteBase.POSITION_OF_COLUMN_ID);

			if (where != null) {
				finalWhere = where + " AND " + finalWhere;
			}

			count = db.update(NoteBase.Note.TABLE_NAME, values, finalWhere, whereArgs);
			break;
		case SNIPPETS:
			count = db.update(NoteBase.Snippet.TABLE_NAME, values, where, whereArgs);
			break;
		case SNIPPET_ID:
			finalWhere = NoteBase.Snippet._ID + "="
					+ uri.getPathSegments().get(NoteBase.POSITION_OF_COLUMN_ID);

			if (where != null) {
				finalWhere = where + " AND " + finalWhere;
			}
			count = db.update(NoteBase.Snippet.TABLE_NAME, values, finalWhere, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int type = sUriMatcher.match(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		String finalWhere;

		int count;

		switch (type) {
		case NOTES:
			count = db.delete(NoteBase.Note.TABLE_NAME, where, whereArgs);
			break;
		case NOTE_ID:
			finalWhere = NoteBase.Note._ID + "="
					+ uri.getPathSegments().get(NoteBase.POSITION_OF_COLUMN_ID);

			if (where != null) {
				finalWhere = where + " AND " + finalWhere;
			}
			count = db.delete(NoteBase.Note.TABLE_NAME, finalWhere, whereArgs);
			break;
		case SNIPPETS:
			count = db.delete(NoteBase.Snippet.TABLE_NAME, where, whereArgs);
			break;
		case SNIPPET_ID:
			finalWhere = NoteBase.Snippet._ID + "="
					+ uri.getPathSegments().get(NoteBase.POSITION_OF_COLUMN_ID);

			if (where != null) {
				finalWhere = where + " AND " + finalWhere;
			}
			count = db.delete(NoteBase.Snippet.TABLE_NAME, finalWhere,
					whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}
}