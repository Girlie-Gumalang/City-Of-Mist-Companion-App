package com.jtdev.cs2a_group3.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jtdev.cs2a_group3.R;
import com.jtdev.cs2a_group3.dialogs.ChangePasswordDialog;
import com.jtdev.cs2a_group3.dialogs.LogoutConfirmationDialog;
import com.jtdev.cs2a_group3.SignIn;

public class AccountInfoActivity extends AppCompatActivity implements LogoutConfirmationDialog.LogoutListener {
    ImageView rb_information, editPasswordButton;
    TextView emailAccountInfoContainer, passwordAccountInfoContainer;
    Button logoutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        rb_information = findViewById(R.id.return_button_information);
        emailAccountInfoContainer = findViewById(R.id.email_account_info_container);
        passwordAccountInfoContainer = findViewById(R.id.password_account_info_container);
        editPasswordButton = findViewById(R.id.edit_text_acc_info);
        logoutButton = findViewById(R.id.logout_button);

        rb_information.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        editPasswordButton.setOnClickListener(v -> {
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
            changePasswordDialog.show(getSupportFragmentManager(), "ChangePasswordDialog");
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

        updateAccountInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccountInfo();
    }

    private void updateAccountInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null && !email.isEmpty()) {
                emailAccountInfoContainer.setText(email);
            } else {
                emailAccountInfoContainer.setText("Email not available");
            }
            passwordAccountInfoContainer.setText("**************");
        } else {
            emailAccountInfoContainer.setText("Not logged in");
            passwordAccountInfoContainer.setText("**************");
            Toast.makeText(this, "Please log in to view account info.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SignIn.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void showLogoutConfirmationDialog() {
        LogoutConfirmationDialog dialog = new LogoutConfirmationDialog();
        dialog.setLogoutListener(this); // I-set ang activity na ito bilang listener
        dialog.show(getSupportFragmentManager(), "LogoutConfirmationDialog");
    }

    @Override
    public void onLogoutConfirmed() {
        mAuth.signOut();
        Toast.makeText(AccountInfoActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AccountInfoActivity.this, SignIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onLogoutCancelled() {
        Toast.makeText(AccountInfoActivity.this, "Logout cancelled.", Toast.LENGTH_SHORT).show();
    }
}