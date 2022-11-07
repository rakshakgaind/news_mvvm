package com.project.news.models


/**
 * main model class acts as response from api and room database
 * */
data class NewsResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)