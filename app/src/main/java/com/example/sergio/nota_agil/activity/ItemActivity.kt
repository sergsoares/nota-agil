package com.example.sergio.nota_agil.activity

import android.R
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import org.jetbrains.anko.listView
import org.jetbrains.anko.onItemClick
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import java.util.*

/**
 * Created by sergio on 24/07/17.
 */

class ItemActivity : AppCompatActivity() {

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    defineLayout()
    reloadAdapter()
  }

  private var filesListView: ListView? = null

  private fun defineLayout() {
    verticalLayout {
      textView {
        text = intent.getSerializableExtra("item").toString()
        textSize = 42f
      }
      filesListView = listView {
        onItemClick { adapterView, view, i, l ->
          //          startActivity<ItemActivity>("item" to ItemsActivity.itens!!)
        }
      }
    }
  }

  private fun reloadAdapter() {
      val item = intent.getSerializableExtra("item") as ArrayList<String>
      val adapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, item)
      filesListView!!.adapter = adapter
  }

}