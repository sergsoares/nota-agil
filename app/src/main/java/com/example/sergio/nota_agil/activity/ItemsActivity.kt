package com.example.sergio.nota_agil.activity

import android.R
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import io.paperdb.Paper
import org.jetbrains.anko.*
import kotlinx.android.synthetic.main.activity_categories.button_new_category as buttonNewCategory
import kotlinx.android.synthetic.main.activity_categories.list_view_categories as listViewCategories

class ItemsActivity : AppCompatActivity() {

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    defineLayout()
    reloadAdapter()
  }

  private fun defineLayout() {
    verticalLayout {
      textView {
        text = intent.getStringExtra("category")
        textSize = 42f
      }
      itensListView = listView {
        onItemClick { adapterView, view, i, l ->
          startActivity<ItemActivity>("item" to itens!!)
        }
      }
    }
  }



  private fun reloadAdapter() {
    itens = Paper.book().read<HashMap<String, List<String>>>(intent.getStringExtra("category"))
    val adapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, itens!!.keys.toList())
    itensListView!!.adapter = adapter
  }

  companion object {
    private val TAG = "ItemsActivity"
    private var itens: HashMap<String, List<String>>? = null
    private var itensListView: ListView? = null
  }

}
