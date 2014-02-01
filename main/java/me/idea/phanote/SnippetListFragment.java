package me.idea.phanote;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;

import me.idea.phanote.provider.NoteBase;

public class SnippetListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "SnippetListFragment";
    private static final String[] PROJECTION = new String[]{
            NoteBase.Snippet._ID, NoteBase.Snippet.COLUMN_NAME_TITLE,
            NoteBase.Snippet.COLUMN_NAME_CREATE_DATE};
    private static final Uri CONTENT_URI = NoteBase.Snippet.CONTENT_URI;

    CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            mList.setItemChecked((Integer) buttonView.getTag(), true);
        } else {
            mList.setItemChecked((Integer) buttonView.getTag(), false);
        }

        }
    };
    private SnippetCursorAdpter mAdapter;
    private Context mContext;
    private ListView mList;
    private AlertDialog mDialog;
    private AbsListView.MultiChoiceModeListener mMultiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mode.setTitle(mList.getCheckedItemCount() + " Selected");
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("None Selected");
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.snippet_selector_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {

                case R.id.snippet_selector_delete:

                    mDialog.show();
                    return true;

                case R.id.snippet_selector_copy:

                    String result = changedText(false);
                    deleteSelectorSnippets();
                    copyToClipboard(result);
                    destroyActionMode();
                    return true;

                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            destroyActionMode();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        mContext = getActivity();
        getActivity().setTitle(R.string.page_title_snippet);
        getLoaderManager().initLoader(0, null, this);

        initListViewData();
        setMessageAlert();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setMessageAlert() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setMessage(R.string.dialog_snippet_selector_message)
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                mDialog.dismiss();
                }
                }).setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                deleteSelectorSnippets();
                destroyActionMode();
                }
                });
        mDialog = mBuilder.create();
    }

    private void initListViewData() {

        mList = getListView();

        mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mList.setMultiChoiceModeListener(mMultiChoiceModeListener);

        String[] dataColumns = {NoteBase.Snippet.COLUMN_NAME_TITLE,
                NoteBase.Snippet.COLUMN_NAME_CREATE_DATE};
        int[] viewIds = {R.id.snippet_item, R.id.snippet_item_create};
        mAdapter = new SnippetCursorAdpter(mContext, R.layout.snippet_item,
                null, dataColumns, viewIds);
        setListAdapter(mAdapter);
    }

    private int deleteSnippet(long id) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, id);
        return mContext.getContentResolver().delete(uri, null, null);
    }

    private String getUpdateAt(long ms) {
        Date date = new Date(ms);
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateSdf.format(date);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(mContext, CONTENT_URI, PROJECTION, null, null,
                NoteBase.Snippet.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    private void deleteSelectorSnippets() {
        for (long l : mList.getCheckedItemIds()) {
            deleteSnippet(l);
        }
    }

    private void destroyActionMode() {
        if (mList.getCheckedItemIds().length > 0) {
            for (int i = 0; i < mList.getChildCount(); i++) {
                View child = mList.getChildAt(i);
                CheckBox checkbox = (CheckBox) child.findViewById(R.id.checkbox_snippet_item);
                checkbox.setChecked(false);
            }
        }
    }

    private String changedText(boolean appentToNote) {
        StringBuilder result = new StringBuilder();

        String[] ids = new String[mList.getCheckedItemIds().length];
        StringBuilder sb = new StringBuilder("_id IN (");
        CharSequence delimiter = ",";
        boolean firstTime = true;
        int j = 0;

        for (long l : mList.getCheckedItemIds()) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append("?");
            ids[j] = Long.toString(l);
            j++;
        }
        sb.append(")");


        Cursor cursor = mContext.getContentResolver().query(CONTENT_URI,
                new String[]{NoteBase.Note.COLUMN_NAME_TITLE},
                sb.toString(), ids, null);

        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                if (appentToNote) {
                    result.append("\n");
                }

                result.append(cursor.getString(cursor.getColumnIndex(NoteBase.Note.COLUMN_NAME_TITLE)));
            }
        }

        return result.toString();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) mContext
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copy text from phanote.", text);
        clipboard.setPrimaryClip(clip);
    }

    private class SnippetCursorAdpter extends SimpleCursorAdapter {

        public SnippetCursorAdpter(Context context, int layout, Cursor c,
                                   String[] from, int[] to) {
            super(context, layout, c, from, to,
                    CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor != null) {
                TextView title = (TextView) view
                        .findViewById(R.id.snippet_item);
                TextView time = (TextView) view
                        .findViewById(R.id.snippet_item_create);

                title.setText(cursor.getString(cursor
                        .getColumnIndex(NoteBase.Note.COLUMN_NAME_TITLE)));
                time.setText(getUpdateAt(cursor.getLong(cursor
                        .getColumnIndex(NoteBase.Note.COLUMN_NAME_CREATE_DATE))));

                CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox_snippet_item);

                checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
                checkbox.setTag(cursor.getPosition());
            }
        }
    }

}