package com.example.vieneviene;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        BottomNavigationView bottomNav2 = findViewById(R.id.bottomNavigation2);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container2, new ReservaFragment())
                .commit();
        bottomNav2.setOnNavigationItemSelectedListener(item -> {
            Fragment selected2 = null;
            int itemId2 = item.getItemId();

            if (itemId2 == R.id.nav_Reservas) {
                selected2 = new ReservaFragment();
            } else if (itemId2 == R.id.nav_Estacionamiento) {
                selected2 = new EstacionamientoFragment();
            } else if (itemId2 == R.id.nav_perfil) {
                selected2 = new PerfilFragment();
            }

            if (selected2 != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container2, selected2)
                        .commit();
            }

            return true;
        });

    }
}