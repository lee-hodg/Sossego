package com.example.android.sossego.ui.gratitude.detail

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.sossego.R
import com.example.android.sossego.database.GratitudeItem
import com.example.android.sossego.databinding.DetailGratitudeItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


class GratitudeDetailAdapter(private val deleteClickListener: GratitudeItemListener,
                             private val textChangedListener: GratitudeItemTextChangedListener)
    :ListAdapter<GratitudeDataItem,
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
        viewHolder.bind(deleteClickListener, textChangedListener, gratitudeDataItem.gratitudeItem)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ViewHolder.from(parent)


    class ViewHolder private constructor(private val binding: DetailGratitudeItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(deleteClickListener: GratitudeItemListener,
                 textChangedListener: GratitudeItemTextChangedListener,
                 item: GratitudeItem) {
            binding.gratitudeItem = item
//            binding.gratitudeItemText.doAfterTextChanged{
//                Timber.d("The text changed to ${it.toString()} for item ${item.gratitudeItemId}!")
//                textChangedListener.textChanged(item)
//            }
            binding.gratitudeItemText.onFocusChangeListener  = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus){
                    // do something when edit text lost focus
                    val currentText = binding.gratitudeItemText.text.toString()
                    if (currentText.isNotBlank()) {
                        Timber.d("The text changed to $currentText for item ${item.gratitudeItemId}!")
                        textChangedListener.textChanged(item)
                    }else{
                        Timber.d("The text changed to blank for item ${item.gratitudeItemId}!")
                        textChangedListener.textDeleted(item)
                    }
                }
            }
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

//    fun removeAt(position: Int) {
//        Timber.d("removeAt $position")
//        deleteClickListener.clickListener()
//    }
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


class GratitudeItemTextChangedListener(val textChangedListener: (gratitudeItem: GratitudeItem) -> Unit,
                                       val textDeletedListener: (gratitudeItem: GratitudeItem) -> Unit) {
    fun textChanged(gratitudeItem: GratitudeItem) = textChangedListener(gratitudeItem)
    fun textDeleted(gratitudeItem: GratitudeItem) = textDeletedListener(gratitudeItem)

}


data class GratitudeDataItem(val gratitudeItem: GratitudeItem) {
    val id = gratitudeItem.gratitudeItemId
}


abstract class SwipeToDeleteCallback(context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24)
    private val intrinsicWidth = deleteIcon!!.intrinsicWidth
    private val intrinsicHeight = deleteIcon!!.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        /**
         * To disable "swipe" for specific item return 0 here.
         * For example:
         * if (viewHolder?.itemViewType == YourAdapter.SOME_TYPE) return 0
         * if (viewHolder?.adapterPosition == 0) return 0
         */
        //if (viewHolder.adapterPosition == 10) return 0
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
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

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the red delete background
        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        // Draw the delete icon
        deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteIcon?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }


}
