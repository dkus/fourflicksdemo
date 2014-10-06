package com.github.dkus.fourflicks.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.github.dkus.fourflicks.R;


public class EditInfoWindowLayout extends RelativeLayout {

    public EditInfoWindowLayout(Context context) {
        super(context);
    }

    public EditInfoWindowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditInfoWindowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void toggleAnimation() {

        if (getVisibility()==GONE) {

            TranslateAnimation translateAnimation =
                    new TranslateAnimation(
                            0, 0, 0, 0,
                            Animation.RELATIVE_TO_PARENT, -1, Animation.RELATIVE_TO_SELF, 0);
            translateAnimation.setInterpolator(new OvershootInterpolator(0.9f));
            translateAnimation.setDuration(650);

            setVisibility(VISIBLE);
            startAnimation(translateAnimation);

        } else {

            TranslateAnimation translateAnimation =
                    new TranslateAnimation(
                            0, 0, 0, 0,
                            Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, -1);
            translateAnimation.setInterpolator(new AnticipateOvershootInterpolator(0.8f));
            translateAnimation.setDuration(950);

            startAnimation(translateAnimation);
            setVisibility(GONE);

        }

    }

    public void setName(String name) {

        ((EditText)findViewById(R.id.editName)).setText(name);

    }

    public void setAddress(String address) {

        ((EditText)findViewById(R.id.editAddress)).setText(address);

    }

    public String getName() {

        return ((EditText)findViewById(R.id.editName)).getText().toString();

    }

    public String getAddress() {

        return ((EditText)findViewById(R.id.editAddress)).getText().toString();

    }

}
