package com.github.dkus.fourflicks.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.dkus.fourflicks.util.Logger;


public class MyWebViewFragment extends Fragment {

    public static final String FRAGMENT_TAG = MyWebViewFragment.class.getSimpleName();

    private WebView mWebView;

    private AuthorizationListener mAuthorizationListener;

    public static MyWebViewFragment getInstance(String url) {

        MyWebViewFragment myWebViewFragment = new MyWebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        myWebViewFragment.setArguments(bundle);

        return myWebViewFragment;

    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        mAuthorizationListener = (AuthorizationListener)activity;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mWebView != null) {
            mWebView.destroy();
        }
        mWebView = new WebView(getActivity());
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl(getArguments().getString("url"));

        return mWebView;

    }

    @Override
    public void onPause() {

        mWebView.onPause();

        super.onPause();

    }

    @Override
    public void onResume() {

        super.onResume();

        mWebView.onResume();

    }

    @Override
    public void onDetach() {

        mAuthorizationListener=null;

        super.onDetach();

    }

    @Override
    public void onDestroy() {

        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }

        super.onDestroy();

    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Uri uri = Uri.parse(url);

            Logger.log("shouldOverrideUrlLoading Uri="+uri);

            if (uri.getQueryParameter("oauth_verifier")!=null) {

                mAuthorizationListener.authorized(
                        uri.getQueryParameter("oauth_token"),
                        uri.getQueryParameter("oauth_verifier"));
                getFragmentManager().popBackStack();

            }

            return false;
        }
    }

    public static interface AuthorizationListener {

        public void authorized(String token, String verifier);

    }
}
