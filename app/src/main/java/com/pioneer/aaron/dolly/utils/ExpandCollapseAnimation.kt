package com.pioneer.aaron.dolly.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import com.pioneer.aaron.dolly.R

/**
 * Created by Aaron on 11/17/17.
 */

class ExpandCollapseAnimation(private val mAnimatedView: View, private val mState: Int) : Animation() {
    private var mEndHeight: Int = 0
    private val mLayoutParams: LinearLayout.LayoutParams

    init {
        mEndHeight = mAnimatedView.measuredHeight
        if (mEndHeight == 0) {
            mEndHeight = mAnimatedView.context.resources.getDimensionPixelSize(R.dimen.expand_collapse_height)
        }

        mLayoutParams = mAnimatedView.layoutParams as LinearLayout.LayoutParams
        if (mState == EXPANEDED) {
            mLayoutParams.bottomMargin = -mEndHeight
        } else {
            mLayoutParams.bottomMargin = 0
        }
        mAnimatedView.visibility = View.VISIBLE
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)
        if (interpolatedTime < 1.0f) {
            if (mState == EXPANEDED) {
                mLayoutParams.bottomMargin = -mEndHeight + (mEndHeight * interpolatedTime).toInt()
            } else {
                mLayoutParams.bottomMargin = -(mEndHeight * interpolatedTime).toInt()
            }
            mAnimatedView.requestLayout()
        } else {
            if (mState == EXPANEDED) {
                mLayoutParams.bottomMargin = 0
                mAnimatedView.requestLayout()
            } else {
                mLayoutParams.bottomMargin = -mEndHeight
                mAnimatedView.visibility = View.GONE
                mAnimatedView.requestLayout()
            }
        }
    }

    companion object {
        var EXPANEDED = 0
        var COLLAPSED = 1
    }
}
