package com.project.news.api

import com.project.news.constants.Constants.Companion.API_KEY
import com.project.news.models.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * NewsApi contains all @GET, @POST @QUERY requests to request to server.
 * Each request must have api key as parameter to request.
 * */
interface NewsApi {

    /**
     * getBreakingNews function, fetches top headlines with parameters of 'country code', 'page_number', and 'required apiKey'.
     * args:- country_code,pageNumber,apiKey.
     * returns:- Response<NewsResponse>.
     * */

    @GET(value = "v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country")
        countryCode: String,
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ): Response<NewsResponse>

    /**
     * searchForNews function searches passed query 'q' and returns result from passed 'page number' with associated 'api-key'.
     * args:- searchQuery,pageNumber,apiKey.
     * returns:- Response<NewsResponse>.
     * */

    @GET(value = "v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ): Response<NewsResponse>


}