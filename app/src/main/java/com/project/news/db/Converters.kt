package com.project.news.db

import androidx.room.TypeConverter
import com.project.news.models.Source


/*
* Type Converts to help sql to store non-primitive type data.
*/
class Converters {
    @TypeConverter
    fun sourceToString(source: Source): String {
        return source.name.toString()
    }

    @TypeConverter
    fun stringToSource(name: String): Source {
        return (Source(name, name))
    }
}