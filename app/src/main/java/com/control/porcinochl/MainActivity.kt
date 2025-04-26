package com.control.porcinochl

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Navegación a otras actividades
        findViewById<Button>(R.id.btnVerCerdas).setOnClickListener {
            startActivity(Intent(this, ListaCerdasActivity::class.java))
        }

        findViewById<Button>(R.id.btnRegistrarNueva).setOnClickListener {
            startActivity(Intent(this, RegistrarCerdaActivity::class.java))
        }

        findViewById<Button>(R.id.btnVerCalendario).setOnClickListener {
            startActivity(Intent(this, CalendarioActivity::class.java))
        }
    }
}