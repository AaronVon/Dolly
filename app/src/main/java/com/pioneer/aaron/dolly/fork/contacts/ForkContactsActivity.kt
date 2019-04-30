package com.pioneer.aaron.dolly.fork.contacts

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.view.ViewManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import anko.immersiveToolbar
import com.pioneer.aaron.dolly.R
import com.pioneer.aaron.dolly.utils.Matrix
import com.pioneer.aaron.dolly.utils.PermissionChecker
import com.pioneer.aaron.dolly.utils.PreferenceHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import me.yokeyword.fragmentation_swipeback.SwipeBackActivity
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView

/**
 * Created by Aaron on 4/18/17.
 */

class ForkContactsActivity : SwipeBackActivity(), IForkContactContract.View {

    private lateinit var mPresenter: IForkContactContract.Presenter
    private lateinit var mContactQuantity: EditText
    private lateinit var mStartForkButton: Button
    private lateinit var mContactsAllTypeCheckBox: CheckBox
    private lateinit var mContactsAvatar: CheckBox

    private var mOnClickListener: View.OnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.start_fork_contact_btn -> startFork(mContactsAllTypeCheckBox.isChecked, mContactsAvatar.isChecked)
            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        anko.makeImmersive(window)
        ForkContactsActivityUI().setContentView(this)

        mPresenter = ForkContactPresenter(this, this)
        if (checkPermission(this)) {
            initUI()
        }
    }

    private fun initUI() {
        if (isTaskRoot) {
            setSwipeBackEnable(false)
        }
        mContactQuantity = findViewById<View>(R.id.contact_quantity_edtxt) as EditText

        mStartForkButton = findViewById<View>(R.id.start_fork_contact_btn) as Button
        mStartForkButton.setOnClickListener(mOnClickListener)

        mContactsAllTypeCheckBox = findViewById<View>(R.id.fork_contact_all_type) as CheckBox
        mContactsAvatar = findViewById<View>(R.id.fork_contact_avatar) as CheckBox
        doAsync {
            val forkContactsData = PreferenceHelper.getInstance(application).mForkContactsData
            uiThread {
                mContactQuantity.setText(forkContactsData.quantity.toString())
                mContactsAllTypeCheckBox.isChecked = forkContactsData.allTypes
                mContactsAvatar.isChecked = forkContactsData.genAvatar
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionChecker.PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty()) {
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

    inner class ForkContactsActivityUI : AnkoComponent<ForkContactsActivity> {
        inline fun ViewManager.textInputEditText(theme: Int = 0, init: TextInputEditText.() -> Unit) = ankoView({ TextInputEditText(it) }, theme, init)

        inline fun ViewManager.textInputLayout(theme: Int = 0, init: TextInputLayout.() -> Unit) = ankoView(::TextInputLayout, theme, init)

        override fun createView(ui: AnkoContext<ForkContactsActivity>): View =
                with(ui) {
                    verticalLayout {
                        immersiveToolbar(this@ForkContactsActivity)

                        scrollView {
                            linearLayout {
                                id = R.id.activity_fork_contact_layout
                                orientation = LinearLayout.VERTICAL
                                padding = dimen(R.dimen.activity_horizontal_margin)
                                frameLayout {
                                    textInputLayout {
                                        textInputEditText {
                                            id = R.id.contact_quantity_edtxt
                                            hint = resources.getString(R.string.contacts_quantity_hint)
                                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                                        }.lparams(width = matchParent)
                                    }.lparams(width = matchParent)
                                }
                                checkBox {
                                    id = R.id.fork_contact_all_type
                                    text = resources.getString(R.string.fork_contacts_all_type)
                                }.lparams(width = matchParent)
                                checkBox {
                                    id = R.id.fork_contact_avatar
                                    text = resources.getString(R.string.fork_contacts_avatar)
                                    setOnClickListener {
                                        GlobalScope.launch {
                                            Matrix.AVATAR_MUTEX_LOCK.withLock {
                                                Matrix.preloadAvatars(context)
                                            }
                                        }
                                    }
                                }.lparams(width = matchParent)
                                button {
                                    id = R.id.start_fork_contact_btn
                                    text = resources.getString(R.string.start_fork_contact)
                                }.lparams(width = matchParent)
                            }.lparams(width = matchParent)
                        }
                    }

                }

    }
}
