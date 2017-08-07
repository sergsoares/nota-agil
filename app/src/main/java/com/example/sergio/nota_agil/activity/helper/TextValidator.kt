package com.example.sergio.nota_agil.activity.helper

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView

/**
 * Created by sergio on 31/07/17.
 */
abstract class TextValidator(private val textView: TextView) : TextWatcher {
  abstract fun validate(textView: TextView, text: String)

  override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

  }

  override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

  }

  override fun afterTextChanged(s: Editable) {
    val text = textView.text.toString()
    validate(textView, text)
  }
}
