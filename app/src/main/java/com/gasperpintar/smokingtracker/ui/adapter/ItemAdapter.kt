package com.gasperpintar.smokingtracker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.gasperpintar.smokingtracker.R

class ItemAdapter<T : ViewBinding>(
    private val items: List<Item>,
    private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> T,
    private val onPageClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<ItemAdapter<T>.PageViewHolder>() {

    data class Item(
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

        fun bind(item: Item) {
            pageTitle.text = item.title
            pageValue.text = item.value
            pageCaption?.text = item.caption.orEmpty()
            pageCaption?.isVisible = !item.caption.isNullOrBlank()

            binding.root.setOnClickListener {
                item.url?.let { url ->
                    onPageClick?.invoke(url)
                }
            }
            binding.root.isClickable = item.url != null && onPageClick != null
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
        holder.bind(items[position])
    }

    @Override
    override fun getItemCount(): Int {
        return items.size
    }

    companion object {
        fun <T : ViewBinding> setupPager(
            pager: ViewPager2,
            indicator: TextView,
            items: List<Item>,
            bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> T,
            onPageClick: ((String) -> Unit)? = null
        ) {
            pager.adapter = ItemAdapter(items = items, bindingInflater = bindingInflater, onPageClick = onPageClick)

            fun updateIndicator(
                position: Int
            ) {
                indicator.text = "%d / %d".format(position + 1, items.size)
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