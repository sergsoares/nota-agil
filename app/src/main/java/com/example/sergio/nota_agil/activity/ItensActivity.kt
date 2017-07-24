package com.example.sergio.nota_agil.activity

import android.R
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import io.paperdb.Paper
import org.jetbrains.anko.*
import kotlinx.android.synthetic.main.activity_categories.button_new_category as buttonNewCategory
import kotlinx.android.synthetic.main.activity_categories.list_view_categories as listViewCategories

class ItensActivity : AppCompatActivity() {

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    category = intent.getStringExtra("category")
    loadItems()
    defineLayout()

  }

  private fun defineLayout() {
    layout = verticalLayout {
      textView {
        text = category
        textSize = 42f
      }
      listView {
        onItemClick { adapterView, view, i, l ->
          startActivity<ItemActivity>("item" to itens!!)
        }
      }
    }
  }

  private fun loadItems() {
    itens = Paper.book().read<HashMap<String, List<String>>>(category)
    val adapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, itens!!.keys.toList())
    layout!!.listView().adapter = adapter
  }

  companion object {
    private val TAG = "ItemsActivity"
    private var itens: HashMap<String, List<String>>? = null
    private var layout: LinearLayout? = null
    private var category: String? = null
  }

}
