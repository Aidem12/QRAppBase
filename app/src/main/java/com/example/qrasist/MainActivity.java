package com.example.qrasist;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Maestro;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private DBHelper db;
    private RadioGroup rgRol;
    private TextInputEditText etUsuario, etPassword;
    private TextInputLayout tilUsuario, tilPassword;
    private Button btnLogin;
    private String rolSeleccionado = "MAESTRO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);

        // Referenciar vistas
        rgRol = findViewById(R.id.rg_rol);
        etUsuario = findViewById(R.id.et_usuario);
        etPassword = findViewById(R.id.et_password);
        tilUsuario = findViewById(R.id.til_usuario);
        tilPassword = findViewById(R.id.til_password);
        btnLogin = findViewById(R.id.btn_login);

        setupRolSelector();
        setupLoginButton();
    }

    private void setupRolSelector() {
        rgRol.setOnCheckedChangeListener((group, checkedId) -> {
            int color;
            if (checkedId == R.id.rb_maestro) {
                rolSeleccionado = "MAESTRO";
                color = ContextCompat.getColor(this, R.color.maestro_primary);
            } else {
                rolSeleccionado = "ALUMNO";
                color = ContextCompat.getColor(this, R.color.alumno_primary);
            }

            // Cambiar colores de los inputs y botón
            ColorStateList colorStateList = ColorStateList.valueOf(color);
            tilUsuario.setBoxStrokeColor(color);
            tilUsuario.setHintTextColor(colorStateList);
            tilPassword.setBoxStrokeColor(color);
            tilPassword.setHintTextColor(colorStateList);
            btnLogin.setBackgroundColor(color);
        });
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (usuario.isEmpty() || password.isEmpty()) {
                Snackbar.make(v, R.string.error_campos_vacios, Snackbar.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("QRAsistPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            if (rolSeleccionado.equals("MAESTRO")) {

                try {

                    String url =
                            ApiHelper.BASE_URL +
                                    "maestros/login";

                    org.json.JSONObject json =
                            new org.json.JSONObject();

                    json.put(
                            "usuario",
                            usuario
                    );

                    json.put(
                            "password",
                            password
                    );

                    Log.d("API_TEST", "Iniciando POST: " + url + " | Datos: " + json.toString());

                    JsonObjectRequest request =
                            new JsonObjectRequest(

                                    Request.Method.POST,

                                    url,

                                    json,

                                    response -> {

                                        Log.d("API_TEST", "Éxito POST: " + url + " | Respuesta: " + response.toString());

                                        try {

                                            int maestroId =
                                                    response.getInt(
                                                            "id"
                                                    );

                                            String nombre =
                                                    response.getString(
                                                            "nombre"
                                                    );

                                            editor.putInt(
                                                    "user_id",
                                                    maestroId
                                            );

                                            editor.putString(
                                                    "user_nombre",
                                                    nombre
                                            );

                                            editor.putString(
                                                    "user_rol",
                                                    "MAESTRO"
                                            );

                                            editor.apply();

                                            startActivity(

                                                    new Intent(
                                                            this,
                                                            DashboardMaestroActivity.class
                                                    )

                                            );

                                            finish();

                                        }

                                        catch (Exception e) {

                                            e.printStackTrace();

                                        }

                                    },

                                    error -> {

                                        Log.e("API_TEST", "Error POST: " + url + " | Error: " + error.toString());

                                        Snackbar.make(

                                                v,

                                                "Credenciales incorrectas",

                                                Snackbar.LENGTH_SHORT

                                        ).show();

                                    }

                            );

                    RequestQueue queue =
                            Volley.newRequestQueue(this);

                    queue.add(request);

                }

                catch (Exception e) {

                    e.printStackTrace();

                }

            } else {
                String url =
                        ApiHelper.BASE_URL +
                                "alumnos/matricula/" +
                                usuario;

                Log.d("API_TEST", "Iniciando GET: " + url);

                JsonObjectRequest request =
                        new JsonObjectRequest(

                                Request.Method.GET,

                                url,

                                null,

                                response -> {

                                    Log.d("API_TEST", "Éxito GET: " + url + " | Respuesta: " + response.toString());

                                    try {

                                        String matricula =
                                                response.getString(
                                                        "matricula"
                                                );

                                        String nombre =
                                                response.getString(
                                                        "nombre"
                                                );

                                        int alumnoId =
                                                response.getInt(
                                                        "id"
                                                );

                                        // =====================================
                                        // PASSWORD = MATRICULA
                                        // =====================================

                                        if (
                                                password.equals(
                                                        matricula
                                                )
                                        ) {

                                            editor.putInt(
                                                    "user_id",
                                                    alumnoId
                                            );

                                            editor.putString(
                                                    "user_nombre",
                                                    nombre
                                            );

                                            editor.putString(
                                                    "user_matricula",
                                                    matricula
                                            );

                                            editor.putString(
                                                    "user_rol",
                                                    "ALUMNO"
                                            );

                                            editor.apply();

                                            startActivity(

                                                    new Intent(
                                                            this,
                                                            DashboardAlumnoActivity.class
                                                    )

                                            );

                                            finish();

                                        }

                                        else {

                                            Snackbar.make(

                                                    v,

                                                    "Contraseña incorrecta",

                                                    Snackbar.LENGTH_SHORT

                                            ).show();

                                        }

                                    }

                                    catch (Exception e) {

                                        e.printStackTrace();

                                    }

                                },

                                error -> {

                                    Log.e("API_TEST", "Error GET: " + url + " | Error: " + error.toString());

                                    Snackbar.make(

                                            v,

                                            "Alumno no encontrado",

                                            Snackbar.LENGTH_SHORT

                                    ).show();

                                }

                        );

                RequestQueue queue =
                        Volley.newRequestQueue(this);

                queue.add(request);
            }
        });
    }
}