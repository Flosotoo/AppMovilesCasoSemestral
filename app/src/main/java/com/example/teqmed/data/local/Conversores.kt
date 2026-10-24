package com.example.teqmed.data.local

import androidx.room.TypeConverter
import java.util.Date

//Conversores de tipo para Room Database en caso de requerir tipos complejos.
class Conversores {

    @TypeConverter
    fun deTimestamp(valor: Long?): Date? = valor?.let { Date(it) }

    @TypeConverter
    fun aTimestamp(fecha: Date?): Long? = fecha?.time
}
