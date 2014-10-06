package com.github.dkus.fourflicks.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dkus.fourflicks.R;
import com.github.dkus.fourflicks.activity.MainActivity;
import com.github.dkus.fourflicks.api.db.DbHandlerThread;
import com.github.dkus.fourflicks.api.model.Venue;
import com.github.dkus.fourflicks.api.service.ServiceHandler;
import com.github.dkus.fourflicks.api.service.foursquare.FoursquareResponse;
import com.github.dkus.fourflicks.util.Logger;
import com.github.dkus.fourflicks.util.Utils;
import com.github.dkus.fourflicks.view.EditInfoWindowLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MapFragment extends Fragment implements GoogleMap.OnMyLocationChangeListener,
        Handler.Callback, Callback<FoursquareResponse>,
        GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener,
        GoogleMap.CancelableCallback,
        ActionMode.Callback {

    public static final String FRAGMENT_TAG = MapFragment.class.getSimpleName();

    private GoogleMap mMap;
    private SupportMapFragment mSupportMapFragment;
    private LatLng mLatLng;
    private LatLng mLatLngEdge;
    private Marker mClicked;

    private float[] mDistance = new float[1];

    private ServiceHandler mServiceHandler;
    private DbHandlerThread mDbHandlerThread;

    private ActionMode mActionMode;

    private boolean mSyncing;

    private Toast mToast;

    private EditInfoWindowLayout mEditInfoWindowLayout;

    private Handler mHandler;

    private HashMap<Marker, Venue> mBoundMarkers = new HashMap<Marker, Venue>();

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        mServiceHandler = ((MainActivity)activity).getServiceHandler();
        mDbHandlerThread = ((MainActivity)activity).getDbHandlerThread();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mHandler=new Handler(this);
        mDbHandlerThread.setCallBack(mHandler);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mEditInfoWindowLayout = (EditInfoWindowLayout)view.findViewById(R.id.editInfoWindow);

        mSupportMapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map_container);
        if (mSupportMapFragment == null) {
            mSupportMapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction().replace(R.id.map_container, mSupportMapFragment)
                    .commit();
        }

        return view;

    }

    @Override
    public void onStart() {

        super.onStart();

        if (mMap==null) {
            mMap = mSupportMapFragment.getMap();
            if (mMap==null) {
                getFragmentManager().popBackStack();
            } else {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationChangeListener(this);
                mMap.setInfoWindowAdapter(this);
                mMap.setOnMarkerClickListener(this);
                mMap.setOnMapClickListener(this);
                mMap.setOnCameraChangeListener(this);
            }
        }

    }

    @Override
    public void onDestroyView() {

        if (mToast!=null) mToast.cancel();

        super.onDestroyView();

    }

    @Override
    public void onDetach() {

        mDbHandlerThread=null;
        mServiceHandler=null;

        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.map, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (mSyncing) {
            menu.findItem(R.id.mRefresh).setActionView(R.layout.actionbar_progress);
        } else {
            menu.findItem(R.id.mRefresh).setActionView(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mRefresh:
                if (mLatLng==null) {
                    mToast=Toast.makeText(getActivity(),
                            R.string.my_location_error, Toast.LENGTH_SHORT);
                    mToast.show();
                } else {
                    callService(prepareServiceCall());
                }
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onMyLocationChange(Location location) {

        if (mLatLng==null) {
            mLatLng=new LatLng(location.getLatitude(), location.getLongitude());
            if(mMap != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15f), this);
            }
        } else {
            mLatLng=new LatLng(location.getLatitude(), location.getLongitude());
        }

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        if (mLatLngEdge==null) {
            mLatLngEdge=mMap.getProjection().getVisibleRegion().farLeft;
        }

        Location.distanceBetween(
                mLatLngEdge.latitude, mLatLngEdge.longitude,
                mMap.getProjection().getVisibleRegion().farLeft.latitude,
                mMap.getProjection().getVisibleRegion().farLeft.longitude,
                mDistance
        );

        if (mDistance[0]>1000) {
            mLatLng=cameraPosition.target;
            mLatLngEdge=mMap.getProjection().getVisibleRegion().farLeft;
            callService(prepareServiceCall());
        }

    }

    @Override
    public void onFinish() {
        callService(prepareServiceCall());
    }

    @Override
    public void onCancel() {
        //nothing here
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case R.id.database_syncing:
                Venue venue = (Venue)msg.obj;
                if (venue!=null) {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(venue.getLocation().getLat(),
                                    venue.getLocation().getLng())));
                    if (mBoundMarkers.get(marker)!=null) {
                        Logger.log("Marker already exist and it is in bound");
                    } else {
                        mBoundMarkers.put(marker, venue);
                        Logger.log("Marker added in bound");
                    }
                }
                break;
            case R.id.database_synced:
                Logger.log("Number of in bound markers (before) ="+mBoundMarkers.size());
                new AsyncTask<LatLngBounds, Marker, Void>() {

                    @Override
                    protected Void doInBackground(LatLngBounds... params) {

                        for (Iterator<Marker> i = mBoundMarkers.keySet().iterator(); i.hasNext();) {
                            if (!params[0].contains(i.next().getPosition())) {
                                publishProgress(i.next());
                                Logger.log("Marker out of bound="+i.next());
                                i.remove();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(Marker... values) {
                        values[0].remove();
                        Logger.log("Removed out of bound marker="+values[0]);
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mSyncing=false;
                        if (mActionMode!=null) {
                            mActionMode.invalidate();
                        }
                        Logger.log("Number of in bound markers (after) ="+mBoundMarkers.size());
                    }
                }.execute(mMap.getProjection().getVisibleRegion().latLngBounds);

                break;
        }

        return true;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.infowindow_map, null);
        ((TextView)v.findViewById(R.id.name))
                .setText(mBoundMarkers.get(marker).getName());
        ((TextView)v.findViewById(R.id.address))
                .setText(mBoundMarkers.get(marker).getLocation().getAddress());
        return v;

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (mActionMode==null && getActivity()!=null) {
            mActionMode = ((ActionBarActivity)getActivity()).startSupportActionMode(this);
        }
        mClicked=marker;
        return false;

    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (mActionMode!=null && !mSyncing) {
            mActionMode.finish();
        }

        if (mEditInfoWindowLayout!=null && mEditInfoWindowLayout.getVisibility()==View.VISIBLE) {
            mEditInfoWindowLayout.toggleAnimation();
        }

    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

        actionMode.getMenuInflater().inflate(R.menu.map_context, menu);
        return true;

    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {

        MenuItem refresh = menu.findItem(R.id.cRefresh);
        refresh.setVisible(Utils.toogle(!mSyncing, true, false));
        refresh.setActionView(R.layout.actionbar_progress);

        MenuItem edit = menu.findItem(R.id.edit);
        edit.setEnabled(Utils.toogle(mSyncing, true, false));
        edit.setTitle(Utils.toogle(edit.getTitle(),
                getString(R.string.edit), getString(R.string.cancel)));

        MenuItem save = actionMode.getMenu().findItem(R.id.save);
        save.setEnabled(Utils.toogle(
                getString(R.string.edit).equals(edit.getTitle()), true, false));

        return true;

    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.edit:

                mEditInfoWindowLayout.toggleAnimation();
                mSyncing=false;
                actionMode.invalidate();

                break;
            case R.id.save:

                mSyncing=true;
                actionMode.invalidate();
                mBoundMarkers.get(mClicked)
                        .setName(mEditInfoWindowLayout.getName());
                mBoundMarkers.get(mClicked).getLocation()
                        .setAddress(mEditInfoWindowLayout.getAddress());
                mDbHandlerThread.sync(mBoundMarkers.get(mClicked));
                mClicked.hideInfoWindow();
                mClicked.showInfoWindow();

                break;
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

        mActionMode=null;

        if (mEditInfoWindowLayout!=null && mEditInfoWindowLayout.getVisibility()==View.VISIBLE) {
            mEditInfoWindowLayout.toggleAnimation();
        }

    }

    @Override
    public void success(FoursquareResponse foursquareResponse, Response response) {

        Logger.log("FoursquareResponse="+foursquareResponse, MapFragment.class);

        mDbHandlerThread.sync(foursquareResponse.getResponse().getVenues());
        mSyncing=false;
        getActivity().invalidateOptionsMenu();

    }

    @Override
    public void failure(RetrofitError retrofitError) {

        Logger.log("FoursquareResponse error", MapFragment.class, retrofitError);
        mSyncing=false;
        getActivity().invalidateOptionsMenu();
        mToast=Toast.makeText(getActivity(), R.string.foursquare_error, Toast.LENGTH_SHORT);
        mToast.show();

    }

    private void callService(final String radius) {

        if (!mSyncing) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mServiceHandler.getFoursquareService().getNearbyObjects(
                            Utils.formatLocation(mLatLng.latitude, mLatLng.longitude),
                            radius, MapFragment.this);
                    mSyncing=true;
                    getActivity().invalidateOptionsMenu();
                }
            }, 700);
        }

    }

    private String prepareServiceCall() {

        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();

        Location.distanceBetween(visibleRegion.farLeft.latitude,
                visibleRegion.farLeft.longitude,
                mLatLng.latitude, mLatLng.longitude, mDistance);
        double d1=mDistance[0];

        Location.distanceBetween(visibleRegion.farRight.latitude,
                visibleRegion.farRight.longitude,
                mLatLng.latitude, mLatLng.longitude, mDistance);
        double d2=mDistance[0];

        Location.distanceBetween(visibleRegion.nearLeft.latitude,
                visibleRegion.nearLeft.longitude,
                mLatLng.latitude, mLatLng.longitude, mDistance);
        double d3=mDistance[0];

        Location.distanceBetween(visibleRegion.nearRight.latitude,
                visibleRegion.nearRight.longitude,
                mLatLng.latitude, mLatLng.longitude, mDistance);
        double d4=mDistance[0];

        double radius = Math.max(Math.max(d1, d2), Math.min(d3, d4));

        Logger.log("radius="+radius, MapFragment.class);
        if (mMap.getCameraPosition().zoom < 15f) {
            radius = Math.max(10, radius*0.75);
            Logger.log("radius new="+radius, MapFragment.class);
        }

        return String.valueOf(radius);

    }

    private static class MarkerSyncTask extends AsyncTask<LatLngBounds, Marker, Void> {

        @Override
        protected Void doInBackground(LatLngBounds... params) {
            return null;
        }

        @Override
        protected void onProgressUpdate(Marker... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
