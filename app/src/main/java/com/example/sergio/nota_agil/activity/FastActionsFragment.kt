package com.example.sergio.nota_agil.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.sergio.nota_agil.R
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_navigation.*
import org.jetbrains.anko.onClick
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast

import kotlinx.android.synthetic.main.activity_fast_actions.button_new_category as buttonNewCategory
import kotlinx.android.synthetic.main.activity_fast_actions.button_search_item as buttonSearchItem


class FastActionsFragment : Fragment() {

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(com.example.sergio.nota_agil.R.layout.activity_fast_actions, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    buttonSearchItem.onClick {
      val fm = fragmentManager
      var fragmentTransaction: FragmentTransaction? = fm.beginTransaction()
    val newFrag = SearchFragment()
      fragmentTransaction?.replace(R.id.frame_layout, newFrag);
      fragmentTransaction?.addToBackStack(null)
      fragmentTransaction?.commit()
    }

//    buttonLastItem.onClick {
//      val sharedPreferences = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
//      val lastCategory = sharedPreferences.getString("CATEGORY", "")
//      val lastItem = sharedPreferences.getString("ITEM", "")
//
//      val intent = Intent(context, ItemActivity::class.java)
//      intent.putExtra("category",lastCategory)
//      intent.putExtra("item", lastItem)
//
//      if(lastItem != null){
//        startActivity(intent)
//      }else{
//        toast("Não possui último item!")
//      }
//
//
//    }

    buttonNewCategory.onClick { createNewCategory() }
  }

  private fun createNewCategory() {
    val input = EditText(context)
    val dialog = AlertDialog.Builder(context)
         .setView(input)
          .setTitle("Criar Nova Categoria")
          dialog.setPositiveButton("OK") { _, _ ->

          val newCategoryName = input.text.toString()

          if (Paper.book().allKeys.contains(newCategoryName) || newCategoryName.isEmpty()) {
              return@setPositiveButton
          }

          Paper.book(newCategoryName)
          Paper.book().write(newCategoryName, newCategoryName)

          val fm = fragmentManager
          var fragmentTransaction: FragmentTransaction? = fm.beginTransaction()
          val bundle = Bundle()
          bundle.putString("category", Paper.book().read(newCategoryName))
          val newFrag = ItemsFragment()
          newFrag.arguments = bundle

          val adapterCategories = ArrayAdapter<String>(context, R.layout.categories_navigation, R.id.category_text_view, Paper.book().allKeys)
          activity.list_view_categories.adapter = adapterCategories

          fragmentTransaction?.replace(R.id.frame_layout, newFrag);
          fragmentTransaction?.addToBackStack(null)
          fragmentTransaction?.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
          fragmentTransaction?.commit()


        }.show();


  }
}