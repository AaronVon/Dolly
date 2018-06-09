package com.pioneer.aaron.dolly.fork

/**
 * Created by Aaron on 5/1/17.
 */

interface IForkListener {
    fun onProgress(percentProgress: Int, realProgress: Int)

    fun onCompleted()

    fun onCanceled()

    fun onFailed()
}
