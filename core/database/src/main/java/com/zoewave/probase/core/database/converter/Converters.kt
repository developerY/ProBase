package com.zoewave.probase.core.database.converter

import android.net.Uri
import androidx.room3.ColumnTypeConverter

class Converters {

    @ColumnTypeConverter
    fun fromString(value: String?): Uri? {
        return if (value == null) null else Uri.parse(value)
    }

    @ColumnTypeConverter
    fun toString(uri: Uri?): String? {
        return uri?.toString()
    }
}
