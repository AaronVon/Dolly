package com.pioneer.aaron.dolly.fork;

/**
 * Created by Aaron on 5/1/17.
 */

public interface IForkListener {
    void onProgress(int progress);

    void onCompleted();

    void onCanceled();

    void onFailed();
}
