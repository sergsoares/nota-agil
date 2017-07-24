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

    private var allCategories: MutableList<String>? = null
    private var lv: ListView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Paper.init(this)

        defineLayout()
        loadCategories()
        addedInitialContent()

    }

    private fun defineLayout() {
        verticalLayout {
            button {
                text = "Nova Categoria"
                onClick { createNewCategory() }
            }
            padding = dip(30)
            lv = listView {
                onItemClick { adapterView, view, i, l ->
                    toast(allCategories!!.get(i))
                    startActivity<ItensActivity>("category" to allCategories!!.get(i))
                }
            }
        }
    }

    private fun addedInitialContent() {
        //Criei uma Lista que vai conter meus audios
        val itens = LinkedList<String>()
        itens.add("/123.3gp")
        itens.add("/321.3gp")

        //Criar minha categoria que vai ser representada por Hash
        val categories = HashMap<String, List<String>>()

        //Vou anexar nessa categoria uma lista de itens com o nome a minha categoria
        categories.put("Orientada a objetos", itens)
        categories.put("Orientada a objetos", itens)
        categories.put("Orientada a objetos", itens)

        //Vou adicionar ao book minha categoria com seu nome (key)
        Paper.book().write("programacao", categories)
    }

    private fun loadCategories(){
        allCategories = Paper.book().getAllKeys()
        val mAdapter =
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, allCategories)
        lv?.adapter = mAdapter
    }

    companion object {
        private val TAG = "CategoriesActivity"
    }

    private fun createNewCategory() {
        val input = EditText(this)
        AlertDialog.Builder(this)
                .setView(input)
                .setTitle("Criar Nova Categoria")
                .setPositiveButton("OK") {
                    _, _ -> Paper.book().write(input.text.toString(), HashMap<String, List<String>>())
                    loadCategories()
                }
                .show();
    }

    override fun onResume() {
        super.onResume();
    }


    //    @OnItemClick(R.id.list_view_categories)
    //    public void clickCategory(int position){
    //        Intent intentToItens = new Intent(CategoriesActivity.this, ItensActivity.class);
    //        intentToItens.putExtra("category", (String) mListViewCategories.getItemAtPosition(position));
    //        startActivity(intentToItens);
    //    }


}
