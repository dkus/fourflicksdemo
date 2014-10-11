package com.github.dkus.fourflicks.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.github.dkus.fourflicks.api.db.DbHandlerThread;
import com.github.dkus.fourflicks.api.service.ServiceHandler;
import com.github.dkus.fourflicks.api.service.flickr.UploadHandlerThread;


public class TaskFragment extends Fragment {

    public static final String FRAGMENT_TAG = TaskFragment.class.getSimpleName();

    private DbHandlerThread mDbHandlerThread;
    private ServiceHandler mServiceHandler;
    private UploadHandlerThread mUploadHandlerThread;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mDbHandlerThread = new DbHandlerThread(getActivity());
        mDbHandlerThread.start();

        mUploadHandlerThread = new UploadHandlerThread(getActivity());
        mUploadHandlerThread.start();

        mServiceHandler = new ServiceHandler();

    }


    @Override
    public void onDestroy() {

        mDbHandlerThread.setCallBack(null);
        mDbHandlerThread.quitDBAndThread();
        
        mUploadHandlerThread.setCallBack(null);
        mUploadHandlerThread.quitUploader();

        super.onDestroy();
    }

    public DbHandlerThread getDbHandlerThread() {
        return mDbHandlerThread;
    }

    public ServiceHandler getServiceHandler() {
        return mServiceHandler;
    }

    public UploadHandlerThread getUploadHandlerThread() {
        return mUploadHandlerThread;
    }

}
