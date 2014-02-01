package me.idea.phanote.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SimpleCursorAdapter;

import me.idea.phanote.ClipNote;
import me.idea.phanote.R;
import me.idea.phanote.provider.NoteBase;
import me.idea.phanote.setting.SettingsActivity;

public class ClipService extends Service {
    private static final String KEY = "clipbroad status";
    // instant filter action can recieved by broadcast
    private static final String STOP_CLIPBROAD_LISTENDER = "me.idea.phanote.receiver.STOP_CLIPBROAD_LISTENDER";
    private static final String START_CLIPBROAD_LISTENER = "me.idea.phanote.receiver.START_CLIPBROAD_LISTENDER";
    private static final String CANCEL_NOTIFICATION = "me.idea.phanote.receiver.CANCEL_NOTIFICATION";

    private static final String[] PROJECTION = {NoteBase.Note._ID,
            NoteBase.Note.COLUMN_NAME_BODY, NoteBase.Note.COLUMN_NAME_STATUS};

    // notification
    private static final int NOTICE_ID = 0x113;

    // Mark app clipped text append to Note or Snippet.
    public static final int NOTE = 1;
    public static final int SNIPPET = 2;
    // Handler send message what code and current service status
    public static final int STOPED = 0;
    public static final int STARTED = 1;
    private static final int CANCEL = 2;
    public static int STATUS = STOPED; // Current service status.

    private static int mAppendType = 0;
    private static Uri mUri;
    private static ClipboardManager mClipboard;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
        switch (msg.what) {
            case STOPED:
                stopClipbroadListener();
                break;
            case STARTED:
                startClipbroadListener();
                break;

            case CANCEL:
                cancelNotification();
                break;
            default:
                super.handleMessage(msg);
        }
        }
    };
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteViews;
    private static String pasteData = "";
    private OnPrimaryClipChangedListener clipListener = new OnPrimaryClipChangedListener() {

        @Override
        public void onPrimaryClipChanged() {

        if (mClipboard.getPrimaryClipDescription() == null)
            return;

        if (mClipboard.getPrimaryClipDescription().hasMimeType(
                ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            ClipData mClipData = mClipboard.getPrimaryClip();

            if (mClipData == null || mClipData.getItemCount() <= 0)
                return;

            ClipData.Item item = mClipData.getItemAt(0);

            if (item.getText() == null)
                return;
            String str = item.getText().toString().trim();

            if (!pasteData.equals(str)) {
                pasteData = str;

                if (getAppendType() == SNIPPET) {
                    insertSnippet(pasteData);
                } else if (getAppendType() == NOTE) {
                    Cursor cursor = visibleActivityNoteCursor();
                    if (cursor.getCount() > 1) {
                        showFloatWindow(pasteData);
                    } else if (cursor.getCount() == 1) {
                        updateNote(cursor, pasteData);
                    } else {
                        cursor = lastUpdatedNoteCursor();
                        if (!cursor.moveToFirst()) {
                            newNoteCursor(pasteData);
                            return;
                        }
                        updateNote(cursor, pasteData);
                    }
                }
            }
        }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getAction();
            if (state == null)
                return;

            if (state.equals(START_CLIPBROAD_LISTENER)) {
                mHandler.sendEmptyMessage(1);
            } else if (state.equals(STOP_CLIPBROAD_LISTENDER)) {
                mHandler.sendEmptyMessage(0);
            } else if (state.equals(CANCEL_NOTIFICATION)) {
                mHandler.sendEmptyMessage(2);
            }
        }
    };

    private static Context mContext;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        registerClipbroadListener();

        mContext = this;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        setNotificationBuilder();
        setRegisterReceiver();
        setAppendToType();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        setCurrentStatus(STARTED);
        mNotificationManager.notify(NOTICE_ID, mBuilder.build());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterClipbroadListener();
        unregisterReceiver(mReceiver);
        mNotificationManager.cancel(NOTICE_ID);
        setCurrentStatus(STOPED);

        super.onDestroy();
    }

    private void setNotificationBuilder() {
        mRemoteViews = setRemoteViewLayout();

        Intent resultIntent = new Intent(this, ClipNote.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_grey_launcher);
        mBuilder.setContent(mRemoteViews);
        mBuilder.setOngoing(true);
        mBuilder.setContentIntent(resultPendingIntent);
    }

    private RemoteViews setRemoteViewLayout() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);

        remoteViews.setOnClickPendingIntent(R.id.notification_status_toggle,
                PendingIntent.getBroadcast(this, 0, new Intent(STOP_CLIPBROAD_LISTENDER), PendingIntent.FLAG_UPDATE_CURRENT));
        remoteViews.setOnClickPendingIntent(R.id.notification_cancel,
                PendingIntent.getBroadcast(this, 0, new Intent(CANCEL_NOTIFICATION), PendingIntent.FLAG_UPDATE_CURRENT));
        return remoteViews;
    }

    private void setRegisterReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(START_CLIPBROAD_LISTENER);
        intentFilter.addAction(STOP_CLIPBROAD_LISTENDER);
        intentFilter.addAction(CANCEL_NOTIFICATION);
        registerReceiver(mReceiver, intentFilter);
    }

    private void setAppendToType() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int type = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SERVICE_TYPE, false) ? NOTE : SNIPPET;
        setAppendType(type);
    }

    private static void setContentUri(int type) {
        switch (type) {
            case SNIPPET:
                setCurrentUri(NoteBase.Snippet.CONTENT_URI);
                break;
            case NOTE:
                setCurrentUri(NoteBase.Note.CONTENT_URI);
                break;
            default:
                setCurrentUri(NoteBase.Snippet.CONTENT_URI);
        }
    }

    public static void setCurrentStatus(int status) {
        STATUS = status;
    }

    public static int getCurrentStatus() {
        return STATUS;
    }

    public static void setAppendType(int type) {
        mAppendType = type;
        setContentUri(type);
    }

    private static int getAppendType() {
        return mAppendType;
    }

    private static void setCurrentUri(Uri uri) {
        mUri = uri;
    }

    private static Uri getCurrentUri() {
        return mUri;
    }

    public void stopClipbroadListener() {
        unregisterClipbroadListener();
        notificationChangeToStartStatus();
    }

    public void startClipbroadListener() {
        registerClipbroadListener();
        notificationChangeToStopStatus();
    }

    private void registerClipbroadListener() {
        mClipboard.addPrimaryClipChangedListener(clipListener);
    }

    private void unregisterClipbroadListener() {
        mClipboard.removePrimaryClipChangedListener(clipListener);
    }

    private void notificationChangeToStopStatus() {
        Intent mIntent = new Intent(STOP_CLIPBROAD_LISTENDER);
        mRemoteViews.setImageViewResource(R.id.notification_status_toggle, R.drawable.ic_menu_pause);
        mRemoteViews.setTextViewText(R.id.notification_content, getString(R.string.stop_clipbroad_listener));
        mRemoteViews.setOnClickPendingIntent(R.id.notification_status_toggle,
            PendingIntent.getBroadcast(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        mBuilder.setContent(mRemoteViews);
        mNotificationManager.notify(NOTICE_ID, mBuilder.build());

    }

    private void notificationChangeToStartStatus() {
        Intent mIntent = new Intent(START_CLIPBROAD_LISTENER);
        mRemoteViews.setImageViewResource(R.id.notification_status_toggle, R.drawable.ic_menu_play);
        mRemoteViews.setTextViewText(R.id.notification_content, getString(R.string.start_clipbroad_listener));
        mRemoteViews.setOnClickPendingIntent(R.id.notification_status_toggle,
            PendingIntent.getBroadcast(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        mBuilder.setContent(mRemoteViews);
        mNotificationManager.notify(NOTICE_ID, mBuilder.build());
    }

    private void cancelNotification() {
        mNotificationManager.cancel(NOTICE_ID);
    }

    private static WindowManager mWindowManager;
    private static LinearLayout mFloatView;
    private static String mClipStr;

    private void showFloatWindow(String str) {

        mClipStr = str;
        mFloatView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.float_panel, null);
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        ImageView iv = (ImageView) mFloatView.findViewById(R.id.float_close);
        iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mWindowManager.removeView(mFloatView);
            }
        });

        ListView lv = (ListView) mFloatView.findViewById(R.id.float_list);
        lv.setAdapter(visibleNotes());
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
	        public void onItemClick(AdapterView l, View v, int position, long id) {

	            updateNote(getNoteCursor(id), mClipStr);
                mWindowManager.removeView(mFloatView);
	        }
        });

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            , PixelFormat.TRANSPARENT);
        layoutParams.gravity = Gravity.CENTER;
        mWindowManager.addView(mFloatView, layoutParams);
    }

    private void updateNote(Cursor cursor, String str) {
        cursor.moveToFirst();

        Uri uri = Uri.withAppendedPath(getCurrentUri(), Long.toString(cursor.getLong(cursor
                .getColumnIndex(NoteBase.Note._ID))));

        String text, oldText;
        oldText = cursor.getString(cursor.getColumnIndex(NoteBase.Note.COLUMN_NAME_BODY));

        if (oldText == null || oldText.equals("")) {
            text = str;
        } else {
            text = oldText + "\n\n" + str;
        }

        ContentValues values = new ContentValues();
        values.put(NoteBase.Note.COLUMN_NAME_BODY, text);
        values.put(NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());

        getContentResolver().update(uri, values, null, null);
        cursor.close();
    }

    private Cursor getNoteCursor(long id) {
        Uri uri = ContentUris.withAppendedId(NoteBase.Note.CONTENT_URI, id);

        return getContentResolver().query(uri, null, null, null, null);
    }

    private SimpleCursorAdapter visibleNotes() {
        String[] fromColumns = {NoteBase.Note.COLUMN_NAME_TITLE};
        int[] toViews = {R.id.float_panel_item};
        return new SimpleCursorAdapter(mContext, R.layout.float_panel_item, visibleActivityNoteCursor(),
            fromColumns, toViews, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
    }

    private Cursor visibleActivityNoteCursor() {
        return getContentResolver().query(NoteBase.Note.ACTIVITIED_URI, null, null, null, NoteBase.Note.DEFAULT_SORT_ORDER + " LIMIT 3");
    };

    private Cursor lastUpdatedNoteCursor() {
        return getContentResolver().query(NoteBase.Note.CONTENT_URI, null, null, null, NoteBase.Note.DEFAULT_SORT_ORDER + " LIMIT 1");
    };

    private Cursor newNoteCursor(String str) {
        ContentValues values = new ContentValues();
        values.put(NoteBase.Note.COLUMN_NAME_TITLE, getResources().getString(R.string.untitled));
        values.put(NoteBase.Note.COLUMN_NAME_BODY, str);
        values.put(NoteBase.Note.COLUMN_NAME_CREATE_DATE, System.currentTimeMillis());
        values.put(NoteBase.Note.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
        Uri uri = getContentResolver().insert(NoteBase.Note.CONTENT_URI, values);

        return getContentResolver().query(uri, PROJECTION, null, null, null);
    }

    private void insertSnippet(String str) {
        ContentValues values = new ContentValues();
        values.put(NoteBase.Snippet.COLUMN_NAME_TITLE, str);
        getContentResolver().insert(getCurrentUri(), values);

        mBuilder.setContentText(str);
        mNotificationManager.notify(NOTICE_ID, mBuilder.build());
    }

}