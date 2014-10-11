package com.github.dkus.fourflicks.api.service.flickr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.github.dkus.fourflicks.R;
import com.github.dkus.fourflicks.util.Logger;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.RequestContext;
import com.googlecode.flickrjandroid.auth.Permission;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.uploader.UploadMetaData;

import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;


public class UploadHandlerThread extends HandlerThread implements Handler.Callback {

    private Handler mReceiver, mCallback;
    private SharedPreferences mSharedPreferences;
    private Flickr mFlickr;

    private String mImagePath;
    private String mTokenSecret;

    public UploadHandlerThread(Context context) {

        super("UploadHandlerThread");
        mSharedPreferences = context.getSharedPreferences("flickr", Context.MODE_PRIVATE);
        mFlickr = new Flickr("69d03ca2d33476b87f343265b291c512", "866c3ddfbf848338");

    }

    @Override
    protected void onLooperPrepared() {

        mReceiver = new Handler(getLooper(), this);

    }

    public Handler getCallback() {
        return mCallback;
    }

    public void setCallBack(Handler callBack) {
        mCallback=callBack;
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case R.id.upload:
                upload();
                break;
            case R.id.upload_authorized:
                upload((Authorization)msg.obj);
                break;
            case R.id.uploader_quit:
                mSharedPreferences=null;
                quit();
                Logger.log("Quit UploadHandlerThread", UploadHandlerThread.class);
                break;
        }

        return true;

    }

    public void uploadImage(String imagePath) {

        mImagePath=imagePath;

        uploadImage(mReceiver);

    }

    public void uploadImage(Authorization authorization) {

        uploadImage(authorization, mReceiver);

    }

    public void quitUploader() {

        quitUploader(mReceiver);

    }

    private void uploadImage(Handler handler) {

        Message msg = Message.obtain();
        msg.what = R.id.upload;
        msg.obj = mImagePath;
        handler.sendMessage(msg);

    }

    private void uploadImage(Authorization authorization, Handler handler) {

        Message msg = Message.obtain();
        msg.what = R.id.upload_authorized;
        msg.obj = authorization;
        handler.sendMessage(msg);

    }

    private void uploading() {

        Message msg = Message.obtain();
        msg.what = R.id.uploading;
        mCallback.sendMessage(msg);

    }

    private void uploaded() {

        Message msg = Message.obtain();
        msg.what = R.id.uploaded;
        mCallback.sendMessage(msg);

    }

    private void quitUploader(Handler handler) {

        Message msg = Message.obtain();
        msg.what = R.id.uploader_quit;
        handler.sendMessage(msg);

    }

    private void authorize(String url) {

        Message msg = Message.obtain();
        msg.what = R.id.upload_authorize;
        msg.obj = url;
        mCallback.sendMessage(msg);

    }

    private void uploadError() {

        Message msg = Message.obtain();
        msg.what = R.id.upload_error;
        mCallback.sendMessage(msg);

    }

    private void upload() {

        String oauthToken = mSharedPreferences.getString("oauth_token", null);
        String oauthTokenSecret = mSharedPreferences.getString("oauth_token_secret", null);

        if (oauthToken!=null || oauthTokenSecret!=null) {

            OAuth oAuth = new OAuth();
            oAuth.setToken(new OAuthToken(oauthToken, oauthTokenSecret));
            upload(oAuth);

        } else {

            try {

                Logger.log("Need authorization", UploadHandlerThread.class);

                OAuthToken oAuthToken = mFlickr.getOAuthInterface().getRequestToken("callback");
                mTokenSecret = oAuthToken.getOauthTokenSecret();

                authorize(mFlickr.getOAuthInterface()
                        .buildAuthenticationUrl(
                                Permission.WRITE,
                                oAuthToken).toString());

            } catch (IOException e) {
                Logger.log("IOException", UploadHandlerThread.class, e);
                uploadError();
            } catch (FlickrException e) {
                Logger.log("FlickrException", UploadHandlerThread.class, e);
                uploadError();
            }
        }
    }

    private void upload(Authorization authorization) {

        try {

            OAuth oAuth = mFlickr.getOAuthInterface().
                    getAccessToken(authorization.getOauthToken(), mTokenSecret,
                    authorization.getOauthVerifier());

            mSharedPreferences.edit()
                    .putString("oauth_token", oAuth.getToken().getOauthToken())
                    .commit();
            mSharedPreferences.edit()
                    .putString("oauth_token_secret", oAuth.getToken().getOauthTokenSecret())
                    .commit();

            upload(oAuth);

        } catch (IOException e) {
            Logger.log("IOException", UploadHandlerThread.class, e);
            uploadError();
        } catch (FlickrException e) {
            Logger.log("FlickrException", UploadHandlerThread.class, e);
            uploadError();
        }

    }

    private void upload(OAuth oAuth) {

        RequestContext.getRequestContext().setOAuth(oAuth);

        Logger.log("doInBackground imagePath="+mImagePath);

        UploadMetaData uploadMetaData = new UploadMetaData();
        uploadMetaData.setDescription("test");

        try {

            uploading();
            mFlickr.getUploader().upload(
                    mImagePath.substring(mImagePath.lastIndexOf("/") + 1),
                    new FileInputStream(mImagePath),
                    uploadMetaData);
            uploaded();

        } catch (IOException e) {
            Logger.log("IOException", UploadHandlerThread.class, e);
            uploadError();
        } catch (FlickrException e) {
            Logger.log("FlickrException", UploadHandlerThread.class, e);
            uploadError();
        } catch (SAXException e) {
            Logger.log("SAXException", UploadHandlerThread.class, e);
            uploadError();
        }

    }

    public static class Authorization {

        private String mOauthToken;
        private String mOauthVerifier;

        public Authorization(String oauthToken, String oauthVerifier) {
            mOauthToken = oauthToken;
            mOauthVerifier = oauthVerifier;
        }

        public String getOauthToken() {
            return mOauthToken;
        }

        public void setOauthToken(String oauthToken) {
            mOauthToken = oauthToken;
        }

        public String getOauthVerifier() {
            return mOauthVerifier;
        }

        public void setOauthVerifier(String oauthVerifier) {
            mOauthVerifier = oauthVerifier;
        }

    }
}
