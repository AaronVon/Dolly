package com.pioneer.aaron.dolly.fork.calllog

import com.pioneer.aaron.dolly.utils.ForkConstants

/**
 * Created by Aaron on 4/29/17.
 */

class ForkCallLogData {

    var phoneNum = ""
    var type = 0
    var callType = 0
    var features = 0
    var enryptCall = 0
    var quantity = 0
    var subId = ForkConstants.SIM_ONE
    var subject: String? = null
    var postCallText: String? = null

    companion object {
        val CALL_TYPE = "call_type"
        val FEATURES = "features"
        val ENCRYPT_CALL = "encrypt_call"
        val IS_PRIMARY = "is_primary"
        val SUBJECT = "subject"
        val POST_CALL_TEXT = "post_call_text"
    }
}
