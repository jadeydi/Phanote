package me.idea.phanote.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NoteBase {

    public static final String COLUMN_NAME_CREATE_DATE = "created_at";
    public static final String COLUMN_NAME_MODIFICATION_DATE = "updated_at";
    public static final String DEFAULT_SORT_ORDER = "updated_at DESC";
    private static final String SCHEME = "content://";
    public static final String AUTHORITY = "me.idea.provider.phanote";
    public static final int POSITION_OF_COLUMN_ID = 1;

    public NoteBase() {
    }

    public static final class Note implements BaseColumns {

        private Note() {
        }

        public static final String TABLE_NAME = "notes";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.me.idea.phanote.provider.note";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.me.idea.phanote.provider.note";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_BODY = "body";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_CREATE_DATE = "created_at";
        public static final String COLUMN_NAME_MODIFICATION_DATE = "updated_at";
        public static final String DEFAULT_SORT_ORDER = "updated_at DESC";
        private static final String PATH_NOTES = "/notes";
        private static final String PATH_NOTE_ID = "/notes/";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY
                + PATH_NOTES);
        public static final Uri ACTIVITIED_URI = Uri.parse(SCHEME + AUTHORITY + "/notes/activities");
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME
                + AUTHORITY + PATH_NOTE_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME
                + AUTHORITY + PATH_NOTE_ID + "#");
    }

    public static final class Snippet implements BaseColumns {

        private Snippet() {
        }

        public static final String TABLE_NAME = "snippets";
        public static final String PATH_SNIPPETS = "/snippets";
        public static final String PATH_SNIPPET_ID = "/snippets/";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.me.idea.phanote.provider.snippet";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.me.idea.phanote.provider.snippet";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_CREATE_DATE = "created_at";
        public static final String COLUMN_NAME_MODIFICATION_DATE = "updated_at";
        public static final String DEFAULT_SORT_ORDER = "updated_at DESC";

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY
                + PATH_SNIPPETS);
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME
                + AUTHORITY + PATH_SNIPPET_ID);
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME
                + AUTHORITY + PATH_SNIPPET_ID + "#");
    }

}