package com.project.news.vm

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.news.constants.Resource
import com.project.news.models.Article
import com.project.news.models.NewsResponse
import com.project.news.repository.NewsRepository
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

/**
 * Instead of ViewModel() extend with AndroidViewModel which is extended version that can hold applicationContext.
 * LiveData is used to observe changes in the data , so that subscribers will get notify about changes.
 * Encapsulation achieved by declaring all field and methods as private, and provided getter methods.
 * Common NetworkCallback listener, provides live changes when connection establish or gone
 * */

class NewsViewModel(app: Application, private val newsRepository: NewsRepository) : AndroidViewModel(app) {

    private val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    fun getBreakingNews() = breakingNews

    private var breakingNewsPage = 1
    fun getBreakingNewsPage() = breakingNewsPage

    private var connectivity: MutableLiveData<Boolean> = MutableLiveData(false)
    fun getConnectivity() = connectivity

    private var breakingNewsResponse: NewsResponse? = null
    private var searchNewsResponse: NewsResponse? = null
    fun restoreSearchNewsResponse() {
        searchNewsResponse = null
    }

    private val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    fun getSearchNews() = searchNews

    private var searchNewsPage = 1
    fun getSearchNewsPage() = searchNewsPage
    fun restoreSearchPageCount() {
        searchNewsPage = 1
    }

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    private var isConnected = false

    init {
        val networkRequest = NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET).addTransportType(TRANSPORT_WIFI).addTransportType(TRANSPORT_CELLULAR).build()

        initializeNetworkCallback()
        val connectivityManager = app.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    fun loadBreakingNews(countryCode: String) = viewModelScope.launch {
        breakingNews.postValue(Resource.Loading())
        try {
            if (isConnected) {
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resource.Error("No internet Connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }

    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse
                } else {
                    breakingNewsResponse?.articles?.addAll(resultResponse.articles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun loadSearchResults(searchQuery: String) = viewModelScope.launch {
        searchNews.postValue(Resource.Loading())
        try {
            if (isConnected) {
                val response = newsRepository.getSearchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }

    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {

        if (response.isSuccessful) {
            response.body()?.let { newsResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = newsResponse
                } else {
                    searchNewsResponse?.articles?.addAll(newsResponse.articles)
                }
                return Resource.Success(searchNewsResponse ?: newsResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveNews(article: Article) = viewModelScope.launch {
        newsRepository.saveNews(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteNews(article: Article) = viewModelScope.launch {
        newsRepository.deleteNews(article)
    }

    private fun initializeNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                isConnected = true
                connectivity.postValue(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isConnected = false
                connectivity.postValue(false)
            }
        }
    }

}