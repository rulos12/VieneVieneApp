package com.example.vieneviene;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vieneviene.modelos.EstacionamientoApi;

import java.util.ArrayList;
import java.util.List;

public class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.ViewHolder> {

    Context context;
    ArrayList<EstacionamientoApi> lista;

    public FavoritosAdapter(Context context, ArrayList<EstacionamientoApi> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        EstacionamientoApi e = lista.get(position);



        holder.txtNombre.setText(e.nombre);
        holder.txtPrecio.setText("$" + (int)e.precio_hora + " MXN");

        holder.iconFavoritoCard.setImageResource(R.drawable.ic_favoritos_activo);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EstacionamientoDetalleActivity.class);
            intent.putExtra("id_estacionamiento", e.id_estacionamiento);
            context.startActivity(intent);
        });

        Glide.with(holder.itemView.getContext())
                .load(e.foto_principal)
                .placeholder(R.drawable.ic_google_background)
                .error(R.drawable.ic_google_background)
                .centerCrop()
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombre, txtPrecio;
        ImageView iconFavoritoCard,img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgEstacionamientoFav);
            txtNombre = itemView.findViewById(R.id.txtNombreFav);
            txtPrecio = itemView.findViewById(R.id.txtPrecioFav);
            iconFavoritoCard = itemView.findViewById(R.id.iconFavorito);
        }
    }
}
