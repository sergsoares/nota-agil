package com.example.sergio.nota_agil.activity

import android.R
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import io.paperdb.Paper
import org.jetbrains.anko.onItemClick
import kotlinx.android.synthetic.main.search_fragment.list_view_fetched_cards as listViewItems
import kotlinx.android.synthetic.main.search_fragment.search_view_cards as searchViewItems

class SearchFragment : Fragment() {

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(com.example.sergio.nota_agil.R.layout.search_fragment, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    Paper.init(context)

    val adapter = reloadAdapter()

    listViewItems.onItemClick { adapterView, view, i, l ->
      val intent = Intent(context, ItemActivity::class.java)
      intent.putExtra("category", itemsCategories?.get(i))
      intent.putExtra("item", allItems?.get(i))
      startActivity(intent)
    }
    
    searchViewItems.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String): Boolean {
        adapter.getFilter().filter(query)
        return false
      }

      override fun onQueryTextChange(newText: String): Boolean {
        return false
      }
    })
  }

  private var allItems: ArrayList<String>? = null
  private var itemsCategories: ArrayList<String>? = null

  private fun reloadAdapter(): ArrayAdapter<String> {
    val categoriesKeys = Paper.book().allKeys
    allItems = ArrayList<String>()
    itemsCategories = ArrayList<String>()
    
    for (category in categoriesKeys) {
      val allKeys = Paper.book(category).allKeys
      allItems?.addAll(allKeys)
      for(key in allKeys){
        itemsCategories?.add(category)
      }
    }

    val adapter = ArrayAdapter(context, R.layout.simple_list_item_1, allItems)
    listViewItems.adapter = adapter
    return adapter
  }
}
