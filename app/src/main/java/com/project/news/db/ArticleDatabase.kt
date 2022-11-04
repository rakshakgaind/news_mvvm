package com.project.news.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.project.news.models.Article


/*
* Database class is implemented with Room library.
* Singleton pattern implemented for database object for whole application.
*/

@Database(entities = [Article::class], version = 1)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {

    abstract fun getArticleDao(): ArticleDao

    companion object {
        @Volatile
        private var instance: ArticleDatabase? = null

        operator fun invoke(context: Context) = instance ?: synchronized(this) {
            instance ?: createDatabase(context)
        }

        private fun createDatabase(context: Context): ArticleDatabase {
            return Room.databaseBuilder(
                context.applicationContext, ArticleDatabase
                ::class.java, "article_db.db"
            ).build()
        }

    }

}