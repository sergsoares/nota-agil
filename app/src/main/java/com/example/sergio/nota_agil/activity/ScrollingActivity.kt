package com.example.sergio.nota_agil.activity

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ArrayAdapter
import com.example.sergio.nota_agil.R
import io.paperdb.Paper
import org.jetbrains.anko.toast
import kotlinx.android.synthetic.main.content_scrolling.list_view_files as listViewFiles

class ScrollingActivity : AppCompatActivity() {

  private var  ITEM: String = ""
  private var CATEGORY: String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scrolling)
    val toolbar = findViewById(R.id.toolbar) as Toolbar
    setSupportActionBar(toolbar)

//    val fab = findViewById(R.id.fab) as FloatingActionButton
//    fab.setOnClickListener { view ->
//      Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//          .setAction("Action", null).show()
//    }

    CATEGORY = intent.getStringExtra("category")
    ITEM = intent.getStringExtra("item")
    toast(CATEGORY)
    toast(ITEM)
    reloadAdapter()
  }

  private fun fetchItem(): ArrayList<String> = Paper.book(CATEGORY).read(ITEM)

  private fun reloadAdapter() {
    val adapter = ArrayAdapter<String>(this, R.layout.categories_navigation, R.id.category_text_view, fetchItem())
    listViewFiles!!.adapter = adapter
  }


}
