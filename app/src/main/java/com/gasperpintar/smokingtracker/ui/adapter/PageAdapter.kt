package com.gasperpintar.smokingtracker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.gasperpintar.smokingtracker.R

class PageAdapter<T : ViewBinding>(
    private val pages: List<Page>,
    private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> T,
    private val onPageClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<PageAdapter<T>.PageViewHolder>() {

    data class Page(
        val title: String,
        val value: String,
        val caption: String? = null,
        val url: String? = null
    )

    inner class PageViewHolder(
        private val binding: T
    ) : RecyclerView.ViewHolder(binding.root) {

        private val pageTitle: TextView = binding.root.findViewById(R.id.page_title)
        private val pageValue: TextView = binding.root.findViewById(R.id.page_value)
        private val pageCaption: TextView? = binding.root.findViewById(R.id.page_caption)

        fun bind(page: Page) {
            pageTitle.text = page.title
            pageValue.text = page.value
            pageCaption?.text = page.caption.orEmpty()
            pageCaption?.isVisible = !page.caption.isNullOrBlank()

            binding.root.setOnClickListener {
                page.url?.let { url ->
                    onPageClick?.invoke(url)
                }
            }
            binding.root.isClickable = page.url != null && onPageClick != null
        }
    }

    @Override
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PageViewHolder {
        val binding = bindingInflater(LayoutInflater.from(parent.context), parent, false)
        binding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return PageViewHolder(binding)
    }

    @Override
    override fun onBindViewHolder(
        holder: PageViewHolder,
        position: Int
    ) {
        holder.bind(pages[position])
    }

    @Override
    override fun getItemCount(): Int {
        return pages.size
    }

    companion object {
        fun <T : ViewBinding> setupPager(
            pager: ViewPager2,
            indicator: TextView,
            pages: List<Page>,
            bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> T,
            onPageClick: ((String) -> Unit)? = null
        ) {
            pager.adapter = PageAdapter(pages = pages, bindingInflater = bindingInflater, onPageClick = onPageClick)

            fun updateIndicator(
                position: Int
            ) {
                indicator.text = "%d / %d".format(position + 1, pages.size)
            }

            updateIndicator(position = 0)
            pager.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    @Override
                    override fun onPageSelected(position: Int) {
                        updateIndicator(position)
                    }
                }
            )
        }
    }
}