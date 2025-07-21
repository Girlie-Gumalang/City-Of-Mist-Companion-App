package com.jtdev.cs2a_group3;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;


import com.jtdev.cs2a_group3.fragments.AccountPage;
import com.jtdev.cs2a_group3.fragments.CharactersPage;
import com.jtdev.cs2a_group3.fragments.RulesPage;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.nav_view);

        String fragmentToLoad = getIntent().getStringExtra("FRAGMENT_TO_LOAD");
        if (fragmentToLoad != null) {
            switch (fragmentToLoad) {
                case "CHARACTERS":
                    loadFragment(new CharactersPage());
                    bottomNavigationView.setSelectedItemId(R.id.nav_characters);
                    break;
                case "RULES":
                    loadFragment(new RulesPage());
                    bottomNavigationView.setSelectedItemId(R.id.nav_rules);
                    break;
                case "ACCOUNT":
                    loadFragment(new AccountPage());
                    bottomNavigationView.setSelectedItemId(R.id.nav_account);
                    break;
                default:

                    loadFragment(new RulesPage());
                    bottomNavigationView.setSelectedItemId(R.id.nav_rules);
                    break;
            }
        } else {
            loadFragment(new RulesPage());
            bottomNavigationView.setSelectedItemId(R.id.nav_rules);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_characters) {
                loadFragment(new CharactersPage());
                return true;
            } else if (itemId == R.id.nav_rules) {
                loadFragment(new RulesPage());
                return true;
            } else if (itemId == R.id.nav_account) {
                loadFragment(new AccountPage());
                return true;
            }
            return false;
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}