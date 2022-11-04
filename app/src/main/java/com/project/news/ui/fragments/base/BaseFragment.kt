package com.project.news.ui.fragments.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.news.R
import com.project.news.db.ArticleDatabase
import com.project.news.ui.activity.MainActivity
import com.project.news.vm.NewsViewModel

/*
*  Re-initialization already from already initialized members from activity, to access them in child fragments .
*/

open class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var articleDatabase: ArticleDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!(this::newsViewModel.isInitialized && this::articleDatabase.isInitialized)) {
            newsViewModel = (requireActivity() as MainActivity).getViewModels()
            articleDatabase = (requireActivity() as MainActivity).getDataBase()
        }
    }

    override fun onStart() {
        super.onStart()
        val btmNav = (requireActivity() as MainActivity).findViewById<BottomNavigationView>(R.id.btm_nav)
        btmNav.visibility = View.VISIBLE
        val searchView = (requireActivity() as MainActivity).findViewById<SearchView>(R.id.sv)
        searchView.visibility = View.GONE
    }

}