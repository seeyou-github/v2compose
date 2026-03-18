package com.imhanjie.v2ex.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppendTopicInfo(
    val once: String
) : Parcelable
