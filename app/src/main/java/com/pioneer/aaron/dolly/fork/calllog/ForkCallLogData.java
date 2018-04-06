package com.pioneer.aaron.dolly.fork.calllog;

import android.text.TextUtils;

import com.pioneer.aaron.dolly.utils.ForkConstants;
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
    public static final String IS_PRIMARY = "is_primary";
    public static final String SUBJECT = "subject";
    public static final String POST_CALL_TEXT = "post_call_text";

    private String mPhoneNum = "";
    private int mType = 0;
    private int mCallType = 0;
    private int mFeatures = 0;
    private int mEnryptCall = 0;
    private int mQuantity = 0;
    private int mSubId = ForkConstants.SIM_ONE;
    private String mSubject;
    private String mPostCallText;

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        mSubject = subject;
    }

    public String getPostCallText() {
        return mPostCallText;
    }

    public void setPostCallText(String postCallText) {
        mPostCallText = postCallText;
    }

    public void setPhoneNum(String phoneNum) {
        mPhoneNum = phoneNum;
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

    public void setSubId(int subId) {
        mSubId = subId;
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

    public int getSubId() {
        return mSubId;
    }
}
