package com.jtdev.cs2a_group3;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText inputEmailLogin, inputPasswordLogin;
    private CheckBox rememberMe;
    private ImageButton loginButtonInLogin;
    private TextView forgotPasswordLink;
    private ImageView returnButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        inputEmailLogin = findViewById(R.id.inputEmailLogin);
        inputPasswordLogin = findViewById(R.id.inputPasswordLogin);
        rememberMe = findViewById(R.id.rememberMe);
        loginButtonInLogin = findViewById(R.id.loginButtonInLogin);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        returnButton = findViewById(R.id.return_button);
        progressBar = findViewById(R.id.progressBar);

        if (loginButtonInLogin != null) {
            loginButtonInLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = inputEmailLogin.getText().toString().trim();
                    String password = inputPasswordLogin.getText().toString().trim();

                    if (TextUtils.isEmpty(email)) {
                        inputEmailLogin.setError("Email is required!");
                        inputEmailLogin.requestFocus();
                        return;
                    }

                    if (TextUtils.isEmpty(password)) {
                        inputPasswordLogin.setError("Password is required!");
                        inputPasswordLogin.requestFocus();
                        return;
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    loginButtonInLogin.setEnabled(false);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    loginButtonInLogin.setEnabled(true);

                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            if (user.isEmailVerified()) {
                                                Toast.makeText(Login.this, "Login successful. Welcome!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(Login.this, HomeScreen.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(Login.this, "Please verify your email first. A verification email has been sent to " + email, Toast.LENGTH_LONG).show();
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(verificationTask -> {
                                                            if (!verificationTask.isSuccessful()) {
                                                                Toast.makeText(Login.this, "Failed to resend verification email.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                mAuth.signOut();
                                            }
                                        }
                                    } else {
                                        String errorMessage = "Authentication failed.";
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthInvalidUserException e) {
                                            errorMessage = "Account does not exist or has been disabled. Please register or contact support.";
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            errorMessage = "Invalid email or password.";
                                        } catch (Exception e) {
                                            errorMessage += " " + e.getMessage();
                                        }
                                        Toast.makeText(Login.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            });
        }

        if (forgotPasswordLink != null) {
            forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = inputEmailLogin.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        inputEmailLogin.setError("Please enter your email to reset password.");
                        inputEmailLogin.requestFocus();
                        return;
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(Login.this, "Failed to send password reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });
        }

        if (returnButton != null) {
            returnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }
}