package com.example.proyectofinalmovil3

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    //variables del entorno grafico
    private lateinit var iniciarSesionButtonVista: Button
    private lateinit var registrarseButtonVista: Button

    private lateinit var iniciarSesionButton: Button
    private lateinit var registrarseButton: Button

    private lateinit var correoInicioSesionInput: TextInputLayout
    private lateinit var contraseñaInicioSesionInput: TextInputLayout

    private lateinit var nombreRegistroInput: TextInputLayout
    private lateinit var correoRegistroInput: TextInputLayout
    private lateinit var contraseñaRegistroInput: TextInputLayout

    private lateinit var vistaInicioSesion: LinearLayout
    private lateinit var vistaRegistro: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //inicializacion de variables del entorno grafico
        iniciarSesionButtonVista = findViewById(R.id.iniciarSesionButtonVista2)
        registrarseButtonVista = findViewById(R.id.registrarseButtonVista1)
        iniciarSesionButton = findViewById(R.id.iniciarSesionButton)
        registrarseButton = findViewById(R.id.registrarseButton)
        correoInicioSesionInput = findViewById(R.id.correoInicioSesionInputLayout)
        contraseñaInicioSesionInput = findViewById(R.id.contrasenaInicioSesionInputLayout)
        nombreRegistroInput = findViewById(R.id.nombreRegistroInputLayout)
        correoRegistroInput = findViewById(R.id.correoRegistroInputLayout)
        contraseñaRegistroInput = findViewById(R.id.contrasenaRegistroInputLayout)
        vistaInicioSesion = findViewById(R.id.inicioSesionLinearLayout)
        vistaRegistro = findViewById(R.id.registroLinearLayout)

        //acciones de botones
        iniciarSesionButtonVista.setOnClickListener { vistaInicioSesion() }
        registrarseButtonVista.setOnClickListener { vistaRegistro() }
    }

    private fun vistaRegistro(){
        vistaInicioSesion.visibility = View.GONE
        vistaRegistro.visibility = View.VISIBLE

    }

    private fun vistaInicioSesion(){
        vistaInicioSesion.visibility = View.VISIBLE
        vistaRegistro.visibility = View.GONE
    }
}