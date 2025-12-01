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

import java.util.ArrayList;
import java.util.List;

public class PropietarioEstacionamientoAdapter extends RecyclerView.Adapter<PropietarioEstacionamientoAdapter.ViewHolder> {

    List<EstacionamientoPropietario> lista = new ArrayList<>();
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(EstacionamientoPropietario estacionamiento);
    }

    public PropietarioEstacionamientoAdapter(List<EstacionamientoPropietario> lista,
                                             Context context,
                                             OnItemClickListener listener) {
        this.lista = lista;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_estacionamiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        EstacionamientoPropietario item = lista.get(position);

        holder.txtNombre.setText(item.getNombre());
        holder.txtDireccion.setText(item.getDireccion());
        holder.txtEstado.setText(item.getEstado());

        switch (item.getEstado().toLowerCase()) {
            case "activo":
                holder.txtEstado.setBackgroundResource(R.drawable.bg_estado_confirmada);
                break;

            case "inactivo":
                holder.txtEstado.setBackgroundResource(R.drawable.bg_estado_pendiente);
                break;

            default:
                holder.txtEstado.setBackgroundResource(R.drawable.bg_estado_pendiente);
                break;
        }

        Glide.with(context)
                .load(item.getFoto_principal())
                .into(holder.imgFoto);

        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgFoto;
        TextView txtNombre, txtDireccion, txtEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgFoto = itemView.findViewById(R.id.imgFotoEstacionamiento);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            txtNombre = itemView.findViewById(R.id.txtNombreEstacionamiento);
            txtDireccion = itemView.findViewById(R.id.txtDireccionEstacionamiento);
        }
    }
}
