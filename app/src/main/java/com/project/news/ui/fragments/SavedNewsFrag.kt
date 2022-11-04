package com.project.news.ui.fragments

import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.project.news.R
import com.project.news.adapters.NewsAdapter
import com.project.news.databinding.FragmentSavedNewsBinding
import com.project.news.ui.fragments.base.BaseFragment
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

/*
* Fetches and List all article from room database <article_db.db> to recyclerview.
* ItemTouchHelperCallback is used to provide swipe gesture for deletion from database.
* On swipe, it shows delete icon, for that third party library is used https://github.com/xabaras/RecyclerViewSwipeDecorator .
* */

class SavedNewsFrag : BaseFragment(R.layout.fragment_saved_news) {
    private lateinit var binding: FragmentSavedNewsBinding
    private val adapter: NewsAdapter by lazy { NewsAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSavedNewsBinding.bind(view)
        setUpRecyclerView()
        obServer()
    }

    private fun obServer() {
        newsViewModel.getSavedNews().observe(viewLifecycleOwner) {
            if (it?.isNotEmpty() == true) {
                adapter.asyncList.submitList(it)
                binding.lottieAnimation.visibility = View.GONE
                binding.rv.visibility = View.VISIBLE
            } else {
                binding.lottieAnimation.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.rv.adapter = adapter
        binding.rv.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        adapter.onItemClickListener = {
            val action = SavedNewsFragDirections.actionSavedNewsFragToArticleFrag(it)
            findNavController().navigate(action)
        }
        ItemTouchHelper(itemTouchHelperCallback).apply { attachToRecyclerView(binding.rv) }

    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val article = adapter.asyncList.currentList[position]
            newsViewModel.deleteNews(article)

            Snackbar.make(binding.root, "Successfully deleted item", Snackbar.LENGTH_LONG).setAction("UNDO") {
                newsViewModel.saveNews(article)

            }.show()
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
                .addActionIcon(R.drawable.ic_delete).create().decorate()
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

    }

}