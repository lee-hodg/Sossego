package com.example.android.sossego.ui.journal.listing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.sossego.R
import com.example.android.sossego.convertTimestampToMonthYear
import com.example.android.sossego.database.journal.FirebaseJournalEntry
import com.example.android.sossego.databinding.JournalEntryListingItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1


class JournalEntryListAdapter(private val clickListener: JournalEntryListener) : ListAdapter<DataItem,
        RecyclerView.ViewHolder>(JournalEntryDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun submitJournalEntryList(list: List<FirebaseJournalEntry>?) {
        adapterScope.launch {

            val listItems : MutableList<DataItem> = ArrayList()
            var currentMonthYear: String? = null
            list?.forEach {
                val monthYear = convertTimestampToMonthYear(it.createdDate)
                if(monthYear != currentMonthYear){
                    currentMonthYear = monthYear
                    listItems.add(DataItem.Header(monthYear))
                }
                listItems.add(DataItem.JournalEntryItem(it))
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
            is DataItem.JournalEntryItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val journalEntryItem = getItem(position) as DataItem.JournalEntryItem
                holder.bind(clickListener, journalEntryItem.journalEntry)
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

    class ViewHolder private constructor(private val binding: JournalEntryListingItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: JournalEntryListener, item: FirebaseJournalEntry) {
            binding.journalEntry = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = JournalEntryListingItemBinding.inflate(layoutInflater, parent,
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
class JournalEntryDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class JournalEntryListener(val clickListener: (journalEntryId: String) -> Unit) {
    fun onClick(journalEntry: FirebaseJournalEntry) = journalEntry.journalEntryId?.let {
        clickListener(
            it
        )
    }
}


sealed class DataItem {
    abstract val id: String?

    data class JournalEntryItem(val journalEntry: FirebaseJournalEntry): DataItem() {
        override val id = journalEntry.journalEntryId
    }

    data class Header(val monthYear: String): DataItem() {
        override val id: Nothing? = null
    }
}


