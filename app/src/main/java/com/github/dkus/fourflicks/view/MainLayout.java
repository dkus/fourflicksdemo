package com.github.dkus.fourflicks.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.dkus.fourflicks.R;
import com.github.dkus.fourflicks.util.Logger;
import com.github.dkus.fourflicks.util.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;


public class MainLayout extends RelativeLayout {

    private int mW, mH;

    private boolean mImageShowing;
    private String mImagePath;

    public MainLayout(Context context) {
        super(context);
    }

    public MainLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        if (mW<=0) mW = r-l;
        if (mH<=0) mH = b-t;

    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {

        super.dispatchFreezeSelfOnly(container);

    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {

        super.dispatchThawSelfOnly(container);

    }

    @Override
    protected Parcelable onSaveInstanceState() {

        return new SavedState(super.onSaveInstanceState(), mImageShowing, mImagePath);

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mImageShowing = savedState.isImageShowing();
        mImagePath = savedState.getImagePath();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mImageShowing) showImage(mImagePath);
            }
        }, 20);

    }

    public void showImage(String imagePath) {

        new LayoutTask((ImageView)findViewById(R.id.image),
                (TextView)findViewById(R.id.locations),
                (TextView)findViewById(R.id.takePicture), mW, mH).execute(imagePath);

        mImagePath = imagePath;
        mImageShowing = imagePath!=null;

    }

    public void hideImage() {

        final ObjectAnimator locations =
                ObjectAnimator.ofFloat(findViewById(R.id.locations), "alpha", 0f, 1f);
        locations.setDuration(900);
        final ObjectAnimator takePicture =
                ObjectAnimator.ofFloat(findViewById(R.id.takePicture), "alpha", 0f, 1f);
        takePicture.setDuration(900);
        ObjectAnimator image =
                ObjectAnimator.ofFloat(findViewById(R.id.image), "alpha", 1f, 0f);
        image.setDuration(500);

        image.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {

                ((ImageView)findViewById(R.id.image)).setImageDrawable(null);
                locations.start();
                takePicture.start();
                findViewById(R.id.locations).setEnabled(true);
                findViewById(R.id.takePicture).setEnabled(true);

                mImagePath=null;
                mImageShowing = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        image.start();

    }

    private static class LayoutTask extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<ImageView> mImageViewRef;
        private WeakReference<TextView> mTextViewLocRef;
        private WeakReference<TextView> mTextViewPicRef;

        private int mOrientation;
        private int mLwidth, mLheight;

        public LayoutTask(ImageView imageView,
                          TextView textViewLoc, TextView textViewPic,
                          int lwidth, int lheight) {

            mImageViewRef = new WeakReference<ImageView>(imageView);
            mTextViewLocRef = new WeakReference<TextView>(textViewLoc);
            mTextViewPicRef = new WeakReference<TextView>(textViewPic);
            mLwidth = lwidth; mLheight = lheight;

        }

        @Override
        protected Bitmap doInBackground(String... params) {

            try {
                ExifInterface exifInterface = new ExifInterface(params[0]);
                mOrientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(params[0], options);

            boolean shouldSwap = mOrientation==ExifInterface.ORIENTATION_ROTATE_90 ||
                    mOrientation==ExifInterface.ORIENTATION_ROTATE_270;

            Logger.log("doInBackground mLwidth="+mLwidth+", mLheight="+mLheight,
                    MainLayout.class);

            options.inSampleSize =
                    Utils.getScaleToFitFactor(mLwidth, mLheight,
                            shouldSwap ? options.outHeight : options.outWidth,
                            shouldSwap ? options.outWidth : options.outHeight);

            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(params[0], options);

        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {

            if (mImageViewRef.get()==null
                    || mTextViewLocRef.get()==null
                    || mTextViewPicRef.get()==null) return;

            final ObjectAnimator image =
                    ObjectAnimator.ofFloat(mImageViewRef.get(), "alpha", 0f, 1f);
            image.setDuration(900);
            ObjectAnimator takePicture =
                    ObjectAnimator.ofFloat(mTextViewLocRef.get(), "alpha", 1f, 0f);
            takePicture.setDuration(500);
            ObjectAnimator locations =
                    ObjectAnimator.ofFloat(mTextViewPicRef.get(), "alpha", 1f, 0f);
            locations.setDuration(500);

            takePicture.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {

                    mTextViewLocRef.get().setEnabled(false);
                    mTextViewPicRef.get().setEnabled(false);

                    mImageViewRef.get().setAlpha(0f);
                    mImageViewRef.get().setImageBitmap(bitmap);
                    switch (mOrientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            mImageViewRef.get().setRotation(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            mImageViewRef.get().setRotation(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            mImageViewRef.get().setRotation(270);
                            break;
                    }
                    image.start();

                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            takePicture.start();
            locations.start();
        }
    }

    private static class SavedState extends BaseSavedState {

        private final boolean mImageShowing;
        private final String mImagePath;

        public SavedState(Parcelable superState, boolean isImageShowing, String imagePath) {

            super(superState);
            mImageShowing=isImageShowing;
            mImagePath=imagePath;

        }

        private SavedState(Parcel in) {
            super(in);
            mImageShowing = in.readInt()==1;
            mImagePath = in.readString();
        }

        public boolean isImageShowing() {
            return mImageShowing;
        }

        public String getImagePath() {
            return mImagePath;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {

            super.writeToParcel(out, flags);

            out.writeInt(mImageShowing ? 1 : 0);
            out.writeString(mImagePath != null ? mImagePath : "");

        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        };

    }

}
