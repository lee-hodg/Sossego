package com.example.android.sossego.ui.gratitude.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.sossego.database.gratitude.FirebaseGratitudeItem
import com.example.android.sossego.databinding.DetailGratitudeItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Note with ListAdapter we do not need to define data or override getItemCount
 */
class GratitudeDetailAdapter(private val deleteClickListener: GratitudeItemListener,
                             private val textChangedListener: GratitudeItemTextChangedListener)
    :ListAdapter<GratitudeDataItem,
        RecyclerView.ViewHolder>(GratitudeItemDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    /**
     * This will be a list of gratitude list items for a given gratitude list.
     * We wrap them in the dataclass and we offload the work with a co-routine
     */
    fun submitGratitudeItemList(list: List<FirebaseGratitudeItem>?) {
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
        viewHolder.bind(deleteClickListener, textChangedListener, gratitudeDataItem.gratitudeItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ViewHolder.from(parent)

    /**
     * Define the ViewHolder.
     * Notice the constructor is private so can only be called within the class (the "from"
     * companion method).
     * The binding and construction (via from) is encapsulated within the ViewHolder itself
     * which means the adapter doesn't have to worry about it. This is especially handy
     * if our adapter handled multiple different types of viewholders in a given recycler view
     * for example
     *
     * Note that editText also has a doAfterTextChanged method, but I had less success with that
     */
    class ViewHolder private constructor(private val binding: DetailGratitudeItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(deleteClickListener: GratitudeItemListener,
                 textChangedListener: GratitudeItemTextChangedListener,
                 item: FirebaseGratitudeItem
        ) {
            binding.gratitudeItem = item
            binding.clickListener = deleteClickListener
            binding.gratitudeItemText.onFocusChangeListener  =
                textChangedListener.onFocusChangeListener(item)
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
 * These listener classes define methods for what should have if an item is clicked or
 * has its text changed/deleted etc
 * In the Fragment we pass these into the adapter construct as lambda's that call
 * method's on the view-model that handle the actual logic. This separation of conerns
 * means that the adapter doesn't need to know anything about what actually happens on a click
 * and this is defined in the viewmodel
 */

class GratitudeItemListener(val clickListener: (gratitudeListId: String) -> Unit) {
    fun onClick(gratitudeItem: FirebaseGratitudeItem) = clickListener(gratitudeItem.gratitudeItemId)
}

class GratitudeItemTextChangedListener(
    private val textChangedCallback: (gratitudeItem: FirebaseGratitudeItem) -> Unit,
    private val textErasedCallback: (gratitudeItem: FirebaseGratitudeItem) -> Unit) {

    fun onFocusChangeListener(item: FirebaseGratitudeItem) = View.OnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            // Do something when edit text lost focus
            // (this includes Enter, clicking away and navigating back)
            val currentText = item.gratitudeText
            if (currentText.isNotBlank()) {
                Timber.d("The text changed to $currentText for item ${item.gratitudeItemId}!")
                textChangedCallback(item)
            } else {
                Timber.d("The text was erased for item ${item.gratitudeItemId}!")
                textErasedCallback(item)
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


/**
 * Part of the reason for wrapping the GratitudeItem in a data class is we
 * get equality defined for us so the DiffCallback's areContentsTheSame just works
 * Although the GratitudeItem is itself a data class here, so it's not strictly needed for that.
 * Notice we copy the id field to `id` on the dataclass, so areItemsTheSame is quite generic
 */
data class GratitudeDataItem(val gratitudeItem: FirebaseGratitudeItem) {
    val id = gratitudeItem.gratitudeItemId
}
