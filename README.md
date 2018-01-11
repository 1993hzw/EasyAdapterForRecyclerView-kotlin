# EasyAdapterForRecyclerView-kotlin


#### 介绍
EasyAdapter是用于RecyclerView的适配器，在原有的适配器基础上可支持设置点击模式、单选和多选模式，并监听相应的事件。在多选模式下，可设置最大可选数量，以及提供了全选、反选等接口。

![easyadapter](https://raw.githubusercontent.com/1993hzw/common/master/Androids/easyadapter.gif)


#### EasyAdapter的示例代码

主要是给RecyclerView设置一个继承了EasyAdapter的适配器，适配器的用法跟官方提供的适配器基本一样。

```kotlin
...

recyclerView.layoutManager = layoutManager
val data: Array<String> = arrayOf("篮球", "足球", "羽毛球", "乒乓球", "排球", "橄榄球", "棒球")

// 创建一个支持多选的适配器
easyAdapter = object : EasyAdapter<MySelectionHolder>(this, Mode.MULTI_SELECT) {
    override fun getItemCount(): Int {
        return data.size
    }

    // 创建ViewHolder
    override fun whenCreateViewHolder(parent: ViewGroup?, viewType: Int): MySelectionHolder {
        return MySelectionHolder(View.inflate(this@MainActivity, R.layout.item_string, null))
    }

    // 绑定数据
    override fun whenBindViewHolder(holder: MySelectionHolder, position: Int) {
        holder.textView.text = data[position]
    }
}

// 设置点击监听器
easyAdapter.setOnItemClickedListener { it ->
    Toast.makeText(this, "clicked:" + it, Toast.LENGTH_SHORT).show()
}

// 设置单选监听器
easyAdapter.onSingleSelectListener = object : EasyAdapter.OnSingleSelectListener {
    override fun onSelected(position: Int) {
        Toast.makeText(this@MainActivity, "selected:$position", Toast.LENGTH_SHORT).show()
    }

}

// 设置多选监听器
easyAdapter.onMultiSelectListener = object : EasyAdapter.OnMultiSelectListener {
    override fun onSelected(position: Int, isSelected: Boolean) {
        Toast.makeText(this@MainActivity, "selected:$position $isSelected", Toast.LENGTH_SHORT).show()
    }

    override fun onOutOfMax(position: Int) {
        Toast.makeText(this@MainActivity, "onOutOfMax:" + easyAdapter.maxSelectionCount, Toast.LENGTH_SHORT).show()
    }
}

recyclerView.adapter = easyAdapter

...
```