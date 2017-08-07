package com.example.sergio.nota_agil.activity

import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.sergio.nota_agil.R
import io.paperdb.Paper
import org.jetbrains.anko.onClick
import org.jetbrains.anko.onItemClick
import org.jetbrains.anko.toast
import java.util.*
import kotlinx.android.synthetic.main.activity_navigation.button_new_category as buttonNewCategory
import kotlinx.android.synthetic.main.activity_navigation.list_view_categories as listViewCategories


class NavigationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
  override fun onNavigationItemSelected(item: MenuItem): Boolean {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Paper.init(this)
    setContentView(R.layout.activity_navigation)
    val toolbar = findViewById(R.id.toolbar) as Toolbar
    setSupportActionBar(toolbar)

//    fab.setOnClickListener { view ->
//      Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//          .setAction("Action", null).show()
//    }

    val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
    val toggle = ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
    drawer.setDrawerListener(toggle)
    toggle.syncState()

    val navigationView = findViewById(R.id.nav_view) as NavigationView
    navigationView.setNavigationItemSelectedListener(this)
    reloadAdapter()
    putItemsFragment()

    buttonNewCategory.onClick { createNewCategory() }
    registerForContextMenu(listViewCategories)

    openSearchFragment()

    val sharedPreferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
    val result = sharedPreferences.getString("CATEGORY", "")
    toast(result)

  }

  private fun openSearchFragment() {
    val fm = getSupportFragmentManager()
    var fragmentTransaction: FragmentTransaction? = fm.beginTransaction()
    val newFrag = SearchFragment()
    fragmentTransaction?.replace(R.id.frame_layout, newFrag);
    fragmentTransaction?.addToBackStack(null)
    fragmentTransaction?.commit()
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

  private fun putItemsFragment() {
    listViewCategories.onItemClick { adapterView, view, i, l ->

      val fm = getSupportFragmentManager()
      var fragmentTransaction: FragmentTransaction? = fm.beginTransaction()

      val bundle = Bundle()
      bundle.putString("category", Paper.book().allKeys[i])
      val newFrag = ItemsFragment()

      newFrag.arguments = bundle
      fragmentTransaction?.replace(R.id.frame_layout, newFrag);
      fragmentTransaction?.addToBackStack(null)
      fragmentTransaction?.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
      fragmentTransaction?.commit()
      val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
      drawer.closeDrawer(GravityCompat.START)
//        startActivity<ItemsFragment>("category" to Paper.book().allKeys[i])
    }
  }

  // Insert CODE

  private fun reloadAdapter(){
    val adapterCategories = ArrayAdapter<String>(this, R.layout.categories_navigation, R.id.category_text_view ,Paper.book().allKeys)
    listViewCategories.adapter = adapterCategories
  }

  override fun onBackPressed() {
    val drawer = findViewById(R.id.drawer_layout) as  DrawerLayout
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START)
    } else {
      openSearchFragment()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.navigation, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    val id = item.itemId


    if (id == R.id.action_settings) {
      return true
    }

    return super.onOptionsItemSelected(item)
  }

  override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenu.ContextMenuInfo) {

    val allCategories = Paper.book().allKeys
    val info = menuInfo as AdapterView.AdapterContextMenuInfo
    val categoryClicked = allCategories[info.position]

    menu.add("Deletar").setOnMenuItemClickListener {
      Paper.book().delete(categoryClicked)
      Paper.book(categoryClicked).destroy()
      toast("Categoria Deletada")
      reloadAdapter()
      false
    }

    menu.add("Renomear").setOnMenuItemClickListener {
      val input = EditText(this)

      AlertDialog.Builder(this)
          .setView(input)
          .setTitle("Insira novo nome")
          .setPositiveButton("OK") { _, _ ->

            val arrayListTemp = Paper.book(categoryClicked).allKeys
            val newName = input.text.toString()

            Paper.book(newName)
            Paper.book().write(newName, newName)

            //Copy from one book to another
            for(item in arrayListTemp){
              val itemValue = Paper.book(categoryClicked).read<ArrayList<String>>(item)
              Paper.book(newName).write(item, itemValue)
            }

            Paper.book().delete(categoryClicked)
            Paper.book(categoryClicked).destroy()
            reloadAdapter()
          }.show();

      false
    }



  }
}
