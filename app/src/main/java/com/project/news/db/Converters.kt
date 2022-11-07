package com.project.news.db

import androidx.room.TypeConverter
import com.project.news.models.Source


/**
 * Type Converts to help sql to store non-primitive type data.
 */
class Converters {
    /**
     * sourceToString(source) converts 'Source' type to string .
     * args:- Source
     * returns:- String
     * */
    @TypeConverter
    fun sourceToString(source: Source): String {
        return source.name.toString()
    }

    /**
     * stringToSource(name) converts 'name' type to 'Source .
     * args:- String
     * returns:- Source
     * */

    @TypeConverter
    fun stringToSource(name: String): Source {
        return (Source(name, name))
    }
}