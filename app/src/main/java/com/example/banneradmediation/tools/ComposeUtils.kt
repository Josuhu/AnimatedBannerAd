package com.example.banneradmediation.tools

import android.content.Context
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


object ComposeUtils {

    fun dpFromPx(context: Context, px: Int): Dp {
        return (px / context.resources.displayMetrics.density).dp
    }

    @Suppress("unused")
    fun pxFromDp(context: Context, dp: Dp): Float {
        return (dp * context.resources.displayMetrics.density).value
    }

}