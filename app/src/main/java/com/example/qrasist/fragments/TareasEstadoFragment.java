package com.example.qrasist.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.adapters.TareaAlumnoAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.TareaAlumno;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;



import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.example.qrasist.ApiHelper;

import org.json.JSONObject;

public class TareasEstadoFragment extends Fragment implements TareaAlumnoAdapter.OnTareaActionListener {

    private String tabTipo;
    private int alumnoId;
    private DBHelper db;
    private RecyclerView rv;
    private LinearLayout layoutEmpty;
    private TextView tvMessage;
    private TareaAlumnoAdapter adapter;
    private List<TareaAlumno> listaFiltrada = new ArrayList<>();

    private TareaAlumno taskToUpdate;
    private int taskPosition = -1;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && taskToUpdate != null) {
                    try {
                        // Crear un archivo local permanente en la carpeta de la app
                        String fileName = "evidencia_" + taskToUpdate.getId() + "_" + System.currentTimeMillis() + ".jpg";
                        File destFile = new File(requireContext().getFilesDir(), fileName);

                        // Copiar datos de la URI al archivo local
                        InputStream in = requireContext().getContentResolver().openInputStream(uri);
                        OutputStream out = new FileOutputStream(destFile);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();

                        // Guardamos la RUTA ABSOLUTA del archivo local
                        subirImagenServidor(
                                destFile,
                                taskToUpdate,
                                taskPosition
                        );

                        if (adapter != null) {
                            adapter.notifyItemChanged(taskPosition);
                        }
                        Toast.makeText(getContext(), "Imagen adjuntada correctamente", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    public static TareasEstadoFragment newInstance(String tipo, int aId) {
        TareasEstadoFragment fragment = new TareasEstadoFragment();
        Bundle args = new Bundle();
        args.putString("tipo", tipo);
        args.putInt("aId", aId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabTipo = getArguments().getString("tipo");
            alumnoId = getArguments().getInt("aId");
        }
        db = new DBHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tareas_estado, container, false);
        rv = v.findViewById(R.id.rv_tareas_estado);
        layoutEmpty = v.findViewById(R.id.layout_empty_tareas_alumno);
        tvMessage = v.findViewById(R.id.tv_empty_message);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        cargarTareas();
        return v;
    }

    private void cargarTareas() {

        listaFiltrada.clear();

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

                                for (
                                        int i = 0;
                                        i < response.length();
                                        i++
                                ) {

                                    JSONObject obj =
                                            response.getJSONObject(i);

                                    int alumno =
                                            Integer.parseInt(
                                                    obj.getString("alumno_id")
                                            );

                                    // =====================================
                                    // SOLO MIS TAREAS
                                    // =====================================

                                    if (alumno != alumnoId)
                                        continue;

                                    String estado =
                                            obj.getString(
                                                    "estado"
                                            );

                                    boolean agregar =
                                            false;

                                    // =====================================
                                    // FILTROS
                                    // =====================================

                                    if (
                                            tabTipo.equals(
                                                    "Pendiente"
                                            )
                                    ) {

                                        agregar =
                                                estado.equals(
                                                        "Pendiente"
                                                );

                                    }

                                    else if (
                                            tabTipo.equals(
                                                    "Entregada"
                                            )
                                    ) {

                                        agregar =

                                                estado.equals(
                                                        "Pendiente Revisión"
                                                )

                                                        ||

                                                        estado.equals(
                                                                "Aceptada"
                                                        )

                                                        ||

                                                        estado.equals(
                                                                "Rechazada"
                                                        );

                                    }

                                    if (agregar) {

                                        TareaAlumno ta =
                                                new TareaAlumno();

                                        ta.setId(
                                                Integer.parseInt(
                                                        obj.getString("id")
                                                )
                                        );

                                        ta.setEstado(
                                                estado
                                        );

                                        ta.setComentario(
                                                obj.optString(
                                                        "comentario"
                                                )
                                        );

                                        ta.setTitulo(

                                                obj.optString(
                                                        "titulo"
                                                )

                                        );

                                        ta.setDescripcion(

                                                obj.optString(
                                                        "descripcion"
                                                )

                                        );


                                        listaFiltrada.add(
                                                ta
                                        );

                                    }

                                }

                                // =====================================
                                // UI
                                // =====================================

                                if (
                                        listaFiltrada.isEmpty()
                                ) {

                                    layoutEmpty.setVisibility(
                                            View.VISIBLE
                                    );

                                    rv.setVisibility(
                                            View.GONE
                                    );

                                }

                                else {

                                    layoutEmpty.setVisibility(
                                            View.GONE
                                    );

                                    rv.setVisibility(
                                            View.VISIBLE
                                    );

                                    adapter =
                                            new TareaAlumnoAdapter(

                                                    getContext(),

                                                    listaFiltrada,

                                                    db,

                                                    this

                                            );

                                    rv.setAdapter(adapter);

                                }

                            }

                            catch (Exception e) {

                                e.printStackTrace();

                            }

                        },

                        error -> {

                            Log.e("API_TEST", "Error GET: " + url + " | Error: " + error.toString());

                            Toast.makeText(

                                    getContext(),

                                    "Error cargando tareas",

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

    @Override
    public void onAdjuntarImagen(TareaAlumno ta, int position) {
        this.taskToUpdate = ta;
        this.taskPosition = position;
        pickImageLauncher.launch("image/*");
    }

    private void subirImagenServidor(

            File imageFile,

            TareaAlumno tarea,

            int position

    ) {

        new Thread(() -> {

            try {

                String boundary =
                        "----QRASIST" +
                                System.currentTimeMillis();

                String urlString = ApiHelper.BASE_URL + "tarea-alumno/upload";
                java.net.URL url = new java.net.URL(urlString);

                Log.d("API_TEST", "Iniciando POST (Multipart): " + urlString + " | Archivo: " + imageFile.getName());

                java.net.HttpURLConnection connection =
                        (java.net.HttpURLConnection)
                                url.openConnection();

                connection.setUseCaches(false);

                connection.setDoOutput(true);

                connection.setDoInput(true);

                connection.setRequestMethod("POST");

                connection.setRequestProperty(

                        "Content-Type",

                        "multipart/form-data; boundary=" +
                                boundary

                );

                java.io.DataOutputStream request =
                        new java.io.DataOutputStream(
                                connection.getOutputStream()
                        );

                // =====================================
                // FILE
                // =====================================

                request.writeBytes("--" + boundary + "\r\n");

                request.writeBytes(

                        "Content-Disposition: form-data; " +

                                "name=\"imagen\"; " +

                                "filename=\"" +

                                imageFile.getName() +

                                "\"\r\n"

                );

                request.writeBytes(
                        "Content-Type: image/jpeg\r\n\r\n"
                );

                java.io.FileInputStream inputStream =
                        new java.io.FileInputStream(
                                imageFile
                        );

                byte[] buffer =
                        new byte[4096];

                int bytesRead;

                while (
                        (bytesRead =
                                inputStream.read(buffer))
                                != -1
                ) {

                    request.write(
                            buffer,
                            0,
                            bytesRead
                    );

                }

                inputStream.close();

                request.writeBytes("\r\n");

                request.writeBytes(
                        "--" + boundary + "--\r\n"
                );

                request.flush();

                request.close();

                int responseCode =
                        connection.getResponseCode();

                if (responseCode == 200) {

                    java.io.BufferedReader reader =
                            new java.io.BufferedReader(

                                    new java.io.InputStreamReader(

                                            connection.getInputStream()

                                    )

                            );

                    StringBuilder result =
                            new StringBuilder();

                    String line;

                    while (
                            (line = reader.readLine())
                                    != null
                    ) {

                        result.append(line);

                    }

                    reader.close();

                    Log.d("API_TEST", "Éxito POST (Multipart): " + urlString + " | Respuesta: " + result.toString());

                    JSONObject json =
                            new JSONObject(
                                    result.toString()
                            );

                    String imageUrl =
                            json.getString("url");

                    tarea.setComentario(
                            imageUrl
                    );

                    requireActivity().runOnUiThread(() -> {

                        if (adapter != null) {

                            adapter.notifyItemChanged(
                                    position
                            );

                        }

                        Toast.makeText(

                                getContext(),

                                "Imagen subida",

                                Toast.LENGTH_SHORT

                        ).show();

                    });

                } else {
                    Log.e("API_TEST", "Error POST (Multipart): " + urlString + " | Código: " + responseCode);
                }

            }

            catch (Exception e) {

                Log.e("API_TEST", "Excepción POST (Multipart): " + e.getMessage());

                e.printStackTrace();

                requireActivity().runOnUiThread(() ->

                        Toast.makeText(

                                getContext(),

                                "Error subiendo imagen",

                                Toast.LENGTH_SHORT

                        ).show()

                );

            }

        }).start();

    }

}