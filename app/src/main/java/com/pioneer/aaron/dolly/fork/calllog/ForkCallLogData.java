package com.pioneer.aaron.dolly.fork.calllog;

import android.text.TextUtils;

import com.pioneer.aaron.dolly.utils.Matrix;

/**
 * Created by Aaron on 4/29/17.
 */

public class ForkCallLogData {
    public static final String PHONENUMBER = "number";
    public static final String TYPE = "type";
    public static final String CALL_TYPE = "call_type";
    public static final String FEATURES = "features";
    public static final String ENCRYPT_CALL = "encrypt_call";

    private String mPhoneNum = "";
    private int mType = 0;
    private int mCallType = 0;
    private int mFeatures = 0;
    private int mEnryptCall = 0;
    private int mQuantity = 0;

    public void setPhoneNum(String phoneNum) {
        if (TextUtils.isEmpty(phoneNum)) {
            mPhoneNum = Matrix.getRandomPhoneNum();
        } else {
            mPhoneNum = phoneNum;
        }
    }

    public void setType(int type) {
        mType = type;
    }

    public void setCallType(int callType) {
        mCallType = callType;
    }

    public void setFeatures(int features) {
        mFeatures = features;
    }

    public void setEnryptCall(int enryptCall) {
        mEnryptCall = enryptCall;
    }

    public void setQuantity(int quantity) {
        mQuantity = quantity;
    }

    public String getPhoneNum() {
        return mPhoneNum;
    }

    public int getType() {
        return mType;
    }

    public int getCallType() {
        return mCallType;
    }

    public int getFeatures() {
        return mFeatures;
    }

    public int getEnryptCall() {
        return mEnryptCall;
    }

    public int getQuantity() {
        return mQuantity;
    }
}
