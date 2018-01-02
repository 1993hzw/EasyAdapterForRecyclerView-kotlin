package cn.forward.androids.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * 用于RecyclerView的适配器
 * @param mode 可区分点击模式、单选和多选模式
 * @param maxSelection 用于多选模式，设置最大的选择数量，maxSelection<=0 表示不限制选择数
 */
abstract class EasyAdapter<VH : RecyclerView.ViewHolder>(context: Context, mode: Mode = Mode.CLICK, maxSelection: Int = -1) : RecyclerView.Adapter<EasyAdapter.SelectionViewHolder<VH>>() {

    /**
     * 最大可选的数量，maxSelection<=0表示不限制选择数量
     */
    var maxSelection = maxSelection
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
            if (field != Mode.SINGLE_SELECT && value == Mode.SINGLE_SELECT) { // 切换到单选，清空选择项
                selectedSet.clear()
            }
            field = value
            notifyDataSetChanged()
        }

    var onItemClickedListener: OnItemClickedListener? = null
    var onItemSelectedListener: OnItemSelectedListener? = null

    fun setOnItemSelectedListener(listener: (position: Int, isSelected: Boolean) -> Boolean) {
        onItemSelectedListener = object : OnItemSelectedListener {
            override fun onOutOfMax(position: Int) {
            }

            override fun onSelected(position: Int, isSelected: Boolean): Boolean {
                return listener(position, isSelected)
            }
        }
    }

    fun setOnItemClickedListener(listener: (position: Int) -> Unit) {
        onItemClickedListener = object : OnItemClickedListener {
            override fun onClicked(position: Int) {
                listener(position)
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
                selectedSet.clear()
                selectedSet.add(pos)
                onItemSelectedListener?.onSelected(pos, true)
                notifyDataSetChanged()
            } else if (mode == Mode.MULTI_SELECT) {

                if (maxSelection > 0 &&
                        selectedSet.size >= maxSelection // 达到限制
                        && !selectedSet.contains(pos)) { // 且选择新的一项
                    onItemSelectedListener?.onOutOfMax(pos)
                    return@setOnClickListener
                }
                val isSelected = selectedSet.contains(pos)
                if (onItemSelectedListener?.onSelected(pos, !isSelected) == true) {
                    if (isSelected) {
                        selectedSet.remove(pos)
                    } else {
                        selectedSet.add(pos)
                    }
                    notifyDataSetChanged()
                }

                //   Log.e("test", "$maxSelection ${selectedSet.size} {${selectedSet}}")
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: SelectionViewHolder<VH>, position: Int) {
        whenBindViewHolder(holder.viewHolder, position)
        holder.itemView.tag = position
        if (mode == Mode.SINGLE_SELECT && selectedSet.size == 0) { // 单选默认选择第一个
            selectedSet += 0
        }
        when (mode) {
            Mode.CLICK -> holder.itemView.isSelected = false
            Mode.SINGLE_SELECT, Mode.MULTI_SELECT -> holder.itemView.isSelected = selectedSet.contains(position)
        }

    }

    /**
     * 全选
     */
    fun selectAll() {
        if (mode == Mode.SINGLE_SELECT) return
        if (maxSelection > 0) return

        selectedSet += 0..itemCount

        notifyDataSetChanged()
    }

    fun unselectAll() {
        if (mode == Mode.SINGLE_SELECT) return
        selectedSet.clear()
        notifyDataSetChanged()
    }

    /**
     * 反选
     */
    fun reverseSelected() {
        if (mode == Mode.SINGLE_SELECT) return
        if (maxSelection > 0) return

        val set = HashSet(selectedSet)
        selectedSet += 0..itemCount
        selectedSet.removeAll(set)

        notifyDataSetChanged()
    }

    fun select(position: Int) {
        if (maxSelection > 1 && selectedSet.size >= maxSelection) {
            return
        }

        if (mode == Mode.SINGLE_SELECT) { // 单选
            selectedSet.clear()
        }
        selectedSet += position
    }

    fun unselect(position: Int) {
        if (mode == Mode.SINGLE_SELECT) return

        selectedSet.remove(position)
    }

    fun isSelected(position: Int): Boolean {
        return selectedSet.contains(position)
    }

    fun selectedSet() = LinkedHashSet<Int>(selectedSet)

    fun getSingleSelectedPosition(): Int {
        if (mode != Mode.SINGLE_SELECT) return -1
        return selectedSet.last()
    }

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
     * 选择item的监听器
     */
    interface OnItemSelectedListener {
        /**
         * 选择的时候回调
         * @param position 选择的索引位置
         * @param isSelected true为选中，false取消选中
         * @return 返回true表示该次选择生效，false为不生效
         */
        fun onSelected(position: Int, isSelected: Boolean): Boolean

        /**
         * 超出最大选择数量时回调
         */
        fun onOutOfMax(position: Int)
    }

}

