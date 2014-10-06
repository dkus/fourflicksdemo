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
                doSync(msg.obj, msg.arg1 > 0);
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

        sync(venues, mReceiver, false);

    }

    public void sync(Venue venue, boolean forceUpdate) {

        sync(venue, mReceiver, forceUpdate);

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

    private void sync(Object o, Handler handler, boolean forceUpdate) {

        Message msg = Message.obtain();
        msg.what = R.id.database_sync;
        msg.obj = o;
        msg.arg1 = forceUpdate ? 1 : 0;
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

    private void doSync(Object o, boolean forceUpdate) {

        if (o instanceof List) {

            List<Venue> venues = (List<Venue>)o;
            for (Venue venue : venues) {
                doDbSync(venue, forceUpdate);
            }

        } else {
            if (o!=null) doDbSync((Venue) o, forceUpdate);
        }

        synced();

    }

    private void doDbSync(Venue venue, boolean forceUpdate) {

        Venue tmp = mDbCache.get(venue.getId());

        if (tmp!=null) {

            Logger.log("Already in cache venue="+venue);

            if (!venue.equals(tmp) && !forceUpdate) {

                Logger.log("Difference(1) << 1="+tmp, DbHandlerThread.class);
                Logger.log("Difference(1) >> 2="+venue, DbHandlerThread.class);
                venue=tmp;
            }

        } else {

            String[] selectionArgs = {venue.getId()};
            Cursor cursor = mDb.query(VenueDbHelper.TABLE_VENUE,
                    VenueDbHelper.PROJECTION_ALL,
                    VenueDbHelper.SELECTION_BY_ID,
                    selectionArgs,
                    null, null, null);

            if (cursor.moveToNext()) {

                Venue tmp1 = new Venue();
                tmp1.setId(cursor.getString(0));
                tmp1.setName(cursor.getString(1));
                Venue.Location location = new Venue.Location();
                location.setAddress(cursor.getString(2));
                location.setLat(cursor.getDouble(3));
                location.setLng(cursor.getDouble(4));
                tmp1.setLocation(location);
                cursor.close();

                if (!venue.equals(tmp1) && !forceUpdate) {
                    Logger.log("Difference(2) << 1="+tmp1, DbHandlerThread.class);
                    Logger.log("Difference(2) >> 2="+venue, DbHandlerThread.class);
                    venue=tmp1;
                }

            } else {
                long id = mDb.insert(VenueDbHelper.TABLE_VENUE, null, getContentValues(venue));
                if (id!=-1) {
                    mDbCache.put(venue.getId(), venue);
                    Logger.log("Inserted database with generated ID="+id, DbHandlerThread.class);
                }
            }
        }

        if (forceUpdate) {
            String[] selectionArgs = {venue.getId()};
            int count=mDb.update(
                    VenueDbHelper.TABLE_VENUE,
                    getContentValues(venue),
                    VenueDbHelper.SELECTION_BY_ID,
                    selectionArgs);
            if (count==1) {
                Logger.log("Updated database for venue ID="+venue.getId());
                mDbCache.put(venue.getId(), venue);
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
