package com.example.sergio.nota_agil.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.sergio.nota_agil.R
import io.paperdb.Paper
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onItemClick
import java.util.*
import kotlinx.android.synthetic.main.items_fragment.button_new_item as buttonNewItem
import kotlinx.android.synthetic.main.items_fragment.category_name_text_view as categoryNameTextView
import kotlinx.android.synthetic.main.items_fragment.itens_list_view as itensListView

class ItemsFragment : Fragment() {

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(com.example.sergio.nota_agil.R.layout.items_fragment, container, false)
  }

  private var CATEGORY: String? = ""

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val bundle = arguments
    if (bundle != null) {

      CATEGORY = bundle.getString("category")
      categoryNameTextView.text = CATEGORY
    }
    Paper.init(context)
    reloadAdapter()

    buttonNewItem.onClick {
      createNewItem()

    }

    itensListView.onItemClick { adapterView, view, i, l ->
      val intent = Intent(context, ItemActivity::class.java)
      intent.putExtra("category",CATEGORY)
      intent.putExtra("item", fetchItems()[i])

      val settings = context.getSharedPreferences("PREFERENCES", 0)
      val editor = settings.edit()

      editor.putString("CATEGORY", CATEGORY)
      editor.putString("ITEM", fetchItems()[i])
      editor.commit()

      startActivity(intent)
    }
    registerForContextMenu(itensListView)
  }

  private fun createNewItem() {
    val input = EditText(context)
    AlertDialog.Builder(context)
        .setView(input)
        .setTitle("Criar novo Item")
        .setPositiveButton("OK") { _, _ ->
          Paper.book(CATEGORY).write(input.text.toString(), ArrayList<String>())
          reloadAdapter()
        }.show();
  }

  private fun reloadAdapter() {
    val adapter = ArrayAdapter<String>(context, R.layout.custom_layout, R.id.category_text_view ,fetchItems())
    itensListView!!.adapter = adapter
  }

  private fun fetchItems() = Paper.book(CATEGORY).allKeys

  override fun onCreateContextMenu(
      menu: ContextMenu,
      view: View,
      menuInfo: ContextMenu.ContextMenuInfo) {

    val allCategories = fetchItems()
    val info = menuInfo as AdapterView.AdapterContextMenuInfo
    val itemClicked = allCategories[info.position]

    val deletar = menu.add("Deletar")
    val renomear = menu.add("Renomear")

    deletar.setOnMenuItemClickListener {
      Paper.book(CATEGORY).delete(itemClicked)
      reloadAdapter()
      true
    }

    renomear.setOnMenuItemClickListener {
      val input = EditText(context)
      AlertDialog.Builder(context)
          .setView(input)
          .setTitle("Insira novo nome")
          .setPositiveButton("OK") { _, _ ->
            val arrayListTemp = Paper.book(CATEGORY).read<ArrayList<String>>(itemClicked)
            Paper.book(CATEGORY).write(input.text.toString(), arrayListTemp)
            Paper.book(CATEGORY).delete(itemClicked)
            reloadAdapter()
          }.show();
      true
    }
  }
}