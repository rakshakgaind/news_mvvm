package com.project.news.repository

import com.project.news.api.RetrofitInstance
import com.project.news.db.ArticleDatabase
import com.project.news.models.Article

/*
* Repository pattern implemented in MVVM .
* For ViewModel it provides data ,i.e ViewModel unaware from where we are getting data either from remote or local. (Abstraction)
* It handles all operation either to database or api to get response
* */

class NewsRepository(private val db: ArticleDatabase) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) = RetrofitInstance.serviceApi.getBreakingNews(countryCode, pageNumber)

    suspend fun getSearchNews(searchQuery: String, pageNumber: Int) = RetrofitInstance.serviceApi.searchForNews(searchQuery, pageNumber)

    fun getSavedNews() = db.getArticleDao().getArticles()

    suspend fun saveNews(article: Article) = db.getArticleDao().upsert(article)

    suspend fun deleteNews(article: Article) = db.getArticleDao().deleteArticle(article)

    suspend fun getCount(url: String) = db.getArticleDao().getCount(url)
}