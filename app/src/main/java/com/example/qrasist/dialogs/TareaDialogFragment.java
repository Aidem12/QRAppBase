package com.example.qrasist.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Grupo;
import com.example.qrasist.models.Tarea;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;



import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.example.qrasist.ApiHelper;

import org.json.JSONObject;


public class TareaDialogFragment extends DialogFragment {

    public interface OnTareaGuardadaListener {
        void onGuardada();
    }

    private Tarea tareaEditar = null;
    private int maestroId = -1;
    private OnTareaGuardadaListener listener;
    private DBHelper db;
    private List<Grupo> listaGrupos;
    private String fechaLimiteSeleccionada = "";

    private TextView tvTitulo, tvFechaSeleccionada, tvGrupoFijo;
    private TextInputEditText etTitulo, etDescripcion;
    private Spinner spinnerGrupo;
    private Button btnSeleccionarFecha, btnCancelar, btnGuardar;

    public static TareaDialogFragment newInstance(
            Tarea tarea,
            int maestroId
    ) {

        TareaDialogFragment fragment =
                new TareaDialogFragment();

        Bundle args = new Bundle();

        args.putSerializable(
                "tarea",
                tarea
        );

        args.putInt(
                "maestro_id",
                maestroId
        );

        fragment.setArguments(args);

        return fragment;

    }

    public void setOnTareaGuardadaListener(OnTareaGuardadaListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tareaEditar =
                    (Tarea) getArguments()
                            .getSerializable("tarea");
            maestroId = getArguments().getInt("maestro_id");
        }
        db = new DBHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_tarea, container, false);

        tvTitulo = view.findViewById(R.id.tv_titulo_dialog_tarea);
        etTitulo = view.findViewById(R.id.et_titulo_tarea_dialog);
        etDescripcion = view.findViewById(R.id.et_descripcion_tarea_dialog);
        spinnerGrupo = view.findViewById(R.id.spinner_grupo_tarea);
        tvGrupoFijo = view.findViewById(R.id.tv_grupo_fijo);
        btnSeleccionarFecha = view.findViewById(R.id.btn_seleccionar_fecha);
        tvFechaSeleccionada = view.findViewById(R.id.tv_fecha_seleccionada);
        btnCancelar = view.findViewById(R.id.btn_cancelar_tarea_dialog);
        btnGuardar = view.findViewById(R.id.btn_guardar_tarea_dialog);

        configurarSpinner();

        if (tareaEditar != null) {
            tvTitulo.setText("Editar Tarea");
            cargarDatosTarea();
            spinnerGrupo.setVisibility(View.GONE);
            tvGrupoFijo.setVisibility(View.VISIBLE);
        } else {
            tvTitulo.setText("Nueva Tarea");
            spinnerGrupo.setVisibility(View.VISIBLE);
            tvGrupoFijo.setVisibility(View.GONE);
        }

        btnSeleccionarFecha.setOnClickListener(v -> mostrarDatePicker());
        btnCancelar.setOnClickListener(v -> dismiss());
        btnGuardar.setOnClickListener(v -> guardarTarea());

        return view;
    }

    private void configurarSpinner() {

        listaGrupos =
                new ArrayList<>();

        List<String> nombresGrupos =
                new ArrayList<>();

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

                                    nombresGrupos.add(
                                            grupo.getNombre()
                                    );

                                }

                                ArrayAdapter<String> adapter =

                                        new ArrayAdapter<>(

                                                getContext(),

                                                android.R.layout.simple_spinner_item,

                                                nombresGrupos

                                        );

                                adapter.setDropDownViewResource(

                                        android.R.layout.simple_spinner_dropdown_item

                                );

                                spinnerGrupo.setAdapter(
                                        adapter
                                );

                            }

                            catch (Exception e) {

                                e.printStackTrace();

                            }

                        },

                        error -> {

                            Log.e("API_TEST", "Error GET: " + url + " | Error: " + error.toString());

                            Toast.makeText(

                                    getContext(),

                                    "Error cargando grupos",

                                    Toast.LENGTH_SHORT

                            ).show();

                        }

                );

        RequestQueue queue =
                Volley.newRequestQueue(
                        requireContext()
                );

        queue.add(request);

    }

    private void cargarDatosTarea() {
        Tarea tarea =
                tareaEditar;
        if (tarea != null) {
            etTitulo.setText(tarea.getTitulo());
            etDescripcion.setText(tarea.getDescripcion());
            fechaLimiteSeleccionada = tarea.getFechaLimite();
            tvFechaSeleccionada.setText(fechaLimiteSeleccionada);
            btnSeleccionarFecha.setText("Cambiar fecha");

            for (Grupo g : listaGrupos) {
                if (g.getId() == tarea.getGrupoId()) {
                    tvGrupoFijo.setText("Grupo: " + g.getNombre());
                    break;
                }
            }
        }
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1); // Mañana
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(year1, monthOfYear, dayOfMonth);
            fechaLimiteSeleccionada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sel.getTime());
            tvFechaSeleccionada.setText(fechaLimiteSeleccionada);
            btnSeleccionarFecha.setText("Cambiar fecha");
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    //GUARDAR TAREA

    private void guardarTarea() {

        String titulo =
                etTitulo.getText()
                        .toString()
                        .trim();

        String descripcion =
                etDescripcion.getText()
                        .toString()
                        .trim();

        if (

                titulo.isEmpty()

                        || descripcion.isEmpty()

                        || fechaLimiteSeleccionada.isEmpty()

        ) {

            Toast.makeText(

                    getContext(),

                    "Completa todos los campos",

                    Toast.LENGTH_SHORT

            ).show();

            return;

        }

        try {

            String fechaHoy =

                    new SimpleDateFormat(

                            "yyyy-MM-dd",

                            Locale.getDefault()

                    ).format(
                            new Date()
                    );

            int grupoId =

                    listaGrupos.get(

                            spinnerGrupo
                                    .getSelectedItemPosition()

                    ).getId();

            JSONObject json =
                    new JSONObject();

            json.put(
                    "titulo",
                    titulo
            );

            json.put(
                    "descripcion",
                    descripcion
            );

            json.put(
                    "fecha_asignacion",
                    fechaHoy
            );

            json.put(
                    "fecha_limite",
                    fechaLimiteSeleccionada
            );

            json.put(
                    "grupo_id",
                    grupoId
            );

            json.put(
                    "maestro_id",
                    maestroId
            );

            String method = tareaEditar != null ? "PUT" : "POST";
            String url = ApiHelper.BASE_URL + "tareas" + (tareaEditar != null ? "/" + tareaEditar.getId() : "");

            Log.d("API_TEST", "Iniciando " + method + ": " + url + " | Datos: " + json.toString());

            JsonObjectRequest request =
                    new JsonObjectRequest(

                            tareaEditar != null
                                    ? Request.Method.PUT
                                    : Request.Method.POST,

                            url,

                            json,

                            response -> {

                                Log.d("API_TEST", "Éxito " + method + ": " + url + " | Respuesta: " + response.toString());

                                Toast.makeText(

                                        getContext(),

                                        "Tarea creada",

                                        Toast.LENGTH_SHORT

                                ).show();

                                if (listener != null) {

                                    listener.onGuardada();

                                }

                                dismiss();

                            },

                            error -> {

                                Log.e("API_TEST", "Error " + method + ": " + url + " | Error: " + error.toString());

                                Toast.makeText(

                                        getContext(),

                                        "Error guardando tarea",

                                        Toast.LENGTH_SHORT

                                ).show();

                            }

                    );

            RequestQueue queue =
                    Volley.newRequestQueue(
                            requireContext()
                    );

            queue.add(request);

        }

        catch (Exception e) {

            e.printStackTrace();

        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}