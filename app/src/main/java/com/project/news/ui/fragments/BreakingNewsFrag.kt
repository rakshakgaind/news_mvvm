package com.project.news.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.news.R
import com.project.news.adapters.NewsAdapter
import com.project.news.constants.Constants.Companion.COUNTRY_CODE_CONST
import com.project.news.constants.Constants.Companion.QUERY_PAGE_SIZE
import com.project.news.constants.Resource
import com.project.news.databinding.FragmentBreakingNewsBinding
import com.project.news.ui.fragments.base.BaseFragment

/**
 * Fetches and list top 20 news from first page, on scroll fetches again 20 news from next page, pagination concept implemented.
 */
class BreakingNewsFrag : BaseFragment(R.layout.fragment_breaking_news) {

    private lateinit var binding: FragmentBreakingNewsBinding
    private val adapter: NewsAdapter by lazy { NewsAdapter() }
    private val mTAG = "BreakingNewsFrag"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBreakingNewsBinding.bind(view)
        showShimmer()
        setUpRecyclerView()
        observers()
    }

    private fun observers() {
        newsViewModel.getBreakingNews().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    stopShimmer()
                    response.data?.let {
                        adapter.asyncList.submitList(it.articles.toList())
                        val totalPage = it.totalResults
                        stopShimmer()
                        isLastPage = newsViewModel.getBreakingNewsPage() == totalPage
                    }
                }
                is Resource.Error -> {
                    stopShimmer()
                    response.message?.let { message ->
                        Log.e(mTAG, "observers: $message")
                    }
                }
                is Resource.Loading -> {
                    showShimmer()
                }
            }
        }
        newsViewModel.loadBreakingNews(COUNTRY_CODE_CONST)
    }

    private fun setUpRecyclerView() {
        binding.rv.adapter = adapter
        binding.rv.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rv.addOnScrollListener(this@BreakingNewsFrag.scrollListener)
        adapter.onItemClickListener = {
            val action = BreakingNewsFragDirections.actionBreakingNewsFragToArticleFrag(it)
            findNavController().navigate(action)
        }

    }


    private var isLoading = false
    private var isLastPage = false
    private var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = binding.rv.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                newsViewModel.loadBreakingNews(COUNTRY_CODE_CONST)
                isScrolling = false
            } else {
                binding.rv.setPadding(0, 0, 0, 0)
            }
        }
    }

    private fun showShimmer() {
        binding.shimmerContainer.root.visibility = View.VISIBLE
        binding.shimmerContainer.root.startShimmer()
        binding.rv.visibility = View.GONE
    }

    private fun stopShimmer() {
        binding.shimmerContainer.root.visibility = View.GONE
        binding.shimmerContainer.root.stopShimmer()
        binding.rv.visibility = View.VISIBLE
    }


}