package com.example.proyectofinalmovil3.Evento

data class Evento(
    val mes: String = "",
    val dia: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val inscritos: Int = 0,
    val hora: String = "",
    val lugar: String = ""
)
