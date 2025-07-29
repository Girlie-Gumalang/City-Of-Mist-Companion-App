package com.jtdev.cs2a_group3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.widget.TextView;
import com.jtdev.cs2a_group3.MainActivity;
import com.jtdev.cs2a_group3.R;

public class RuleDetailActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView ruleDetailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_detail);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ruleDetailText = findViewById(R.id.ruleDetailText);

        String ruleName = getIntent().getStringExtra("RULE_NAME");
        ruleDetailText.setText(ruleName);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_rules_screen) {
                Intent intent = new Intent(RuleDetailActivity.this, MainActivity.class);
                intent.putExtra("FRAGMENT_NAME", "rules");
                startActivity(intent);
                finish();
            }
            return true;
        });
    }
}
