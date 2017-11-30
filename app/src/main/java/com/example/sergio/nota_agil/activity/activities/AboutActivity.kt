package com.example.sergio.nota_agil.activity.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar

import com.example.sergio.nota_agil.R

class AboutActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_about)
    val toolbar = findViewById(R.id.toolbar_about) as Toolbar
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true);
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return true
  }

}
