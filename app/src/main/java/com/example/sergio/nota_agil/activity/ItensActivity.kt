package com.example.sergio.nota_agil.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.sergio.nota_agil.R
import kotlinx.android.synthetic.main.activity_categories.button_new_category as buttonNewCategory
import kotlinx.android.synthetic.main.activity_categories.list_view_categories as listViewCategories

class ItensActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        val intent = intent

        buttonNewCategory.setOnClickListener { _ ->     }

        val s = intent.getSerializableExtra("category") as String

    }

    companion object {
        private val TAG = "CategoriesActivity"
    }
}