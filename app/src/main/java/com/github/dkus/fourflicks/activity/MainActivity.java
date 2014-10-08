package com.github.dkus.fourflicks.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.github.dkus.fourflicks.R;
import com.github.dkus.fourflicks.api.db.DbHandlerThread;
import com.github.dkus.fourflicks.api.service.ServiceHandler;
import com.github.dkus.fourflicks.api.service.UploadHandlerThread;
import com.github.dkus.fourflicks.fragment.MainFragment;
import com.github.dkus.fourflicks.fragment.MyWebViewFragment;
import com.github.dkus.fourflicks.fragment.TaskFragment;


public class MainActivity extends ActionBarActivity
        implements MyWebViewFragment.AuthorizationListener {

    private TaskFragment mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();

        mTaskFragment = (TaskFragment)
                fragmentManager.findFragmentByTag(TaskFragment.FRAGMENT_TAG);
        if (mTaskFragment==null) {
            mTaskFragment = new TaskFragment();
            fragmentManager.beginTransaction()
                    .add(mTaskFragment, TaskFragment.FRAGMENT_TAG)
                    .commit();
        }

        fragmentManager.beginTransaction()
                .add(R.id.container, new MainFragment(), MainFragment.FRAGMENT_TAG)
                .commit();

    }

    public DbHandlerThread getDbHandlerThread() {
        return mTaskFragment.getDbHandlerThread();
    }

    public ServiceHandler getServiceHandler() {
        return mTaskFragment.getServiceHandler();
    }

    public UploadHandlerThread getUploadHandlerThread() {
        return mTaskFragment.getUploadHandlerThread();
    }

    @Override
    public void authorized(String token, String verifier) {

        MainFragment mainFragment =
                (MainFragment)getSupportFragmentManager()
                        .findFragmentByTag(MainFragment.FRAGMENT_TAG);

        mainFragment.authorized(token, verifier);

    }
}
