package com.project.news.constants

/**
* Sealed (enum) class to differentiate between type of response we are getting and wrap response according to them.
* */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>() : Resource<T>()
}