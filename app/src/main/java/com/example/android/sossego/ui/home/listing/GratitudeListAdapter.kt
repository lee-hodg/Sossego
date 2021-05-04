package com.example.android.sossego.ui.home

import com.example.android.sossego.database.GratitudeList
import kotlinx.android.synthetic.main.fragment_gratitude.view.*

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.sossego.databinding.ListItemGratitudeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GratitudeListAdapter(private val clickListener: GratitudeListListener) : ListAdapter<DataItem,
        RecyclerView.ViewHolder>(GratitudeListDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun submitGratitudeList(list: List<GratitudeList>?) {
        adapterScope.launch {
            val items = list?.map { DataItem.GratitudeListItem(it) }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val gratitudeListItem = getItem(position) as DataItem.GratitudeListItem
                holder.bind(clickListener, gratitudeListItem.gratitudeList)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ListItemGratitudeBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: GratitudeListListener, item: GratitudeList) {
            binding.gratitudeList = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemGratitudeBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minumum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class GratitudeListDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class GratitudeListListener(val clickListener: (gratitudeListId: Long) -> Unit) {
    fun onClick(gratitudeList: GratitudeList) = clickListener(gratitudeList.gratitudeListId)
}

sealed class DataItem {
    abstract val id: Long

    data class GratitudeListItem(val gratitudeList: GratitudeList): DataItem() {
        override val id = gratitudeList.gratitudeListId
    }

}