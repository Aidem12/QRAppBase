package com.example.qrasist.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.example.qrasist.ApiHelper;
import com.example.qrasist.R;
import com.example.qrasist.adapters.AttendanceAccordionAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Asistencia;
import com.example.qrasist.models.Grupo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class HistorialAsistenciasFragment extends Fragment {

    private DBHelper db;
    private Spinner spinnerGrupo;
    private Button btnFecha;
    private TextView tvInfo;
    private RecyclerView rv;
    private AttendanceAccordionAdapter adapter;

    private int grupoIdSeleccionado = -1;
    private String fechaSeleccionada = null;
    private List<Grupo> listaGrupos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial_asistencias, container, false);

        db = new DBHelper(getContext());
        spinnerGrupo = view.findViewById(R.id.spinner_filtro_grupo);
        btnFecha = view.findViewById(R.id.btn_filtro_fecha);
        tvInfo = view.findViewById(R.id.tv_info_filtro);
        rv = view.findViewById(R.id.rv_historial_asistencias);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AttendanceAccordionAdapter(db);
        rv.setAdapter(adapter);

        configurarFiltros();
        actualizarLista();

        return view;
    }

    private void configurarFiltros() {

        listaGrupos = new ArrayList<>();
        List<String> nombres = new ArrayList<>();
        nombres.add("Todos los grupos");

        String url = ApiHelper.BASE_URL + "grupos";

        Log.d("API_TEST", "Iniciando GET: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("API_TEST", "Éxito GET: " + url + " | Respuesta: " + response.toString());

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Grupo grupo = new Grupo();
                            grupo.setId(Integer.parseInt(obj.getString("id")));
                            grupo.setNombre(obj.getString("nombre"));
                            listaGrupos.add(grupo);
                            nombres.add(grupo.getNombre());
                        }

                        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_spinner_item,
                                nombres
                        );
                        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerGrupo.setAdapter(groupAdapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("API_TEST", "Error GET: " + url + " | Error: " + error.toString());
                }
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);

        // =====================================
        // EVENTOS SPINNER
        // =====================================
        spinnerGrupo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                grupoIdSeleccionado = (position == 0) ? -1 : listaGrupos.get(position - 1).getId();
                actualizarLista();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // =====================================
        // FECHA
        // =====================================
        btnFecha.setOnClickListener(v -> {
            if (fechaSeleccionada != null) {
                fechaSeleccionada = null;
                btnFecha.setText("Por Fecha");
                actualizarLista();
            } else {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(
                        getContext(),
                        (dp, year, month, day) -> {
                            fechaSeleccionada = String.format(
                                    Locale.getDefault(),
                                    "%d-%02d-%02d",
                                    year,
                                    month + 1,
                                    day
                            );
                            btnFecha.setText(fechaSeleccionada);
                            actualizarLista();
                        },
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
    }

    // 🔥 MÉTODO CORREGIDO - AHORA USA LA API 🔥
    private void actualizarLista() {
        String url = ApiHelper.BASE_URL + "asistencias?maestro_id=1";

        Log.d("API_TEST", "GET: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        List<Asistencia> asistencias = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Asistencia a = new Asistencia();
                            a.setId(obj.getInt("id"));
                            a.setAlumnoId(obj.getInt("alumno_id"));
                            a.setFecha(obj.getString("fecha"));
                            a.setEstado(obj.getString("estado"));
                            a.setObservacion(obj.optString("observacion", ""));
                            asistencias.add(a);
                        }

                        Map<String, List<Asistencia>> agrupadas = new LinkedHashMap<>();

                        for (Asistencia a : asistencias) {
                            String key = a.getFecha();
                            if (!agrupadas.containsKey(key)) {
                                agrupadas.put(key, new ArrayList<>());
                            }
                            agrupadas.get(key).add(a);
                        }

                        List<Object> items = new ArrayList<>();
                        for (String header : agrupadas.keySet()) {
                            items.add(header);
                            items.addAll(agrupadas.get(header));
                        }

                        adapter.setData(items);
                        tvInfo.setText(asistencias.size() + " registros encontrados");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("API_TEST", "Error GET asistencias: " + error.toString());
                }
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }
}