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
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .addTransportType(TRANSPORT_WIFI)
            .addTransportType(TRANSPORT_CELLULAR)
            .build()

        initializeNetworkCallback()
        val connectivityManager = app.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    /**
     * loadsBreakingNews(countryCode) function takes country as argument and fetches top breaking from specified country.
     * args:- countryCode.
     * postResult:- postValue to breakingNews Live Data for data changes.
     * */
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

    /**
     * It handles response coming from api and based on Success or Errors it returns 'Response'.
     * args:- Response<NewsResponse>
     * returns:-Resource.<T>(data,message)
     * */
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

    /**
     * It takes searchQuery as parameter and based on results it posts value to searchNews.
     * args:- searchQuery
     * return:Job
     * */
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

    /**
     * It handles search response based on success or failure it returns value wrapped in Resource<NewsResponse>.
     * args: Response<NewsResponse>
     * returns:- Resource<NewsResponse>
     * */
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

    /**
     * It saves 'Article' into database and returns primary key of inserted/updated row, if the record already exists it updates the row.
     * args:- Article
     * returns:- Long
     * */
    fun saveNews(article: Article) = viewModelScope.launch {
        newsRepository.saveNews(article)
    }

    /**
     * getSavedNews() returns the result of sql query as list of 'Articles' which is wrapped in LiveData to observe it.
     * args:- N/A.
     * returns:- LiveData<List<Article>>.
     * */
    fun getSavedNews() = newsRepository.getSavedNews()

    /**
     * deleteNews(article) delete 'Article' from database if exists.
     * args:- Article
     * returns:- N/A
     * */
    fun deleteNews(article: Article) = viewModelScope.launch {
        newsRepository.deleteNews(article)
    }

    /**
     * It initialize networkCallback.
     * args: N/A
     * returns:N/A
     * NetworkCallback overrides onAvailable and onLost which is called everytime on connection establish and lost.
     * */
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