package com.example.qrasist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.qrasist.adapters.TareaAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.dialogs.TareaDialogFragment;
import com.example.qrasist.models.Tarea;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.android.volley.toolbox.JsonArrayRequest;

public class GestionTareasFragment extends Fragment implements TareaAdapter.TareaListener, TareaDialogFragment.OnTareaGuardadaListener {

    private DBHelper db;
    private SharedPreferences prefs;
    private List<Tarea> listaTareas;
    private TareaAdapter adapter;
    private int maestroId, grupoIdMaestro;

    private TextView tvResumenTareas;
    private RecyclerView rvTareas;
    private LinearLayout layoutEmptyTareas;
    private ExtendedFloatingActionButton fabNuevaTarea; // TIPO CORREGIDO

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tareas, container, false);

        db = new DBHelper(getContext());
        prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);
        maestroId = prefs.getInt("user_id", -1);

        tvResumenTareas = view.findViewById(R.id.tv_resumen_tareas);
        rvTareas = view.findViewById(R.id.rv_tareas);
        layoutEmptyTareas = view.findViewById(R.id.layout_empty_tareas);
        fabNuevaTarea = view.findViewById(R.id.fab_nueva_tarea_fragment);

        rvTareas.setLayoutManager(new LinearLayoutManager(getContext()));

        fabNuevaTarea.setOnClickListener(v -> abrirDialogTarea(null));

        cargarTareas();

        return view;
    }

    private void cargarTareas() {

        String url =
                ApiHelper.BASE_URL +
                        "tareas";

        Log.d("API_TEST", "Iniciando GET: " + url);

        com.android.volley.toolbox.JsonArrayRequest request =
                new com.android.volley.toolbox.JsonArrayRequest(

                        Request.Method.GET,

                        url,

                        null,

                        response -> {

                            Log.d("API_TEST", "Éxito GET: " + url + " | Respuesta: " + response.toString());

                            listaTareas =
                                    new java.util.ArrayList<>();

                            try {

                                String fechaHoy =
                                        new SimpleDateFormat(
                                                "yyyy-MM-dd",
                                                Locale.getDefault()
                                        ).format(new Date());

                                int activas = 0;
                                int vencidas = 0;

                                for (int i = 0; i < response.length(); i++) {

                                    org.json.JSONObject obj =
                                            response.getJSONObject(i);

                                    int maestroIdApi =
                                            Integer.parseInt(
                                                    obj.getString("maestro_id")
                                            );

                                    // SOLO TAREAS DEL MAESTRO
                                    if (maestroIdApi != maestroId) {
                                        continue;
                                    }

                                    Tarea tarea =
                                            new Tarea();

                                    tarea.setId(
                                            Integer.parseInt(
                                                    obj.getString("id")
                                            )
                                    );

                                    tarea.setTitulo(
                                            obj.getString("titulo")
                                    );

                                    tarea.setDescripcion(
                                            obj.getString("descripcion")
                                    );

                                    tarea.setFechaAsignacion(
                                            obj.getString("fecha_asignacion")
                                    );

                                    tarea.setFechaLimite(
                                            obj.getString("fecha_limite")
                                    );

                                    tarea.setGrupoId(
                                            Integer.parseInt(
                                                    obj.getString("grupo_id")
                                            )
                                    );

                                    tarea.setMaestroId(
                                            Integer.parseInt(
                                                    obj.getString("maestro_id")
                                            )
                                    );

                                    listaTareas.add(tarea);

                                    if (
                                            tarea.getFechaLimite()
                                                    .compareTo(fechaHoy) >= 0
                                    ) {

                                        activas++;

                                    }

                                    else {

                                        vencidas++;

                                    }

                                }

                                tvResumenTareas.setText(

                                        listaTareas.size()

                                                + " tarea(s) · "

                                                + activas

                                                + " activa(s) · "

                                                + vencidas

                                                + " vencida(s)"

                                );

                                if (listaTareas.isEmpty()) {

                                    layoutEmptyTareas
                                            .setVisibility(View.VISIBLE);

                                    rvTareas
                                            .setVisibility(View.GONE);

                                }

                                else {

                                    layoutEmptyTareas
                                            .setVisibility(View.GONE);

                                    rvTareas
                                            .setVisibility(View.VISIBLE);

                                }

                                adapter =
                                        new TareaAdapter(

                                                getContext(),

                                                listaTareas,

                                                db,

                                                this

                                        );

                                rvTareas.setAdapter(adapter);

                            }

                            catch (Exception e) {

                                e.printStackTrace();

                            }

                        },

                        error -> {

                            Log.e("API_TEST", "Error GET: " + url + " | Error: " + error.toString());

                            Snackbar.make(

                                    rvTareas,

                                    "Error cargando tareas",

                                    Snackbar.LENGTH_SHORT

                            ).show();

                        }

                );

        RequestQueue queue =
                Volley.newRequestQueue(requireContext());

        queue.add(request);

    }

    private void abrirDialogTarea(Tarea tarea) {

        TareaDialogFragment dialog =
                TareaDialogFragment.newInstance(

                        tarea,

                        maestroId

                );

        dialog.setOnTareaGuardadaListener(this);

        dialog.show(
                getChildFragmentManager(),
                "dialog_tarea"
        );

    }

    private void mostrarConfirmacionEliminar(Tarea tarea) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar tarea")
                .setMessage("¿Eliminar '" + tarea.getTitulo() + "'?\nSe eliminará el registro de todos los alumnos.")
                .setPositiveButton("Eliminar", (dialog, which) -> {

                    String url =

                            ApiHelper.BASE_URL +

                                    "tareas/" +

                                    tarea.getId();

                    Log.d("API_TEST", "Iniciando DELETE: " + url);

                    com.android.volley.toolbox.StringRequest request =

                            new com.android.volley.toolbox.StringRequest(

                                    Request.Method.DELETE,

                                    url,

                                    response -> {

                                        Log.d("API_TEST", "Éxito DELETE: " + url + " | Respuesta: " + response);

                                        Snackbar.make(

                                                rvTareas,

                                                "Tarea eliminada",

                                                Snackbar.LENGTH_SHORT

                                        ).show();

                                        cargarTareas();

                                    },

                                    error -> {

                                        Log.e("API_TEST", "Error DELETE: " + url + " | Error: " + error.toString());

                                        Snackbar.make(

                                                rvTareas,

                                                "Error eliminando tarea",

                                                Snackbar.LENGTH_SHORT

                                        ).show();

                                    }

                            );

                    RequestQueue queue =
                            Volley.newRequestQueue(requireContext());

                    queue.add(request);

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditar(Tarea tarea) {
        abrirDialogTarea(tarea);
    }

    @Override
    public void onEliminar(Tarea tarea) {
        mostrarConfirmacionEliminar(tarea);
    }

    @Override
    public void onGuardada() {
        cargarTareas();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarTareas();
    }
}