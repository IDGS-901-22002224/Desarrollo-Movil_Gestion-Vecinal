package org.utl.reddeseguridadvecinal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ir directamente a LoginActivity
        val intent = Intent(this, Login::class.java)
        startActivity(intent)

        // Cerrar MainActivity para que no quede en el stack
        finish()
    }
}
