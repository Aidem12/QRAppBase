package com.example.qrasist.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Tarea;
import com.example.qrasist.models.TareaAlumno;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import com.example.qrasist.ApiHelper;


public class TareaAlumnoAdapter extends RecyclerView.Adapter<TareaAlumnoAdapter.ViewHolder> {

    public interface OnTareaActionListener {
        void onAdjuntarImagen(TareaAlumno ta, int position);
    }

    private Context context;
    private List<TareaAlumno> lista;
    private DBHelper db;
    private OnTareaActionListener listener;

    public TareaAlumnoAdapter(Context context, List<TareaAlumno> lista, DBHelper db, OnTareaActionListener listener) {
        this.context = context;
        this.lista = lista;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tarea_alumno, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TareaAlumno ta = lista.get(position);


        holder.tvTitulo.setText(
                ta.getTitulo()
        );

        holder.tvDescripcion.setText(
                ta.getDescripcion()
        );

        String estado = ta.getEstado();
        String label = estado;
        int colorRes = R.color.tarea_pendiente;

        // Lógica de etiquetas solicitada para Alumno
        if (estado.equals("Entregada") || estado.equals("Pendiente Revisión")) {
            label = "Pendiente de revisión"; // "Pendiente" en la sección de Entregadas
            colorRes = R.color.tarea_pendiente;
        } else if (estado.equals("Pendiente")) {
            label = "Pendiente";
            colorRes = R.color.tarea_pendiente;
        } else if (estado.equals("Rechazada")) {
            label = "Rechazada";
            colorRes = R.color.tarea_no_entregada;
        } else if (estado.equals("Revisada")) {
            label = "Revisada";
            colorRes = R.color.tarea_entregada;
        } else if (estado.equals("No entregada")) {
            label = "No entregada";
            colorRes = R.color.tarea_no_entregada;
        }

        holder.chipEstado.setText(label);
        holder.chipEstado.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)));
        holder.chipEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white));

        if (ta.getFechaEntrega() != null && !ta.getFechaEntrega().isEmpty()) {
            holder.tvFechaEntrega.setVisibility(View.VISIBLE);
            holder.tvFechaEntrega.setText("Enviada: " + ta.getFechaEntrega());
        } else {
            holder.tvFechaEntrega.setVisibility(View.GONE);
        }

        // --- LÓGICA DE INTERACCIÓN (Panel de Envío) ---
        holder.itemView.setOnClickListener(v -> {
            if (estado.equals("Revisada") || estado.equals("No entregada")) {
                return;
            }

            int visibility = (holder.layoutAcciones.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;
            holder.layoutAcciones.setVisibility(visibility);

            if (estado.equals("Pendiente")) {
                holder.btnAdjuntar.setText("Adjuntar Imagen");
                holder.btnEnviar.setText("Enviar Tarea");
            }
            else if (

                    estado.equals("Entregada")

                            ||

                            estado.equals("Pendiente Revisión")

            ) {

                holder.btnAdjuntar.setText(
                        "Cambiar Imagen"
                );

                holder.btnEnviar.setText(
                        "Modificar Envío"
                );

            }
            else if (estado.equals("Rechazada")) {
                holder.btnAdjuntar.setText("Subir Nueva Imagen");
                holder.btnEnviar.setText("Reenviar");
            }
        });


        holder.btnAdjuntar.setOnClickListener(v -> {

            if (listener != null) {

                listener.onAdjuntarImagen(
                        ta,
                        position
                );

            }

        });


        holder.btnEnviar.setOnClickListener(v -> {

            if (
                    ta.getComentario() == null
                            ||
                            ta.getComentario().isEmpty()
            ) {

                Toast.makeText(

                        context,

                        "Adjunta una imagen antes de enviar",

                        Toast.LENGTH_SHORT

                ).show();

                return;

            }

            try {

                JSONObject json =
                        new JSONObject();

                json.put(
                        "evidencia",
                        ta.getComentario()
                );

                json.put(
                        "comentario",
                        "Enviado desde Android"
                );

                String url =

                        ApiHelper.BASE_URL +

                                "tarea-alumno/evidencia/"  +

                                ta.getId();

                Log.d("API_TEST", "Iniciando PUT: " + url + " | Datos: " + json.toString());

                JsonObjectRequest request =
                        new JsonObjectRequest(

                                Request.Method.PUT,

                                url,

                                json,

                                response -> {

                                    Log.d("API_TEST", "Éxito PUT: " + url + " | Respuesta: " + response.toString());

                                    Toast.makeText(

                                            context,

                                            "Tarea enviada",

                                            Toast.LENGTH_SHORT

                                    ).show();

                                    ta.setEstado(
                                            "Pendiente Revisión"
                                    );

                                    notifyItemChanged(
                                            position
                                    );

                                },

                                error -> {

                                    Log.e("API_TEST", "Error PUT: " + url + " | Error: " + error.toString());

                                    Toast.makeText(

                                            context,

                                            "Error enviando tarea",

                                            Toast.LENGTH_SHORT

                                    ).show();

                                }

                        );

                RequestQueue queue =
                        Volley.newRequestQueue(
                                context
                        );

                queue.add(request);

            }

            catch (Exception e) {

                e.printStackTrace();

            }

        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvFechaLimite, tvFechaEntrega;
        Chip chipEstado;
        LinearLayout layoutAcciones;
        Button btnAdjuntar, btnEnviar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo_tarea_alumno);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_tarea_alumno);
            tvFechaLimite = itemView.findViewById(R.id.tv_fecha_limite_tarea_alumno);
            tvFechaEntrega = itemView.findViewById(R.id.tv_fecha_entrega_tarea_alumno);
            chipEstado = itemView.findViewById(R.id.chip_estado_tarea_alumno);
            layoutAcciones = itemView.findViewById(R.id.layout_acciones_alumno);
            btnAdjuntar = itemView.findViewById(R.id.btn_adjuntar_imagen);
            btnEnviar = itemView.findViewById(R.id.btn_enviar_tarea);
        }
    }
}