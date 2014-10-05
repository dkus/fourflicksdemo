package com.github.dkus.fourflicks.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.github.dkus.fourflicks.R;
import com.github.dkus.fourflicks.api.db.DbHandlerThread;
import com.github.dkus.fourflicks.api.service.ServiceHandler;
import com.github.dkus.fourflicks.fragment.MainFragment;
import com.github.dkus.fourflicks.fragment.TaskFragment;


public class MainActivity extends ActionBarActivity {

    private TaskFragment mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .add(R.id.container, new MainFragment(), MainFragment.FRAGMENT_TAG)
                .commit();

        if (fragmentManager.findFragmentByTag(TaskFragment.FRAGMENT_TAG)==null) {
            mTaskFragment = new TaskFragment();
            fragmentManager.beginTransaction()
                    .add(mTaskFragment, TaskFragment.FRAGMENT_TAG)
                    .commit();
        }


    }

    public DbHandlerThread getDbHandlerThread() {
        return mTaskFragment.getDbHandlerThread();
    }

    public ServiceHandler getServiceHandler() {
        return mTaskFragment.getServiceHandler();
    }
}
