package com.example.vieneviene;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vieneviene.modelos.ReservaHistorial;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<ReservaHistorial> reservas;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ReservaHistorial reserva);
    }

    public HistorialAdapter(List<ReservaHistorial> reservas, OnItemClickListener listener) {
        this.reservas = reservas;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReservaHistorial r = reservas.get(position);

        holder.tvNombre.setText(r.estacionamiento);
        holder.tvFecha.setText(r.hora_inicio.substring(0,10)); // yyyy-mm-dd

        Glide.with(holder.itemView.getContext())
                .load(r.foto_principal)
                .placeholder(R.drawable.ic_google_background)
                .error(R.drawable.ic_google_background)
                .into(holder.img);

        switch (r.estado.toLowerCase()) {
            case "confirmada":
                holder.tvEstadoReserva.setTextColor(
                        holder.itemView.getResources().getColor(R.color.estado_confirmada)
                );
                holder.tvEstadoReserva.setText(r.estado);
                break;

            case "completada":
                holder.tvEstadoReserva.setTextColor(
                        holder.itemView.getResources().getColor(R.color.estado_completada)
                );
                holder.tvEstadoReserva.setText(r.estado);
                break;

            case "pendiente":
                holder.tvEstadoReserva.setTextColor(
                        holder.itemView.getResources().getColor(R.color.estado_pendiente)
                );
                holder.tvEstadoReserva.setText(r.estado);
                break;
            default:
                holder.tvEstadoReserva.setTextColor(
                        holder.itemView.getResources().getColor(android.R.color.darker_gray)
                );
                holder.tvEstadoReserva.setText(r.estado);
        }
            holder.card.setOnClickListener(v -> listener.onItemClick(r));
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre, tvFecha,tvEstadoReserva;
        MaterialCardView card;
        ImageView img;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreEstacionamiento);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvEstadoReserva = itemView.findViewById(R.id.estadoReserva);
            card = itemView.findViewById(R.id.cardHistorial);
            img = itemView.findViewById(R.id.imgHistorial);
        }
    }
}
