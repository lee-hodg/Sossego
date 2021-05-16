package com.example.android.sossego.ui.gratitude.listing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.sossego.R
import com.example.android.sossego.convertTimestampToMonthYear
import com.example.android.sossego.database.gratitude.FirebaseGratitudeList
import com.example.android.sossego.databinding.ListItemGratitudeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1


class GratitudeListAdapter(private val clickListener: GratitudeListListener) : ListAdapter<DataItem,
        RecyclerView.ViewHolder>(GratitudeListDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun submitGratitudeList(list: MutableList<FirebaseGratitudeList>?) {
        adapterScope.launch {

            val listItems : MutableList<DataItem> = ArrayList()
            var currentMonthYear: String? = null
            list?.forEach {
                val monthYear = convertTimestampToMonthYear(it.createdDate)
                if(monthYear != currentMonthYear){
                    currentMonthYear = monthYear
                    listItems.add(DataItem.Header(monthYear))
                }
                listItems.add(DataItem.GratitudeListItem(it))
            }

            withContext(Dispatchers.Main) {
                submitList(listItems)
            }
        }
    }

    /**
     * Needed when we have different item types in the list (header vs normal)
     * and we use a different ViewType for each
     * See onCreateViewHolder, which will use this to determine which ViewHolder
     * to create
     */
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.GratitudeListItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val gratitudeListItem = getItem(position) as DataItem.GratitudeListItem
                holder.bind(clickListener, gratitudeListItem.gratitudeList)
            }
            is HeaderViewHolder -> {
                val headerItem = getItem(position) as DataItem.Header
                holder.bind(headerItem.monthYear)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private val headerTextView: TextView = itemView.findViewById(R.id.month_year_text)

        fun bind(monthYear: String) {
            headerTextView.text = monthYear
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header_gratitude_item, parent, false)
                return HeaderViewHolder(view)
            }
        }
    }

    class ViewHolder private constructor(private val binding: ListItemGratitudeBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: GratitudeListListener, item: FirebaseGratitudeList) {
            binding.gratitudeList = item
            binding.clickListener = clickListener
            binding.listElementCount.text = this.itemView.resources.getString(
                    R.string.item_count_template, item.gratitudeItems?.size ?: 0)
            binding.executePendingBindings()

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemGratitudeBinding.inflate(layoutInflater, parent,
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
class GratitudeListDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class GratitudeListListener(val clickListener: (gratitudeListId: String) -> Unit) {
    fun onClick(gratitudeList: FirebaseGratitudeList) = clickListener(gratitudeList.gratitudeListId)
}


sealed class DataItem {
    abstract val id: String?

    data class GratitudeListItem(val gratitudeList: FirebaseGratitudeList): DataItem() {
        override val id = gratitudeList.gratitudeListId
    }


    data class Header(val monthYear: String): DataItem() {
        override val id: Nothing? = null
    }
}


