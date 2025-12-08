package com.example.proyectofinalmovil3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalmovil3.Evento.Evento
import com.example.proyectofinalmovil3.Evento.EventosAdapter
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var txtNombreUsuario: TextView
    private lateinit var txtCantidadEventos: TextView
    private lateinit var txtCantidadPasos: TextView

    private lateinit var cardBuscarEventos: CardView
    private lateinit var cardMisEstadisticas: CardView

    // Para la lista de eventos en Home (solo 1: el último)
    private lateinit var recyclerEventosHome: RecyclerView
    private lateinit var btnVerTodosEventos: Button
    private val listaEventosHome: MutableList<Evento> = mutableListOf()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference

    private val handler = Handler(Looper.getMainLooper())
    private var pasosSimulados = 0
    private var pasosTotales = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference

        // Referencias UI
        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario)
        txtCantidadEventos = view.findViewById(R.id.txtCantidadEventos)
        txtCantidadPasos = view.findViewById(R.id.txtCantidadPasos)

        cardBuscarEventos = view.findViewById(R.id.cardBuscarEventos)
        cardMisEstadisticas = view.findViewById(R.id.cardMisEstadisticas)

        recyclerEventosHome = view.findViewById(R.id.recyclerEventosHome)
        btnVerTodosEventos = view.findViewById(R.id.btnVerTodosEventos)

        recyclerEventosHome.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        // Navegación
        cardBuscarEventos.setOnClickListener {
            irAFragment(2) // fragment de Eventos
        }

        cardMisEstadisticas.setOnClickListener {
            irAFragment(3) // fragment de Stats
        }

        btnVerTodosEventos.setOnClickListener {
            irAFragment(2) // ir a fragment_eventos
        }

        // Cargar datos de usuario (nombre, pasos)
        cargarDatosDelUsuario()

        // Cargar eventos para contador + preview del último
        cargarEventosHome()

        // Simular pasos si hay permisos
        if (tienePermisoActividadFisica()) {
            iniciarSimulacionPasos()
        } else {
            txtCantidadPasos.text = "0"
        }

        return view
    }

    private fun irAFragment(index: Int) {
        (activity as? ActivityHome)?.irAFragment(index)
    }

    private fun cargarDatosDelUsuario() {
        val email = sharedPreferences.getString("email", null)
        val name = sharedPreferences.getString("name", null)

        if (email == null) {
            txtNombreUsuario.text = "Invitado"
            txtCantidadEventos.text = "0"
            txtCantidadPasos.text = "0"
            return
        }

        val emailKey = email.replace(".", "_")

        val userRef = database.child("usuarios").child(emailKey)
        userRef.child("nombre").get()
            .addOnSuccessListener { snapshot ->
                val nombre = snapshot.getValue(String::class.java) ?: name ?: "Usuario"
                txtNombreUsuario.text = nombre
            }
            .addOnFailureListener {
                txtNombreUsuario.text = name ?: "Usuario"
            }

        val statsRef = userRef.child("estadisticas")
        statsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val pasosTotalesDB = snapshot.child("pasosTotales").getValue(Int::class.java) ?: 0
                    pasosTotales = pasosTotalesDB
                    txtCantidadPasos.text = pasosTotales.toString()
                } else {
                    txtCantidadPasos.text = "0"
                    pasosTotales = 0
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Error al leer estadisticas", error.toException())
            }
        })
    }

    /**
     * Carga TODOS los eventos de /eventos en Firebase:
     * - Actualiza el contador txtCantidadEventos con el total.
     * - Muestra SOLO el último evento en el RecyclerView de Home.
     */
    private fun cargarEventosHome() {
        database.child("eventos").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaEventosHome.clear()

                if (snapshot.exists()) {
                    var totalEventos = 0
                    var ultimoEvento: Evento? = null

                    for (eventoSnapshot in snapshot.children) {
                        val mes = eventoSnapshot.child("mes").getValue(String::class.java) ?: ""
                        val dia = eventoSnapshot.child("dia").getValue(String::class.java) ?: ""
                        val titulo = eventoSnapshot.child("titulo").getValue(String::class.java) ?: ""
                        val descripcion = eventoSnapshot.child("descripcion").getValue(String::class.java) ?: ""
                        val categoria = eventoSnapshot.child("categoria").getValue(String::class.java) ?: ""
                        val inscritos = eventoSnapshot.child("inscritos").getValue(Int::class.java) ?: 0
                        val hora = eventoSnapshot.child("hora").getValue(String::class.java) ?: ""
                        val lugar = eventoSnapshot.child("lugar").getValue(String::class.java) ?: ""

                        if (titulo.isNotEmpty()) {
                            totalEventos++
                            ultimoEvento = Evento(
                                mes = mes,
                                dia = dia,
                                titulo = titulo,
                                descripcion = descripcion,
                                categoria = categoria,
                                inscritos = inscritos,
                                hora = hora,
                                lugar = lugar
                            )
                        }
                    }

                    // Actualizamos contador con el TOTAL
                    txtCantidadEventos.text = totalEventos.toString()

                    // Preview: SOLO el último evento
                    if (ultimoEvento != null) {
                        listaEventosHome.add(ultimoEvento!!)

                        recyclerEventosHome.adapter =
                            EventosAdapter(listaEventosHome) { evento ->
                                clickEventoHome(evento)
                            }
                    } else {
                        recyclerEventosHome.adapter = null
                    }
                } else {
                    txtCantidadEventos.text = "0"
                    recyclerEventosHome.adapter = null
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Error al cargar eventos de Firebase", error.toException())
            }
        })
    }

    private fun clickEventoHome(evento: Evento) {
        // En lugar de abrir previewEvento, mandamos a la pestaña de Eventos
        irAFragment(2)
    }

    // ----------------- Simulación de pasos -----------------

    private fun tienePermisoActividadFisica(): Boolean {
        val permissionState = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun iniciarSimulacionPasos() {
        handler.post(object : Runnable {
            override fun run() {
                pasosSimulados += 5
                val nuevosPasosTotales = pasosTotales + pasosSimulados
                txtCantidadPasos.text = nuevosPasosTotales.toString()
                handler.postDelayed(this, 3000L)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}
