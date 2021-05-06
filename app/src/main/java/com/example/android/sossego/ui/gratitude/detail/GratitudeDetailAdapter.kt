package com.example.android.sossego.ui.gratitude.detail

import com.example.android.sossego.database.GratitudeList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.sossego.database.GratitudeItem
import com.example.android.sossego.databinding.DetailGratitudeItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GratitudeDetailAdapter(private val deleteClickListener: GratitudeItemListener) : ListAdapter<GratitudeDataItem,
        RecyclerView.ViewHolder>(GratitudeItemDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    /**
     * This will be a list of gratitude list elements/items
     */
    fun submitGratitudeItemList(list: List<GratitudeItem>?) {
        adapterScope.launch {
            val items = list?.map { GratitudeDataItem(it) }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        val gratitudeDataItem = getItem(position) as GratitudeDataItem
        viewHolder.bind(deleteClickListener, gratitudeDataItem.gratitudeItem)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ViewHolder.from(parent)


    class ViewHolder private constructor(private val binding: DetailGratitudeItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(deleteClickListener: GratitudeItemListener, item: GratitudeItem) {
            binding.gratitudeItem = item
            binding.clickListener = deleteClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DetailGratitudeItemBinding.inflate(layoutInflater, parent,
                        false)
                return ViewHolder(binding)
            }
        }
    }
}

/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minimum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class GratitudeItemDiffCallback : DiffUtil.ItemCallback<GratitudeDataItem>() {
    override fun areItemsTheSame(oldItem: GratitudeDataItem, newItem: GratitudeDataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GratitudeDataItem, newItem: GratitudeDataItem): Boolean {
        return oldItem == newItem
    }
}

class GratitudeItemListener(val clickListener: (gratitudeListId: Long) -> Unit) {
    fun onClick(gratitudeItem: GratitudeItem) = clickListener(gratitudeItem.gratitudeItemId)
}

data class GratitudeDataItem(val gratitudeItem: GratitudeItem) {
    val id = gratitudeItem.gratitudeItemId
}

