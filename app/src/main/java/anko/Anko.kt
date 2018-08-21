package anko

import android.content.Context
import android.graphics.Color
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewManager
import android.view.Window
import android.widget.RadioGroup
import com.pioneer.aaron.dolly.R
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar

fun makeImmersive(window: Window) {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    window.statusBarColor = Color.TRANSPARENT
}

fun ViewManager.immersiveToolbar(activity: AppCompatActivity) = verticalLayout {
    setBackgroundResource(R.color.colorPrimary)
    toolbar {
        setTitleTextColor(activity.getColor(R.color.toolbar_title_color))
        activity.setSupportActionBar(this)
    }.lparams(width = matchParent) {
        topMargin = dimen(R.dimen.status_bar_height)
    }
}

fun ViewManager.subscriptionLayout(context: Context) = radioGroup {
    orientation = RadioGroup.HORIZONTAL
    radioButton {
        id = Id.subOneRadioButton
        text = context.getString(R.string.sub_id_one)
    }
    radioButton {
        id = Id.subTwoRadioButton
        text = context.getString(R.string.sub_id_two)
    }
}

object Id {
    @IdRes
    val subscriptionRadioGroup = View.generateViewId()
    @IdRes
    val subOneRadioButton = View.generateViewId()
    @IdRes
    val subTwoRadioButton = View.generateViewId()
}
