package com.project.news.vm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.news.repository.NewsRepository

/*
* Instance of repository is passed as parameter to access all methods related to dao and api.
* We can not pass data to ViewModel Class in its constructor directly, For that ViewModelProviderFactory is used.
*/

class NewsVmProviderFactory(val context: Application, private val newsRepository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(context, newsRepository) as T
    }
}