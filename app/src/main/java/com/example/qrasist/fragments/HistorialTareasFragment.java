package com.example.qrasist.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.adapters.TaskAccordionAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Grupo;
import com.example.qrasist.models.Tarea;
import com.example.qrasist.models.TareaAlumno;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.example.qrasist.ApiHelper;

import org.json.JSONObject;

public class HistorialTareasFragment extends Fragment {

    private DBHelper db;
    private Spinner spinnerFiltroGrupo;
    private RecyclerView rvHistorial;
    private int maestroId;
    private List<Grupo> listaGrupos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial_tareas, container, false);

        db = new DBHelper(getContext());
        SharedPreferences prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);
        maestroId = prefs.getInt("user_id", -1);

        // ID corregido para evitar el error de compilación
        spinnerFiltroGrupo = view.findViewById(R.id.spinner_filtro_grupo_tareas);
        rvHistorial = view.findViewById(R.id.rv_historial_tareas);
        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));

        configurarSpinnerGrupos();
        return view;
    }

    private void configurarSpinnerGrupos() {

        listaGrupos =
                new ArrayList<>();

        List<String> nombres =
                new ArrayList<>();

        nombres.add(
                "Selecciona un grupo..."
        );

        String url =

                ApiHelper.BASE_URL +

                        "grupos";

        Log.d("API_TEST", "Iniciando GET: " + url);

        JsonArrayRequest request =

                new JsonArrayRequest(

                        Request.Method.GET,

                        url,

                        null,

                        response -> {

                            Log.d("API_TEST", "Éxito GET: " + url + " | Respuesta: " + response.toString());

                            try {

                                for (
                                        int i = 0;
                                        i < response.length();
                                        i++
                                ) {

                                    JSONObject obj =
                                            response.getJSONObject(i);

                                    Grupo grupo =
                                            new Grupo();

                                    grupo.setId(

                                            Integer.parseInt(

                                                    obj.getString("id")

                                            )

                                    );

                                    grupo.setNombre(

                                            obj.getString("nombre")

                                    );

                                    listaGrupos.add(
                                            grupo
                                    );

                                    nombres.add(
                                            grupo.getNombre()
                                    );

                                }

                                ArrayAdapter<String> adapter =

                                        new ArrayAdapter<>(

                                                requireContext(),

                                                android.R.layout.simple_spinner_item,

                                                nombres

                                        );

                                adapter.setDropDownViewResource(

                                        android.R.layout.simple_spinner_dropdown_item

                                );

                                spinnerFiltroGrupo.setAdapter(
                                        adapter
                                );

                            }

                            catch (Exception e) {

                                e.printStackTrace();

                            }

                        },

                        error -> {
                            Log.e("API_TEST", "Error GET: " + url + " | Error: " + error.toString());
                        }

                );

        RequestQueue queue =

                Volley.newRequestQueue(
                        requireContext()
                );

        queue.add(request);

        spinnerFiltroGrupo
                .setOnItemSelectedListener(

                        new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(

                                    AdapterView<?> parent,

                                    View view,

                                    int position,

                                    long id

                            ) {

                                if (
                                        position > 0
                                ) {

                                    cargarDatosAcordeon(

                                            listaGrupos
                                                    .get(position - 1)
                                                    .getId()

                                    );

                                }

                                else {

                                    rvHistorial.setAdapter(
                                            null
                                    );

                                }

                            }

                            @Override
                            public void onNothingSelected(
                                    AdapterView<?> parent
                            ) {

                            }

                        }

                );

    }

    private void cargarDatosAcordeon(int grupoId) {

        String url =
                ApiHelper.BASE_URL +
                        "tarea-alumno";

        Log.d("API_TEST", "Iniciando GET: " + url);

        JsonArrayRequest request =
                new JsonArrayRequest(

                        Request.Method.GET,

                        url,

                        null,

                        response -> {

                            Log.d("API_TEST", "Éxito GET: " + url + " | Respuesta: " + response.toString());

                            try {

                                Map<Tarea, List<TareaAlumno>> data =
                                        new LinkedHashMap<>();

                                for (
                                        int i = 0;
                                        i < response.length();
                                        i++
                                ) {

                                    JSONObject obj =
                                            response.getJSONObject(i);

                                    android.util.Log.d(
                                            "HISTORIAL",
                                            obj.toString()
                                    );

                                    if (
                                            obj.isNull("grupo_id")
                                    ) {

                                        continue;

                                    }

                                    int grupo =
                                            Integer.parseInt(
                                                    obj.getString("grupo_id")
                                            );

                                    if (
                                            grupo != grupoId
                                    ) {

                                        continue;

                                    }

                                    // =====================================
                                    // TAREA
                                    // =====================================

                                    Tarea tarea =
                                            new Tarea();

                                    tarea.setId(
                                            Integer.parseInt(
                                                    obj.getString("tarea_id")
                                            )
                                    );

                                    tarea.setTitulo(
                                            obj.getString(
                                                    "titulo"
                                            )
                                    );

                                    // =====================================
                                    // ENTREGA
                                    // =====================================

                                    TareaAlumno entrega =
                                            new TareaAlumno();

                                    entrega.setId(
                                            Integer.parseInt(
                                                    obj.getString("id")
                                            )
                                    );

                                    entrega.setComentario(
                                            obj.optString(
                                                    "evidencia",
                                                    ""
                                            )
                                    );

                                    entrega.setEstado(
                                            obj.getString(
                                                    "estado"
                                            )
                                    );

                                    entrega.setEvidencia(
                                            obj.optString(
                                                    "evidencia",
                                                    ""
                                            )
                                    );

                                    entrega.setNombreAlumno(
                                            obj.optString(
                                                    "nombre",
                                                    "Alumno"
                                            )
                                    );

                                    entrega.setComentario(
                                            obj.optString(
                                                    "comentario_alumno",
                                                    ""
                                            )
                                    );

                                    // =====================================
                                    // AGRUPAR
                                    // =====================================

                                    if (
                                            !data.containsKey(
                                                    tarea
                                            )
                                    ) {

                                        data.put(
                                                tarea,
                                                new ArrayList<>()
                                        );

                                    }

                                    data.get(tarea)
                                            .add(entrega);

                                }

                                rvHistorial.setAdapter(

                                        new TaskAccordionAdapter(

                                                requireContext(),

                                                data,

                                                db

                                        )

                                );

                            }

                            catch (Exception e) {

                                e.printStackTrace();

                            }

                        },

                        error -> {
                            Log.e("API_TEST", "Error GET: " + url + " | Error: " + error.toString());
                        }

                );

        RequestQueue queue =
                Volley.newRequestQueue(
                        requireContext()
                );

        queue.add(request);

    }
}