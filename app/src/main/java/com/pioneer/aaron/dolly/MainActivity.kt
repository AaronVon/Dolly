package com.pioneer.aaron.dolly

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.pioneer.aaron.dolly.fork.DataBaseOperator
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogActivity
import com.pioneer.aaron.dolly.fork.calllog.ForkCallLogData
import com.pioneer.aaron.dolly.fork.calllog.ForkVvmActivity
import com.pioneer.aaron.dolly.fork.contacts.ForkContactsActivity

class MainActivity : AppCompatActivity(), IMainContract.View {

    private lateinit var mForkCallLogButton: View
    private lateinit var mForkContactButton: View
    private lateinit var mForkVvmButton: View

    private lateinit var mPresenter: IMainContract.Presenter

    private var mOnClickListener = { it: View ->
        when (it.id) {
            R.id.fork_call_log_btn -> {
                val forkCallLogIntent = Intent(this@MainActivity, ForkCallLogActivity::class.java)
                startActivity(forkCallLogIntent)
            }
            R.id.fork_contact_btn -> {
                val forkContactIntent = Intent(this@MainActivity, ForkContactsActivity::class.java)
                startActivity(forkContactIntent)
            }
            R.id.fork_vvm_btn -> {
                val forkVvmIntent = Intent(this@MainActivity, ForkVvmActivity::class.java)
                startActivity(forkVvmIntent)
            }
            else -> {
            }
        }
    }

    private var mOnLongClickListener: View.OnLongClickListener = View.OnLongClickListener {
        val columnExists = DataBaseOperator.getInstance(this@MainActivity).columnsExists

        if (columnExists.get(ForkCallLogData.SUBJECT)!!
                && columnExists[ForkCallLogData.POST_CALL_TEXT]!!
                && columnExists[ForkCallLogData.IS_PRIMARY]!!) {
            mPresenter.vibrate()
            mPresenter.forkRCS(this@MainActivity)
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPresenter = MainPresenter(this)

        mForkCallLogButton = findViewById(R.id.fork_call_log_btn)
        mForkContactButton = findViewById(R.id.fork_contact_btn)
        mForkVvmButton = findViewById(R.id.fork_vvm_btn)
        mForkCallLogButton.setOnClickListener(mOnClickListener)
        mForkCallLogButton.setOnLongClickListener(mOnLongClickListener)
        mForkContactButton.setOnClickListener(mOnClickListener)
        mForkVvmButton.setOnClickListener(mOnClickListener)
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
}
