package com.example.sergio.nota_agil.activity

import android.R
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import io.paperdb.Paper
import org.jetbrains.anko.listView
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import kotlinx.android.synthetic.main.activity_categories.button_new_category as buttonNewCategory
import kotlinx.android.synthetic.main.activity_categories.list_view_categories as listViewCategories

class ItensActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val category = intent.getStringExtra("category")
        val itens = Paper.book().read<HashMap<String, List<String>>>(category).keys


        val mAdapter = ArrayAdapter<String>(this, R.layout.simple_list_item_1, itens.toList())

        verticalLayout{
            textView{
                text = category
                textSize = 42f
            }
            listView {
                adapter = mAdapter
            }
        }


    }

    companion object {
        private val TAG = "ItemsActivity"
    }
}
