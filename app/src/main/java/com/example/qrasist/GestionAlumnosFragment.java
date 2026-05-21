package com.example.qrasist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.adapters.AlumnoAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.dialogs.AlumnoDialogFragment;
import com.example.qrasist.dialogs.GrupoDialogFragment;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Grupo;
import com.example.qrasist.models.Maestro;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.android.volley.toolbox.StringRequest;

import com.example.qrasist.ApiHelper;

import org.json.JSONObject;




public class GestionAlumnosFragment extends Fragment implements AlumnoAdapter.AlumnoListener,
        AlumnoDialogFragment.OnAlumnoGuardadoListener, GrupoDialogFragment.OnGrupoGuardadoListener {

    private DBHelper db;
    private SharedPreferences prefs;
    private List<Alumno> listaAlumnos;
    private AlumnoAdapter adapter;
    private List<Grupo> listaGrupos;
    private int grupoIdSeleccionado;

    private SearchView searchView;
    private TextView tvTotalAlumnos;
    private RecyclerView rvAlumnos;
    private LinearLayout layoutEmpty;
    private Spinner spinnerFiltroGrupo;
    private ExtendedFloatingActionButton fabAgregarAlumno, fabAgregarGrupo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alumnos, container, false);

        db = new DBHelper(getContext());
        prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);

        // Inicializar con el grupo del maestro por defecto
        int maestroId = prefs.getInt("user_id", -1);
        Maestro maestro = db.obtenerMaestroPorId(maestroId);
        if (maestro != null) {
            grupoIdSeleccionado = maestro.getGrupoId();
        }

        searchView = view.findViewById(R.id.search_view_alumnos);
        tvTotalAlumnos = view.findViewById(R.id.tv_total_alumnos);
        rvAlumnos = view.findViewById(R.id.rv_alumnos);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        spinnerFiltroGrupo = view.findViewById(R.id.spinner_filtro_grupo_alumnos);
        fabAgregarAlumno = view.findViewById(R.id.fab_agregar_alumno);
        fabAgregarGrupo = view.findViewById(R.id.fab_agregar_grupo);

        rvAlumnos.setLayoutManager(new LinearLayoutManager(getContext()));

        configurarSpinnerGrupos();
        configurarBusqueda();

        fabAgregarAlumno.setOnClickListener(v -> abrirDialogAlumno(-1));
        fabAgregarGrupo.setOnClickListener(v -> abrirDialogGrupo());

        return view;
    }

    private void configurarSpinnerGrupos() {

        listaGrupos = new ArrayList<>();
        List<String> nombres = new ArrayList<>();

        String url = ApiHelper.BASE_URL + "grupos";

        Log.d("API_TEST", "Iniciando GET: " + url);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("API_TEST", "Éxito GET: " + url + " | Respuesta: " + response.toString());

                    try {
                        int indexInicial = 0;

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Grupo grupo = new Grupo();
                            grupo.setId(Integer.parseInt(obj.getString("id")));
                            grupo.setNombre(obj.getString("nombre"));
                            listaGrupos.add(grupo);
                            nombres.add(grupo.getNombre());

                            if (grupo.getId() == grupoIdSeleccionado) {
                                indexInicial = i;
                            }
                        }

                        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                                getContext(),
                                android.R.layout.simple_spinner_item,
                                nombres
                        );
                        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerFiltroGrupo.setAdapter(adapterSpinner);
                        spinnerFiltroGrupo.setSelection(indexInicial);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("API_TEST", "Error GET: " + url + " | Error: " + error.toString());
                    Snackbar.make(rvAlumnos, "Error cargando grupos", Snackbar.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);

        spinnerFiltroGrupo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                grupoIdSeleccionado = listaGrupos.get(position).getId();
                cargarAlumnos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void cargarAlumnos() {

        if (listaAlumnos == null) {
            listaAlumnos = new ArrayList<>();
        }

        listaAlumnos.clear();

        String url = ApiHelper.BASE_URL + "alumnos";

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
                            int grupoId = obj.getInt("grupo_id");

                            // FILTRAR POR GRUPO
                            if (grupoId != grupoIdSeleccionado) {
                                continue;
                            }

                            Alumno alumno = new Alumno();
                            alumno.setId(obj.getInt("id"));
                            alumno.setNombre(obj.getString("nombre"));
                            alumno.setMatricula(obj.getString("matricula"));
                            alumno.setGrupoId(grupoId);

                            listaAlumnos.add(alumno);
                        }

                        tvTotalAlumnos.setText("Total: " + listaAlumnos.size() + " alumno(s)");

                        if (listaAlumnos.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            rvAlumnos.setVisibility(View.GONE);
                        } else {
                            layoutEmpty.setVisibility(View.GONE);
                            rvAlumnos.setVisibility(View.VISIBLE);
                        }

                        // =============================================
                        // CODIGO CORREGIDO - Siempre crear nuevo adapter
                        // =============================================
                        adapter = new AlumnoAdapter(
                                getContext(),
                                listaAlumnos,
                                db,
                                GestionAlumnosFragment.this
                        );
                        rvAlumnos.setAdapter(adapter);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("API_TEST", "Error GET: " + url + " | Error: " + error.toString());
                    Snackbar.make(rvAlumnos, "Error cargando alumnos", Snackbar.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private void configurarBusqueda() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) adapter.filtrar(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
                return true;
            }
        });
    }

    private void abrirDialogAlumno(int alumnoId) {
        AlumnoDialogFragment dialog = AlumnoDialogFragment.newInstance(alumnoId);
        dialog.setOnAlumnoGuardadoListener(this);
        dialog.show(getChildFragmentManager(), "dialog_alumno");
    }

    private void abrirDialogGrupo() {
        GrupoDialogFragment dialog = new GrupoDialogFragment();
        dialog.setOnGrupoGuardadoListener(this);
        dialog.show(getChildFragmentManager(), "dialog_grupo");
    }

    private void mostrarConfirmacionEliminar(Alumno alumno) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_eliminar_titulo)
                .setMessage("¿Eliminar a " + alumno.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    String url = ApiHelper.BASE_URL + "alumnos/" + alumno.getId();

                    Log.d("API_TEST", "Iniciando DELETE: " + url);

                    StringRequest request = new StringRequest(
                            Request.Method.DELETE,
                            url,
                            response -> {
                                Log.d("API_TEST", "Éxito DELETE: " + url + " | Respuesta: " + response);
                                Snackbar.make(rvAlumnos, "Alumno eliminado", Snackbar.LENGTH_SHORT).show();
                                cargarAlumnos();
                            },
                            error -> {
                                Log.e("API_TEST", "Error DELETE: " + url + " | Error: " + error.toString());
                                Snackbar.make(rvAlumnos, "Error eliminando alumno", Snackbar.LENGTH_SHORT).show();
                            }
                    );

                    RequestQueue queue = Volley.newRequestQueue(requireContext());
                    queue.add(request);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditar(Alumno alumno) {
        abrirDialogAlumno(alumno.getId());
    }

    @Override
    public void onEliminar(Alumno alumno) {
        mostrarConfirmacionEliminar(alumno);
    }

    @Override
    public void onGuardado() {
        cargarAlumnos();
        Snackbar.make(rvAlumnos, R.string.exito_alumno_guardado, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onGrupoGuardado() {
        configurarSpinnerGrupos(); // Recargar el spinner para incluir el nuevo grupo
        Snackbar.make(rvAlumnos, R.string.exito_grupo_guardado, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarAlumnos();
    }
}