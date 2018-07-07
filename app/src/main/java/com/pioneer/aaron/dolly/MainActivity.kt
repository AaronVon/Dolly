package com.pioneer.aaron.dolly

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.pioneer.aaron.dolly.fork.DataBaseOperator
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogActivity
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData
import com.pioneer.aaron.dolly.fork.calllog.ForkVvmActivity
import com.pioneer.aaron.dolly.fork.contacts.ForkContactsActivity
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity(), IMainContract.View {

    private lateinit var mForkCallLogButton: View
    private lateinit var mForkContactButton: View
    private lateinit var mForkVvmButton: View

    private lateinit var mPresenter: IMainContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityUI().setContentView(this)
        mPresenter = MainPresenter(this)
    }

    override fun onResume() {
        super.onResume()
        mPresenter.checkPermissions(this)
        mPresenter.loadResInBackground(this)
    }

    override fun onDestroy() {
        mPresenter.onDestroy(this)
        super.onDestroy()
    }

    inner class MainActivityUI : AnkoComponent<MainActivity> {
        override fun createView(ui: AnkoContext<MainActivity>): View =
                with(ui) {
                    scrollView {
                        padding = dip(resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin))
                        verticalLayout {
                            mForkCallLogButton = linearLayout {
                                imageView(R.drawable.ic_call_log)
                                textView(R.string.fork_calllog_btn) {
                                    textColor = getColor(android.R.color.black)
                                    typeface = Typeface.DEFAULT_BOLD
                                }.lparams {
                                    marginStart = dimen(R.dimen.fork_image_btn)
                                    gravity = Gravity.CENTER
                                }
                                setOnClickListener {
                                    startActivity(Intent(this@MainActivity, ForkCallLogActivity::class.java))
                                }
                                setOnLongClickListener {
                                    val columnExists = DataBaseOperator.getInstance(this@MainActivity).columnsExists

                                    if (columnExists[ForkCallLogData.SUBJECT]!!
                                            && columnExists[ForkCallLogData.POST_CALL_TEXT]!!
                                            && columnExists[ForkCallLogData.IS_PRIMARY]!!) {
                                        mPresenter.vibrate()
                                        mPresenter.forkRCS(this@MainActivity)
                                    }
                                    true
                                }
                            }

                            mForkContactButton = linearLayout {
                                imageView(R.drawable.ic_contact)
                                textView(R.string.fork_contact_btn) {
                                    textColor = getColor(android.R.color.black)
                                    typeface = Typeface.DEFAULT_BOLD
                                }.lparams {
                                    marginStart = dimen(R.dimen.fork_image_btn)
                                    gravity = Gravity.CENTER
                                }

                                setOnClickListener {
                                    startActivity(Intent(this@MainActivity, ForkContactsActivity::class.java))
                                }
                            }.lparams(width = matchParent) {
                                topMargin = dimen(R.dimen.fork_btn_margin)
                            }

                            mForkVvmButton = linearLayout {
                                imageView(R.drawable.voicemail) {
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                }.lparams(width = dimen(R.dimen.fork_btn_width),
                                        height = dimen(R.dimen.fork_btn_height))
                                textView(R.string.fork_vvm_btn) {
                                    textColor = getColor(android.R.color.black)
                                    typeface = Typeface.DEFAULT_BOLD
                                }.lparams {
                                    marginStart = dimen(R.dimen.fork_image_btn)
                                    gravity = Gravity.CENTER
                                }

                                setOnClickListener {
                                    startActivity(Intent(this@MainActivity, ForkVvmActivity::class.java))
                                }
                            }.lparams(width = matchParent) {
                                topMargin = dimen(R.dimen.fork_btn_margin)
                            }
                        }.applyRecursively { view ->
                            when (view) {
                                mForkCallLogButton, mForkContactButton, mForkVvmButton -> {
                                    view.isClickable = true
                                    view.setBackgroundResource(R.drawable.ripple_btn)
                                    view.setPadding(0, dimen(R.dimen.fork_btn_padding),
                                            0, dimen(R.dimen.fork_btn_padding))
                                    if (view is LinearLayout) {
                                        view.gravity = Gravity.CENTER
                                    }
                                }
                            }
                        }
                    }
                }
    }
}
