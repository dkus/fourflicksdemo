package com.github.dkus.fourflicks.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.github.dkus.fourflicks.R;


public class MainLayout extends RelativeLayout {

    private boolean mAnimate=true;

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
    protected void onAttachedToWindow() {

        super.onAttachedToWindow();

        if (mAnimate) {
            TranslateAnimation translateAnimation =
                    new TranslateAnimation(
                            0, 0, 0, 0,
                            Animation.RELATIVE_TO_PARENT, -1, Animation.RELATIVE_TO_SELF, 0);
            translateAnimation.setInterpolator(new DecelerateInterpolator());
            translateAnimation.setDuration(1000);

            TranslateAnimation translateAnimation2 =
                    new TranslateAnimation(
                            0, 0, 0, 0,
                            Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_SELF, 0);
            translateAnimation2.setInterpolator(new DecelerateInterpolator());
            translateAnimation2.setDuration(1000);

            findViewById(R.id.locations).startAnimation(translateAnimation);
            findViewById(R.id.takePicture).startAnimation(translateAnimation2);

            mAnimate=false;
        }
    }

    public boolean isAnimate() {
        return mAnimate;
    }

    public void setAnimate(boolean animate) {
        this.mAnimate = animate;
    }

}
