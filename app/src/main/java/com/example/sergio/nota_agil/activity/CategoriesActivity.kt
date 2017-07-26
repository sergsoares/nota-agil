package com.example.sergio.nota_agil.activity

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import io.paperdb.Paper
import org.jetbrains.anko.*
import java.util.*
import kotlinx.android.synthetic.main.activity_categories.button_new_category as buttonNewCategory
import kotlinx.android.synthetic.main.activity_categories.list_view_categories as listViewCategories

class CategoriesActivity : AppCompatActivity() {

  companion object {
    private val TAG = "CategoriesActivity"
    private var allCategories: MutableList<String>? = null
    private var  categoriesListView: ListView? = null
  }

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Paper.init(this)
//    addedInitialContent()
    defineLayout()
    reloadAdapter()
  }

  private fun defineLayout() {
    verticalLayout {
      button {
        text = "Nova Categoria"
        onClick { createNewCategory() }
      }

      padding = dip(30)

      categoriesListView = listView {
        onItemClick { adapterView, view, i, l ->
          startActivity<ItemsActivity>("category" to Paper.book().allKeys[i])
        }
      }
    }
  }

  private fun addedInitialContent() {
    val itens = ArrayList<String>()
    itens.add("/123.3gp")
    itens.add("/321.3gp")

    Paper.book("programacao").write("Orientada a objetos", itens)
    Paper.book("programacao").write("SOLID", itens)
    Paper.book("programacao").write("Teste", itens)
    Paper.book().write("programacao", "programacao")
  }

  private fun reloadAdapter(){
    val adapterCategories = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Paper.book().allKeys)
    categoriesListView!!.adapter = adapterCategories
  }


  private fun createNewCategory() {
    val input = EditText(this)
    AlertDialog.Builder(this)
        .setView(input)
        .setTitle("Criar Nova Categoria")
        .setPositiveButton("OK") { _, _ ->
          Paper.book(input.text.toString())

          //book default save others book Names
          Paper.book().write(input.text.toString(), input.text.toString())
          reloadAdapter()
        }.show();
  }
}
