package com.example.qrasist.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.example.qrasist.ApiHelper;
import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class GrupoDialogFragment extends DialogFragment {

    public interface OnGrupoGuardadoListener {
        void onGrupoGuardado();
    }

    private OnGrupoGuardadoListener listener;
    private DBHelper db;
    private TextInputEditText etNombre, etDescripcion;
    private Button btnCancelar, btnGuardar;

    public void setOnGrupoGuardadoListener(OnGrupoGuardadoListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_grupo, container, false);

        etNombre = view.findViewById(R.id.et_nombre_grupo_dialog);
        etDescripcion = view.findViewById(R.id.et_descripcion_grupo_dialog);
        btnCancelar = view.findViewById(R.id.btn_cancelar_grupo_dialog);
        btnGuardar = view.findViewById(R.id.btn_guardar_grupo_dialog);

        btnCancelar.setOnClickListener(v -> dismiss());
        btnGuardar.setOnClickListener(v -> guardarGrupo());

        return view;
    }

    private void guardarGrupo() {

        String nombre =

                etNombre
                        .getText()
                        .toString()
                        .trim();

        String descripcion =

                etDescripcion
                        .getText()
                        .toString()
                        .trim();

        if (
                nombre.isEmpty()
        ) {

            Toast.makeText(

                    getContext(),

                    "El nombre del grupo es obligatorio",

                    Toast.LENGTH_SHORT

            ).show();

            return;

        }

        String url =

                ApiHelper.BASE_URL +

                        "grupos";

        JSONObject body =
                new JSONObject();

        try {

            body.put(
                    "nombre",
                    nombre
            );

            body.put(
                    "descripcion",
                    descripcion
            );

        }

        catch (JSONException e) {

            e.printStackTrace();

        }

        Log.d("API_TEST", "Iniciando POST: " + url + " | Datos: " + body.toString());

        JsonObjectRequest request =

                new JsonObjectRequest(

                        Request.Method.POST,

                        url,

                        body,

                        response -> {

                            Log.d("API_TEST", "Éxito POST: " + url + " | Respuesta: " + response.toString());

                            Toast.makeText(

                                    getContext(),

                                    "Grupo guardado",

                                    Toast.LENGTH_SHORT

                            ).show();

                            if (
                                    listener != null
                            ) {

                                listener.onGrupoGuardado();

                            }

                            dismiss();

                        },

                        error -> {

                            Log.e("API_TEST", "Error POST: " + url + " | Error: " + error.toString());

                            Toast.makeText(

                                    getContext(),

                                    "Error al guardar grupo",

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
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}