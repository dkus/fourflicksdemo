package com.github.dkus.fourflicks.api.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;

import com.github.dkus.fourflicks.R;
import com.github.dkus.fourflicks.api.model.Venue;
import com.github.dkus.fourflicks.util.Logger;

import java.util.List;


public class DbHandlerThread extends HandlerThread implements Handler.Callback {

    private Handler mReceiver, mCallback;

    private DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    private LruCache<String, Venue> mDbCache;

    private static final short MEMCACHE_LIMIT = 500;


    public DbHandlerThread(Context context) {

        super("DbHandlerThread");
        mDbHelper = new DbHelper(context);
        mDbHelper.addOnTableDdlListener(new VenueDbHelper());

    }

    @Override
    protected void onLooperPrepared() {

        mReceiver = new Handler(getLooper(), this);
        mDbCache = new LruCache<String, Venue> (MEMCACHE_LIMIT);
        openDB(mReceiver);

    }


    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case R.id.database_sync:
                if (!isDBOpened()) openDB();
                doSync(msg.obj);
                break;
            case R.id.database_open:
                if (!isDBOpened()) openDB();
                Logger.log("Database opened", DbHandlerThread.class);
                break;
            case R.id.database_quit:
                closeDB();
                Logger.log("Database closed", DbHandlerThread.class);
                if (mDbCache!=null) {
                    mDbCache.evictAll();
                    Logger.log("Memory cache cleared", DbHandlerThread.class);
                }
                quit();
                Logger.log("Quit DbHandlerThread", DbHandlerThread.class);
                break;
        }

        return true;
    }

    public Handler getCallback() {
        return mCallback;
    }

    public void setCallBack(Handler callBack) {
        mCallback=callBack;
    }

    public void sync(List<Venue> venues) {

        sync(venues, mReceiver);

    }

    public void sync(Venue venue) {

        sync(venue, mReceiver);

    }

    public void quitDBAndThread() {
        quitDBAndThread(mReceiver);
    }

    private boolean isDBOpened() {
        return mDb!=null && mDb.isOpen();
    }

    private void openDB() {

        if (mDbHelper!=null) mDb=mDbHelper.getWritableDatabase();

    }

    private void closeDB() {

        if (mDb!=null) mDb.close();
        if (mDbHelper!=null) mDbHelper.close();

    }

    private void openDB(Handler handler) {

        Message msg = Message.obtain();
        msg.what = R.id.database_open;
        handler.sendMessage(msg);

    }

    private void quitDBAndThread(Handler handler) {

        Message msg = Message.obtain();
        msg.what = R.id.database_quit;
        handler.sendMessage(msg);

    }

    private void sync(Object o, Handler handler) {

        Message msg = Message.obtain();
        msg.what = R.id.database_sync;
        msg.obj = o;
        handler.sendMessage(msg);

    }

    private void syncing(Venue venue) {

        Message msg = Message.obtain();
        msg.what = R.id.database_syncing;
        msg.obj = venue;
        mCallback.sendMessage(msg);

    }

    private void synced() {

        Message msg = Message.obtain();
        msg.what = R.id.database_synced;
        mCallback.sendMessage(msg);

    }

    private void doSync(Object o) {

        if (o instanceof List) {

            List<Venue> venues = (List<Venue>)o;
            for (Venue venue : venues) {
                doDbSync(venue);
            }

        } else {
            if (o!=null) doDbSync((Venue) o);
        }

        synced();

    }

    private void doDbSync(Venue venue) {

        Venue tmp = mDbCache.get(venue.getId());

        if (tmp!=null) {

            if (!venue.equals(tmp)) {

                String[] selectionArgs = {venue.getId()};

                int count=mDb.update(
                        VenueDbHelper.TABLE_VENUE,
                        getContentValues(venue),
                        VenueDbHelper.SELECTION_BY_ID,
                        selectionArgs);
                if (count==1) {
                    Logger.log("Updated database for venue ID="+venue.getId());
                    tmp.setName(venue.getName());
                    tmp.setLocation(venue.getLocation());
                }
            } else {
                Logger.log("Database has cached record for venue ID="+venue.getId());
            }

        } else {

            String[] selectionArgs = {venue.getId()};

            Cursor cursor = mDb.query(VenueDbHelper.TABLE_VENUE,
                    VenueDbHelper.PROJECTION_ALL,
                    VenueDbHelper.SELECTION_BY_ID,
                    selectionArgs,
                    null, null, null);

            boolean doInsert = !cursor.moveToNext();
            cursor.close();

            if (doInsert) {
                long id = mDb.insert(VenueDbHelper.TABLE_VENUE, null, getContentValues(venue));
                if (id!=-1) {
                    mDbCache.put(venue.getId(), venue);
                    Logger.log("Inserted database with generated ID="+id);
                }
            } else {
                mDbCache.put(venue.getId(), venue);
                Logger.log("Database has selected record for venue ID="+venue.getId());
            }

        }

        syncing(venue);

    }

    private ContentValues getContentValues(Venue venue) {

        ContentValues values = new ContentValues();
        values.put(VenueDbHelper.VENUE_ID, venue.getId());
        values.put(VenueDbHelper.VENUE_NAME, venue.getName());
        values.put(VenueDbHelper.VENUE_ADDRESS, venue.getLocation().getAddress());
        values.put(VenueDbHelper.VENUE_LAT, venue.getLocation().getLat());
        values.put(VenueDbHelper.VENUE_LNG, venue.getLocation().getLng());

        return values;

    }

}
