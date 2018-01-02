# EasyAdapterForRecyclerView-kotlin

![easyadapter](https://raw.githubusercontent.com/1993hzw/common/master/Androids/easyadpter.gif)

EasyAdapter的示例代码
```kotlin
...

recyclerView.layoutManager = layoutManager
val data: Array<String> = arrayOf("篮球", "足球", "羽毛球", "乒乓球", "排球", "橄榄球", "棒球")

// 创建一个支持多选的适配器
easyAdapter = object : EasyAdapter<MySelectionHolder>(this, Mode.MULTI_SELECT) {
    override fun whenCreateViewHolder(parent: ViewGroup?, viewType: Int): MySelectionHolder {
        return MySelectionHolder(View.inflate(this@MainActivity, R.layout.item_string, null))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun whenBindViewHolder(holder: MySelectionHolder, position: Int) {
        holder.textView.text = data[position]
    }
}

// 设置选择监听器
easyAdapter.onItemSelectedListener = object : EasyAdapter.OnItemSelectedListener {
    override fun onSelected(position: Int, isSelected: Boolean): Boolean {
        Toast.makeText(this@MainActivity, "selected:$position $isSelected", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onOutOfMax(position: Int) {
        Toast.makeText(this@MainActivity, "onOutOfMax:" + easyAdapter.maxSelection, Toast.LENGTH_SHORT).show()
    }
}

// 设置点击监听器
easyAdapter.setOnItemClickedListener { it ->
    Toast.makeText(this, "clicked:" + it, Toast.LENGTH_SHORT).show()
}

recyclerView.adapter = easyAdapter

...
```