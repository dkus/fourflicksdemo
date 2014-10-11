package com.github.dkus.fourflicks.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.dkus.fourflicks.R;
import com.github.dkus.fourflicks.api.service.flickr.UploadHandlerThread;
import com.github.dkus.fourflicks.util.Device;
import com.github.dkus.fourflicks.util.Logger;
import com.github.dkus.fourflicks.util.Utils;
import com.github.dkus.fourflicks.view.MainLayout;

import java.io.File;


public class MainFragment extends Fragment
        implements View.OnClickListener, ActionMode.Callback, Handler.Callback {

    public static final String FRAGMENT_TAG = MapFragment.class.getSimpleName();

    private MainLayout mainLayout;

    private static final int CAMERA_REQUEST_CODE = 1;

    private String mImagePath;

    private boolean mUploading=false;

    private UploadHandlerThread mUploadHandlerThread;

    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainLayout = (MainLayout)inflater.inflate(R.layout.fragment_main, container, false);

        mainLayout.findViewById(R.id.locations).setOnClickListener(this);
        mainLayout.findViewById(R.id.takePicture).setOnClickListener(this);

        return mainLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        mUploadHandlerThread = ((TaskFragment)getFragmentManager()
                .findFragmentByTag(TaskFragment.FRAGMENT_TAG))
                .getUploadHandlerThread();
        mUploadHandlerThread.setCallBack(new Handler(this));

        if (savedInstanceState!=null) {
            mUploading=savedInstanceState.getBoolean("uploading");
        }

        if (mUploading) {
            mActionMode = ((ActionBarActivity)getActivity()).startSupportActionMode(this);
        }

    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

        actionMode.getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {

        MenuItem refresh = menu.findItem(R.id.uploading);
        refresh.setVisible(Utils.toggle(!mUploading, true, false));
        if (refresh.isVisible()) {
            refresh.setActionView(R.layout.actionbar_progress);
        } else {
            refresh.setActionView(null);
        }

        MenuItem upload = menu.findItem(R.id.upload);
        upload.setEnabled(Utils.toggle(mUploading, true, false));

        return true;

    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.upload:
                if (!mUploading) {
                    mUploadHandlerThread.uploadImage(mImagePath);
                }
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

        mUploading=false;
        mActionMode=null;
        mImagePath=null;
        mainLayout.hideImage();

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.locations) {

            if (!Device.hasPlayServices(getActivity())) {
                Toast.makeText(getActivity(), getString(R.string.map_error),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container,
                    new MapFragment(),
                    MapFragment.FRAGMENT_TAG).addToBackStack(MapFragment.FRAGMENT_TAG).commit();

        } else if (v.getId() == R.id.takePicture) {

            if (!Device.hasCamera(getActivity())) {
                Toast.makeText(getActivity(), getString(R.string.camera_error),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = Device.hasCameraApp(getActivity());
            if (intent==null) {
                Toast.makeText(getActivity(), getString(R.string.camera_app_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                File file = Utils.createImgFile();
                if (file==null) {
                    Toast.makeText(getActivity(), getString(R.string.file_error),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
                mImagePath=file.getAbsolutePath();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
           addImageToGallery();
            mActionMode = ((ActionBarActivity)getActivity()).startSupportActionMode(this);
            mainLayout.showImage(mImagePath);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void addImageToGallery() {

        File file = new File(mImagePath);
        Uri contentUri = Uri.fromFile(file);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        getActivity().sendBroadcast(mediaScanIntent);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("uploading", mUploading);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case R.id.uploading:
                mUploading=true;
                if (mActionMode!=null) {
                    mActionMode.invalidate();
                }
                break;
            case R.id.uploaded:
                mUploading=false;
                if (mActionMode!=null) {
                    mActionMode.invalidate();
                }
                Toast.makeText(getActivity(), getString(R.string.flickr_done),
                        Toast.LENGTH_SHORT).show();
                mainLayout.hideImage();
                mImagePath=null;
                break;
            case R.id.upload_authorize:

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container,
                        MyWebViewFragment.getInstance((String)msg.obj),
                        MyWebViewFragment.FRAGMENT_TAG)
                        .addToBackStack(MyWebViewFragment.FRAGMENT_TAG).commit();

                break;
            case R.id.upload_error:
                Toast.makeText(getActivity(), getString(R.string.flickr_error),
                        Toast.LENGTH_SHORT).show();
                break;
        }

        return true;

    }

    public void authorized(String token, String verifier) {

        mUploadHandlerThread.uploadImage(
                new UploadHandlerThread.Authorization(token, verifier));

    }

}
