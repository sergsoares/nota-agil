package com.example.sergio.nota_agil.activity.activities

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.example.sergio.nota_agil.R
import org.jetbrains.anko.startActivity

class SplashScreentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed({ startActivity<NavigationActivity>() }, 2000)
    }

}
