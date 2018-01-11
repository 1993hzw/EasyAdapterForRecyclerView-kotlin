package cn.forward.androids.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * 用于RecyclerView的适配器
 * @param mode 可区分点击模式、单选和多选模式
 * @param maxSelection 用于多选模式，设置最大的选择数量，maxSelectionCount<=0 表示不限制选择数
 */
abstract class EasyAdapter<VH : RecyclerView.ViewHolder>(context: Context, mode: Mode = Mode.CLICK, maxSelection: Int = -1) : RecyclerView.Adapter<EasyAdapter.SelectionViewHolder<VH>>() {

    /**
     * 最大可选的数量，maxSelectionCount<=0表示不限制选择数量
     */
    var maxSelectionCount = maxSelection
        set(value) {
            field = value
            if (field > 0) {
                while (selectedSet.size > field) {
                    selectedSet -= selectedSet.last()
                }
            }
            notifyDataSetChanged()
        }

    /**
     * 当前模式
     */
    var mode = mode
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    /**
     * 单选项的索引
     */
    var singleSelectedPosition = 0
        set(value) {
            if (field == value) {
                return
            }
            field = value
            onSingleSelectListener?.onSelected(value)
            notifyDataSetChanged()
        }

    var onItemClickedListener: OnItemClickedListener? = null
    var onSingleSelectListener: OnSingleSelectListener? = null
    var onMultiSelectListener: OnMultiSelectListener? = null

    fun setOnItemClickedListener(listener: (position: Int) -> Unit) {
        onItemClickedListener = object : OnItemClickedListener {
            override fun onClicked(position: Int) {
                listener(position)
            }
        }
    }

    fun setSingleSelectListener(listener: (position: Int) -> Unit) {
        onSingleSelectListener = object : OnSingleSelectListener {
            override fun onSelected(position: Int) {
                listener(position)
            }
        }
    }

    fun setMultiSelectListener(listener: (position: Int, isSelected: Boolean) -> Unit) {
        onMultiSelectListener = object : OnMultiSelectListener {
            override fun onOutOfMax(position: Int) {
            }

            override fun onSelected(position: Int, isSelected: Boolean) {
                listener(position, isSelected)
            }
        }
    }


    /**
     * 记录已选择的item
     */
    private val selectedSet = LinkedHashSet<Int>()

    abstract fun whenCreateViewHolder(parent: ViewGroup?, viewType: Int): VH

    abstract fun whenBindViewHolder(holder: VH, position: Int)


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SelectionViewHolder<VH> {
        val vh = whenCreateViewHolder(parent, viewType)

        val viewHolder = SelectionViewHolder(vh)
        viewHolder.itemView.setOnClickListener {
            val pos = viewHolder.itemView.tag as Int
            if (mode == Mode.CLICK) {
                onItemClickedListener?.onClicked(pos)
            } else if (mode == Mode.SINGLE_SELECT) {
                singleSelectedPosition = pos
            } else if (mode == Mode.MULTI_SELECT) {
                if (maxSelectionCount > 0 &&
                        selectedSet.size >= maxSelectionCount // 达到限制
                        && !selectedSet.contains(pos)) { // 且选择新的一项
                    onMultiSelectListener?.onOutOfMax(pos)
                    return@setOnClickListener
                }
                var isSelected = selectedSet.contains(pos)
                if (isSelected) {
                    selectedSet.remove(pos)
                } else {
                    selectedSet.add(pos)
                }
                onMultiSelectListener?.onSelected(pos, !isSelected)
                notifyDataSetChanged()

                //   Log.e("test", "$maxSelectionCount ${selectedSet.size} {${selectedSet}}")
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: SelectionViewHolder<VH>, position: Int) {
        whenBindViewHolder(holder.viewHolder, position)
        holder.itemView.tag = position
        when (mode) {
            Mode.CLICK -> holder.itemView.isSelected = false
            Mode.SINGLE_SELECT -> holder.itemView.isSelected = singleSelectedPosition == position
            Mode.MULTI_SELECT -> holder.itemView.isSelected = selectedSet.contains(position)
        }

    }

    /**
     * 全选，，只在maxSelection不限制可选数时有效
     */
    fun selectAll() {
        if (maxSelectionCount > 0) return

        selectedSet += 0..itemCount
        notifyDataSetChanged()
    }

    fun unselectAll() {
        selectedSet.clear()
        notifyDataSetChanged()
    }

    /**
     * 反选，只在maxSelection不限制可选数时有效
     */
    fun reverseSelected() {
        if (maxSelectionCount > 0) return

        val set = HashSet(selectedSet)
        selectedSet += 0..itemCount
        selectedSet.removeAll(set)

        notifyDataSetChanged()
    }

    /**
     * 选中某项。单选时只有position[0]才生效
     */
    fun select(vararg position: Int) {
        if (mode == Mode.SINGLE_SELECT) { // 单选
            singleSelectedPosition = position[0]
            onSingleSelectListener?.onSelected(singleSelectedPosition)
        } else {
            position.filter { it !in selectedSet && it < itemCount }
                    .forEach {
                        // 检查是否超出限制
                        if (maxSelectionCount > 1 && selectedSet.size >= maxSelectionCount) {
                            onMultiSelectListener?.onOutOfMax(it)
                        } else {
                            selectedSet += it
                            onMultiSelectListener?.onSelected(it, true)
                        }
                    }
            notifyDataSetChanged()
        }
    }

    fun unselect(vararg position: Int) {
        position.filter { it in selectedSet && it < itemCount }
                .forEach {
                    selectedSet -= it
                    onMultiSelectListener?.onSelected(it, false)
                }
        notifyDataSetChanged()
    }


    fun isSelected(position: Int): Boolean {
        return selectedSet.contains(position)
    }

    fun selectedSet() = LinkedHashSet<Int>(selectedSet)

    /**
     * 可区分点击模式、单选和多选模式
     */
    enum class Mode {
        CLICK, SINGLE_SELECT, MULTI_SELECT
    }

    class SelectionViewHolder<out VH : RecyclerView.ViewHolder>(val viewHolder: VH) : RecyclerView.ViewHolder(SelectionItemView(viewHolder))

    private class SelectionItemView(viewHolder: RecyclerView.ViewHolder) : FrameLayout(viewHolder.itemView.context) {
        init {
            addView(viewHolder.itemView)
        }
    }

    /**
     * 点击item的监听器
     */
    interface OnItemClickedListener {
        fun onClicked(position: Int)
    }

    /**
     * 单选的监听器
     */
    interface OnSingleSelectListener {
        fun onSelected(position: Int)
    }

    /**
     * 多选的监听器
     */
    interface OnMultiSelectListener {


        /**
         * 选择的时候回调
         * @param position 选择的索引位置
         * @param isSelected true为选中，false取消选中
         */
        fun onSelected(position: Int, isSelected: Boolean)

        /**
         * 超出最大选择数量时回调
         */
        fun onOutOfMax(position: Int) {

        }

    }

}

