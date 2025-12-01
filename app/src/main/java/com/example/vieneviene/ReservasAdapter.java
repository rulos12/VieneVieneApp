package com.example.vieneviene;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservasAdapter extends RecyclerView.Adapter<ReservasAdapter.ViewHolder> {

    private List<Reserva> lista;
    private Context context;
    private OnReservaClickListener listener;

    public interface OnReservaClickListener {
        void onReservaClick(Reserva reserva);
    }
    public ReservasAdapter(List<Reserva> lista, Context context, OnReservaClickListener listener) {
        this.lista = lista;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservacion, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reserva item = lista.get(position);


        holder.txtCliente.setText(item.getNombre_cliente());
        holder.txtVehiculo.setText(item.getModelo_vehiculo() + " - " + item.getPlacas());
        holder.txtEstado.setText(item.getEstado());
        switch (item.getEstado()) {
            case "pendiente":
                holder.txtEstado.setBackgroundResource(R.drawable.bg_estado_pendiente);
                break;

            case "confirmada":
                holder.txtEstado.setBackgroundResource(R.drawable.bg_estado_confirmada);
                break;

            case "completada":
                holder.txtEstado.setBackgroundResource(R.drawable.bg_estado_completada);
                break;

            default:
                holder.txtEstado.setBackgroundResource(0);
                break;
        }

        String fecha = item.getFecha_reserva();
        String inicio = item.getHora_inicio();
        String fin = item.getHora_fin();

        String fechaFormateada = "";
        try {
            LocalDate f = LocalDate.parse(fecha);
            fechaFormateada = f.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            fechaFormateada = fecha;
        }

        String inicioFmt = "";
        String finFmt = "";

        try {
            LocalTime hi = LocalDateTime.parse(inicio).toLocalTime();
            LocalTime hf = LocalDateTime.parse(fin).toLocalTime();
            DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("h:mm a");
            inicioFmt = hi.format(formatoHora);
            finFmt = hf.format(formatoHora);
        } catch (Exception e) {
            inicioFmt = inicio;
            finFmt = fin;
        }
        holder.txtFechaHora.setText(fechaFormateada + " | " + inicioFmt + " - " + finFmt);

        holder.itemView.setOnClickListener(v -> listener.onReservaClick(item));

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgEstacionamiento;
        TextView txtCliente, txtVehiculo, txtEstado, txtFechaHora;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //imgEstacionamiento = itemView.findViewById(R.id.imgFotoEstacionamiento);
            txtCliente = itemView.findViewById(R.id.txtCliente);
            txtVehiculo = itemView.findViewById(R.id.txtVehiculo);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            txtFechaHora = itemView.findViewById(R.id.txtFechaHora);
        }

    }
}
