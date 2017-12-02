package com.pioneer.aaron.dolly.utils;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.pioneer.aaron.dolly.R;

/**
 * Created by Aaron on 11/17/17.
 */

public class ExpandCollapseAnimation extends Animation {
    private View mAnimatedView;
    private int mEndHeight;
    private int mState;
    public static int EXPANEDED = 0;
    public static int COLLAPSED = 1;
    private LinearLayout.LayoutParams mLayoutParams;

    public ExpandCollapseAnimation(View animatedView, int state) {
        mAnimatedView = animatedView;
        mState = state;
        mEndHeight = mAnimatedView.getMeasuredHeight();
        if (mEndHeight == 0) {
            mEndHeight = mAnimatedView.getContext().getResources().getDimensionPixelSize(R.dimen.expand_collapse_height);
        }

        mLayoutParams = (LinearLayout.LayoutParams) mAnimatedView.getLayoutParams();
        if (mState == EXPANEDED) {
            mLayoutParams.bottomMargin = -mEndHeight;
        } else {
            mLayoutParams.bottomMargin = 0;
        }
        mAnimatedView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0f) {
            if (mState == EXPANEDED) {
                mLayoutParams.bottomMargin = -mEndHeight + (int) (mEndHeight * interpolatedTime);
            } else {
                mLayoutParams.bottomMargin = -(int) (mEndHeight * interpolatedTime);
            }
            mAnimatedView.requestLayout();
        } else {
            if (mState == EXPANEDED) {
                mLayoutParams.bottomMargin = 0;
                mAnimatedView.requestLayout();
            } else {
                mLayoutParams.bottomMargin = -mEndHeight;
                mAnimatedView.setVisibility(View.GONE);
                mAnimatedView.requestLayout();
            }
        }
    }
}
