package com.example.qrasist.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Asistencia;
import com.google.android.material.chip.Chip;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.example.qrasist.ApiHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MiQrFragment extends Fragment {

    private DBHelper db;
    private SharedPreferences prefs;
    private int alumnoId;
    private TextView tvNombreAlumno, tvMatriculaAlumno;
    private ImageView ivCodigoQr;
    private Button btnCompartirQr;
    private RecyclerView rvHistorialAlumno;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mi_qr, container, false);

        db = new DBHelper(getContext());
        prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);
        alumnoId = prefs.getInt("user_id", -1);

        tvNombreAlumno = view.findViewById(R.id.tv_nombre_alumno_qr);
        tvMatriculaAlumno = view.findViewById(R.id.tv_matricula_alumno_qr);
        ivCodigoQr = view.findViewById(R.id.iv_codigo_qr);
        btnCompartirQr = view.findViewById(R.id.btn_compartir_qr);
        rvHistorialAlumno = view.findViewById(R.id.rv_historial_alumno);

        rvHistorialAlumno.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarDatosAlumno();

        btnCompartirQr.setOnClickListener(v -> compartirQr());

        return view;
    }

    private void cargarDatosAlumno() {

        String matricula =
                prefs.getString(
                        "user_matricula",
                        ""
                );

        String url =
                ApiHelper.BASE_URL +
                        "alumnos/matricula/" +
                        matricula;

        Log.d("API_TEST", "Iniciando GET: " + url);

        JsonObjectRequest request =
                new JsonObjectRequest(

                        Request.Method.GET,

                        url,

                        null,

                        response -> {

                            Log.d("API_TEST", "Éxito GET: " + url + " | Respuesta: " + response.toString());

                            try {

                                String nombre =
                                        response.getString(
                                                "nombre"
                                        );

                                String matriculaAlumno =
                                        response.getString(
                                                "matricula"
                                        );

                                // =====================================
                                // MOSTRAR DATOS
                                // =====================================

                                tvNombreAlumno.setText(
                                        nombre
                                );

                                tvMatriculaAlumno.setText(
                                        "Matrícula: " +
                                                matriculaAlumno
                                );

                                // =====================================
                                // GENERAR QR
                                // =====================================

                                try {

                                    MultiFormatWriter writer =
                                            new MultiFormatWriter();

                                    BitMatrix bitMatrix =
                                            writer.encode(

                                                    matriculaAlumno,

                                                    BarcodeFormat.QR_CODE,

                                                    500,

                                                    500

                                            );

                                    BarcodeEncoder encoder =
                                            new BarcodeEncoder();

                                    Bitmap bitmap =
                                            encoder.createBitmap(
                                                    bitMatrix
                                            );

                                    ivCodigoQr.setImageBitmap(
                                            bitmap
                                    );

                                }

                                catch (Exception e) {

                                    e.printStackTrace();

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

                                    "Error cargando alumno",

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

    private void compartirQr() {
        if (ivCodigoQr.getDrawable() == null) return;
        Bitmap bitmap = ((BitmapDrawable) ivCodigoQr.getDrawable()).getBitmap();
        try {
            File cachePath = new File(requireContext().getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "alumno_qr.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, requireContext().getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Compartir QR"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al compartir QR", Toast.LENGTH_SHORT).show();
        }
    }
}