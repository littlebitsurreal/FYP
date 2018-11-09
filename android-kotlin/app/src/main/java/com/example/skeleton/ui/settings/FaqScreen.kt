package com.example.skeleton.ui.settings

import android.content.Context
import android.graphics.Color
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.skeleton.R
import com.example.skeleton.helper.LP
import com.example.skeleton.helper.ResourceHelper
import com.example.skeleton.helper.ResourceHelper.dp
import com.example.skeleton.helper.Touchable
import com.example.skeleton.ui.base.BaseController
import com.example.skeleton.widget.ActionBar

class FaqScreen : BaseController() {
    override fun onCreateView(context: Context): View {
        return setup(context)
    }

    private fun setup(context: Context): View {
        val baseLayout = FrameLayout(context)
        val actionBar = setupActionBar(context)
        val contentLayout = LinearLayout(context)
        val textView = TextView(context)

        textView.apply {
            movementMethod = ScrollingMovementMethod()
            setPadding(dp(26), 0, dp(26), 0)
            text = "\nQ1: OUASFH?\n" +
                    "ASOUAGSFO\n" +
                    "\n" +
                    "Q2: UKASGFKJASF?\n" +
                    "Aoishf oaih aoawio h ao as hasf aksjfh  aosiuf hoih oiowqruoi oiu oiuqw  jkqwr.\n" +
                    "\nQ1: OUASFH?\n" +
                    "ASOUAGSFO\n" +
                    "\n" +
                    "Q2: UKASGFKJASF?\n" +
                    "Aoishf oaih aoawio h ao as hasf aksjfh  aosiuf hoih oiowqruoi oiu oiuqw  jkqwr.\n" +
                    "\nQ1: OUASFH?\n" +
                    "ASOUAGSFO\n" +
                    "\n" +
                    "Q2: UKASGFKJASF?\n" +
                    "Aoishf oaih aoawio h ao as hasf aksjfh  aosiuf hoih oiowqruoi oiu oiuqw  jkqwr.\n" +
                    "\nQ1: OUASFH?\n" +
                    "ASOUAGSFO\n" +
                    "\n" +
                    "Q2: UKASGFKJASF?\n" +
                    "Aoishf oaih aoawio h ao as hasf aksjfh  aosiuf hoih oiowqruoi oiu oiuqw  jkqwr.\n" +
                    "\nQ1: OUASFH?\n" +
                    "ASOUAGSFO\n" +
                    "\n" +
                    "Q2: UKASGFKJASF?\n" +
                    "Aoishf oaih aoawio h ao as hasf aksjfh  aosiuf hoih oiowqruoi oiu oiuqw  jkqwr.\n" +
                    "\nQ1: OUASFH?\n" +
                    "ASOUAGSFO\n" +
                    "\n" +
                    "Q2: UKASGFKJASF?\n" +
                    "Aoishf oaih aoawio h ao as hasf aksjfh  aosiuf hoih oiowqruoi oiu oiuqw  jkqwr.\n" +
                    "\n"
        }

        contentLayout.apply {
            orientation = LinearLayout.VERTICAL
            addView(textView, LP.linear(LP.MATCH_PARENT, LP.MATCH_PARENT).build())
        }

        baseLayout.apply {
            addView(actionBar, LP.frame(LP.MATCH_PARENT, dp(50)).build())
            addView(contentLayout, LP.frame(LP.MATCH_PARENT, LP.WRAP_CONTENT)
                    .setMargins(0, dp(50), 0, 0)
                    .build())
        }

        return baseLayout
    }

    private fun setupActionBar(context: Context): ActionBar {
        return ActionBar(context).apply {
            val title = TextView(context).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                typeface = ResourceHelper.font(R.font.barlow_condensed_medium)
                textSize = 22f
                setTextColor(ResourceHelper.color(R.color.primary))
                text = "Frequently Asked Questions"
                Touchable.make(this@apply)
            }

            elevation = 2f
            setBackgroundColor(Color.WHITE)

            addLeftButton(R.drawable.ic_arrow_back_24dp, View.OnClickListener { _ -> popController() })
            addLeftView(title)
        }
    }
}
