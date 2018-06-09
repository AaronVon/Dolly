package com.pioneer.aaron.dolly.fork.contacts

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText

import com.pioneer.aaron.dolly.R
import com.pioneer.aaron.dolly.utils.PermissionChecker

import me.yokeyword.fragmentation_swipeback.SwipeBackActivity

/**
 * Created by Aaron on 4/18/17.
 */

class ForkContactsActivity : SwipeBackActivity(), IForkContactContract.View {

    private lateinit var mPresenter: IForkContactContract.Presenter
    private lateinit var mContactQuantity: EditText
    private lateinit var mStartForkButton: Button
    private lateinit var mContactsAllTypeCheckBox: CheckBox
    private lateinit var mContactsAvatar: CheckBox

    internal var mOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.start_fork_contact_btn -> startFork(mContactsAllTypeCheckBox.isChecked, mContactsAvatar.isChecked)
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forkcontacts)

        mPresenter = ForkContactPresenter(this)
        if (mPresenter.checkPermissions(this)) {
            initUI()
        }
    }

    private fun initUI() {
        if (isTaskRoot) {
            setSwipeBackEnable(false)
        }
        mContactQuantity = findViewById<View>(R.id.contact_quantity_edtxt) as EditText
        mContactQuantity.setText(CONTACT_DEFAULT_QUANTITY.toString())

        mStartForkButton = findViewById<View>(R.id.start_fork_contact_btn) as Button
        mStartForkButton.setOnClickListener(mOnClickListener)

        mContactsAllTypeCheckBox = findViewById<View>(R.id.fork_contact_all_type) as CheckBox
        mContactsAvatar = findViewById<View>(R.id.fork_contact_avatar) as CheckBox
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionChecker.PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                var allPermissionGranted = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionGranted = false
                        break
                    }
                }
                if (allPermissionGranted) {
                    initUI()
                }
            }
            else -> {
            }
        }

    }

    private fun startFork(allTypes: Boolean, avatarIncluded: Boolean) {
        val quantityStr = mContactQuantity.text.toString()
        if (TextUtils.isEmpty(quantityStr) || Integer.valueOf(quantityStr) <= 0) {
            Snackbar.make(findViewById(R.id.activity_fork_contact_layout), R.string.contact_quantity_msg, Snackbar.LENGTH_SHORT)
                    .show()
            return
        }

        val quantity = Integer.parseInt(quantityStr)
        mPresenter.forkContacts(this, quantity, allTypes, avatarIncluded)
    }

    override fun onDestroy() {
        mPresenter.onDestroy(this)
        super.onDestroy()
    }

    companion object {
        private const val CONTACT_DEFAULT_QUANTITY = 20
    }
}
