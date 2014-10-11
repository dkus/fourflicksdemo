package com.github.dkus.fourflicks.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.github.dkus.fourflicks.R;
import com.github.dkus.fourflicks.fragment.MainFragment;
import com.github.dkus.fourflicks.fragment.MyWebViewFragment;
import com.github.dkus.fourflicks.fragment.TaskFragment;


public class MainActivity extends ActionBarActivity
        implements MyWebViewFragment.AuthorizationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState==null) {

            FragmentManager fragmentManager = getSupportFragmentManager();

            if (fragmentManager.findFragmentByTag(TaskFragment.FRAGMENT_TAG)==null) {
                fragmentManager.beginTransaction()
                        .add(new TaskFragment(), TaskFragment.FRAGMENT_TAG)
                        .commit();
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.container, new MainFragment(), MainFragment.FRAGMENT_TAG)
                    .commit();
        }

    }

    @Override
    public void authorized(String token, String verifier) {

        MainFragment mainFragment =
                (MainFragment)getSupportFragmentManager()
                        .findFragmentByTag(MainFragment.FRAGMENT_TAG);

        mainFragment.authorized(token, verifier);

    }
}
