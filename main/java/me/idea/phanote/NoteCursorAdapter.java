package me.idea.phanote;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.idea.phanote.provider.NoteBase;

public class NoteCursorAdapter extends CursorAdapter {
    private static final String TAG = "NoteCursorAdapter";
	private Context mContext;

	public NoteCursorAdapter(Context context, Cursor c,
			int flagRegisterContentObserver) {
		super(context, c, flagRegisterContentObserver);

		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder holder;
		Cursor c = getCursor();

		if (c == null) {
			return LayoutInflater.from(mContext).inflate(R.layout.note_item, null);
		}

		if (convertView == null) {
		    holder = new ViewHolder();
            convertView = super.getView(position, convertView, parent);

            holder.title = (TextView) convertView.findViewById(R.id.note_item_title);
            holder.timestamp = (TextView) convertView.findViewById(R.id.note_item_update);
            holder.imageView = (ImageView) convertView.findViewById(R.id.note_status_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
		
		c.moveToPosition(position);
        holder.title.setText(c.getString(c
				.getColumnIndex(NoteBase.Note.COLUMN_NAME_TITLE)));
		holder.timestamp.setText(getUpdateAt(c.getLong(c
                .getColumnIndex(NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE))));

        if (c.getInt(c.getColumnIndex(NoteBase.Note.COLUMN_NAME_STATUS)) == 1) {
            holder.imageView.setImageResource(R.drawable.ic_menu_gray_important);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_menu_gray_not_important);
        }

		return convertView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView title = (TextView) view.findViewById(R.id.note_item_title);
		TextView time = (TextView) view.findViewById(R.id.note_item_update);

		title.setText(cursor.getString(cursor
				.getColumnIndex(NoteBase.Note.COLUMN_NAME_TITLE)));
		time.setText(getUpdateAt(cursor.getLong(cursor
				.getColumnIndex(NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE))));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup root) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.note_item, root, false);
		return v;
	}

	private String getUpdateAt(long ms) {
		Date date = new Date(ms);
		SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",
				Locale.CHINA);
		return dateSdf.format(date);
	}

	static class ViewHolder {
		TextView title;
		TextView timestamp;
		ImageView imageView;
	}

}