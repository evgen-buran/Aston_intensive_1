package com.buranchikov.audioplayer.utils

import android.widget.Toast

fun showToast(msg: String) {
    Toast.makeText(APP_ACTIVITY, msg, Toast.LENGTH_SHORT).show()
}