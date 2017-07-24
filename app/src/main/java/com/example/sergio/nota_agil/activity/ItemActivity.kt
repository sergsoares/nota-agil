package com.example.sergio.nota_agil.activity

import android.R
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import io.paperdb.Paper
import org.jetbrains.anko.*

/**
 * Created by sergio on 24/07/17.
 */

class ItemActivity : AppCompatActivity() {

  lateinit var CATEGORY: String
  lateinit var ITEM: String

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    CATEGORY = intent.getStringExtra("category")
    ITEM = intent.getStringExtra("item")
    defineLayout()
    reloadAdapter()
  }

  private var filesListView: ListView? = null

  private fun defineLayout() {
    verticalLayout {
      button {
        text = "Gravar Audio"
        onClick {
          val itens = if (item == null) ArrayList<String>() else item!!
          itens.add("/123.3gp")
          itens.add("/321.3gp")
          Paper.book(CATEGORY).write(ITEM, itens)
          reloadAdapter()
        }
      }
      textView {
        text = ITEM
        textSize = 42f
      }
      filesListView = listView {
        onItemClick { adapterView, view, i, l ->
          //          startActivity<ItemActivity>("item" to ItemsActivity.itens!!)
        }
      }
    }
  }

  private var item: ArrayList<String>? = null

  private fun reloadAdapter() {
      item = Paper.book(CATEGORY).read(ITEM)
//      item = intent.getSerializableExtra("item") as ArrayList<String>
      val adapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, item)
      filesListView!!.adapter = adapter
  }

}