package com.example.qrasist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.adapters.AsistenciaRecienteAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Asistencia;
import com.example.qrasist.models.Maestro;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.example.qrasist.ApiHelper;

import org.json.JSONObject;

public class InicioFragment extends Fragment {

    private DBHelper db;
    private SharedPreferences prefs;
    private int maestroId, grupoId;
    private TextView tvSaludo, tvNombreMaestro;
    private TextView tvStatAsistencias, tvStatTareas, tvStatAlumnos;
    private RecyclerView rvUltimasAsistencias;
    private ExtendedFloatingActionButton fabNuevaTarea;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        db = new DBHelper(getContext());
        prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);
        maestroId = prefs.getInt("user_id", -1);

        tvSaludo = view.findViewById(R.id.tv_saludo);
        tvNombreMaestro = view.findViewById(R.id.tv_nombre_maestro);
        tvStatAsistencias = view.findViewById(R.id.tv_stat_asistencias);
        tvStatTareas = view.findViewById(R.id.tv_stat_tareas);
        tvStatAlumnos = view.findViewById(R.id.tv_stat_alumnos);
        rvUltimasAsistencias = view.findViewById(R.id.rv_ultimas_asistencias);
        fabNuevaTarea = view.findViewById(R.id.fab_nueva_tarea);

        rvUltimasAsistencias.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarDatos();

        // CORRECCIÓN: Código activado para navegar
        fabNuevaTarea.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GestionTareasActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void cargarDatos() {

        try {

            String nombre = prefs.getString("user_nombre", "Maestro");
            tvNombreMaestro.setText(nombre);

            int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (hora >= 6 && hora < 12) {
                tvSaludo.setText("Buenos días,");
            } else if (hora >= 12 && hora < 18) {
                tvSaludo.setText("Buenas tardes,");
            } else {
                tvSaludo.setText("Buenas noches,");
            }

            // =====================================
            // ALUMNOS
            // =====================================
            String urlAlumnos = ApiHelper.BASE_URL + "alumnos";
            Log.d("API_TEST", "Iniciando GET: " + urlAlumnos);

            JsonArrayRequest alumnosRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    urlAlumnos,
                    null,
                    response -> {
                        Log.d("API_TEST", "Éxito GET: " + urlAlumnos + " | Respuesta: " + response.toString());
                        tvStatAlumnos.setText(String.valueOf(response.length()));
                    },
                    error -> {
                        Log.e("API_TEST", "Error GET: " + urlAlumnos + " | Error: " + error.toString());
                    }
            );

            // =====================================
            // TAREAS
            // =====================================
            String urlTareas = ApiHelper.BASE_URL + "tareas";
            Log.d("API_TEST", "Iniciando GET: " + urlTareas);

            JsonArrayRequest tareasRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    urlTareas,
                    null,
                    response -> {
                        Log.d("API_TEST", "Éxito GET: " + urlTareas + " | Respuesta: " + response.toString());
                        tvStatTareas.setText(String.valueOf(response.length()));
                    },
                    error -> {
                        Log.e("API_TEST", "Error GET: " + urlTareas + " | Error: " + error.toString());
                    }
            );

            // =====================================
            // ASISTENCIAS - 🔥 CORREGIDO 🔥
            // =====================================
            String urlAsistencias = ApiHelper.BASE_URL + "asistencias?maestro_id=" + maestroId;
            Log.d("API_TEST", "Iniciando GET: " + urlAsistencias);

            JsonArrayRequest asistenciasRequest = new JsonArrayRequest(
                    Request.Method.GET,
                    urlAsistencias,
                    null,
                    response -> {
                        Log.d("API_TEST", "Éxito GET: " + urlAsistencias + " | Respuesta: " + response.toString());
                        tvStatAsistencias.setText(String.valueOf(response.length()));
                    },
                    error -> {
                        Log.e("API_TEST", "Error GET: " + urlAsistencias + " | Error: " + error.toString());
                    }
            );

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(alumnosRequest);
            queue.add(tareasRequest);
            queue.add(asistenciasRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatos();
    }
}