package cn.forward.androids.views.demo

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import cn.forward.androids.views.EasyAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    lateinit var adapter: EasyAdapter<MySelectionHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.orientation = GridLayoutManager.HORIZONTAL
        listView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                outRect?.set(10, 10, 10, 10)
            }
        })

        listView.layoutManager = layoutManager
        val data: Array<String> = arrayOf("篮球", "足球", "羽毛球", "乒乓球", "排球", "橄榄球", "棒球")

        adapter = object : EasyAdapter<MySelectionHolder>(this, Mode.MULTI_SELECT) {
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
        adapter.onItemSelectedListener = object : EasyAdapter.OnItemSelectedListener {
            override fun onSelected(position: Int, isSelected: Boolean): Boolean {
                Toast.makeText(this@MainActivity, "selected:$position $isSelected", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onOutOfMax(position: Int) {
                Toast.makeText(this@MainActivity, "onOutOfMax:" + adapter.maxSelection, Toast.LENGTH_SHORT).show()
            }
        }

        // 设置点击监听器
        adapter.setOnItemClickedListener { it ->
            Toast.makeText(this, "clicked:" + it, Toast.LENGTH_SHORT).show()
        }

        listView.adapter = adapter


        val dataList = ArrayList<String>()
        dataList.add("点击模式")
        dataList.add("单选模式")
        dataList.add("多选模式")

        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMode.adapter = spinnerAdapter
        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                panelMultiSel.visibility = View.GONE
                when (position) { // 设置模式
                    0 -> adapter.mode = EasyAdapter.Mode.CLICK
                    1 -> adapter.mode = EasyAdapter.Mode.SINGLE_SELECT
                    2 -> {
                        adapter.mode = EasyAdapter.Mode.MULTI_SELECT
                        panelMultiSel.visibility = View.VISIBLE
                    }
                }
            }
        }
        spinnerMode.setSelection(2)

        // 设置最大可选数量
        pickerMaxSelect.setOnSelectedListener { scrollPickerView, i ->
            adapter.maxSelection = i
        }
        pickerMaxSelect.data = listOf("0", "1", "2", "3", "4", "5", "6", "7")
        pickerMaxSelect.selectedPosition = 0

    }

    fun selectAll(view: View) {
        adapter.selectAll()
    }

    fun reverseSelectAll(view: View) {
        adapter.reverseSelected()
    }
}

class MySelectionHolder(view: View) : RecyclerView.ViewHolder(view) {
    val textView = view.findViewById<TextView>(R.id.textview)

}
