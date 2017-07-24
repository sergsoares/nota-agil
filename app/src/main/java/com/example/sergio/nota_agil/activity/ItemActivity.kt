package com.example.sergio.nota_agil.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by sergio on 24/07/17.
 */

class ItemActivity : AppCompatActivity() {

  private var  item: String? = null

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    item = intent.getStringExtra("item")
  }
}