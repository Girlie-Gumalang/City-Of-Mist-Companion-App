package com.jtdev.cs2a_group3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText inputEmailSignUp, inputPasswordSignUp, inputRepeatPasswordSignUp;
    private ImageButton signUpButton;
    private ProgressBar progressBar;
    private ImageView returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        inputEmailSignUp = findViewById(R.id.inputEmailSignUp);
        inputPasswordSignUp = findViewById(R.id.inputPasswordSignUp);
        inputRepeatPasswordSignUp = findViewById(R.id.inputRepeatPasswordSignUp);
        signUpButton = findViewById(R.id.signUpButton);
        progressBar = findViewById(R.id.progressBar);
        returnButton = findViewById(R.id.return_button);

        if (signUpButton != null) {
            signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = inputEmailSignUp.getText().toString().trim();
                    String password = inputPasswordSignUp.getText().toString().trim();
                    String repeatPassword = inputRepeatPasswordSignUp.getText().toString().trim();

                    if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                        Toast.makeText(SignUp.this, "All fields are required.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!password.equals(repeatPassword)) {
                        Toast.makeText(SignUp.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    if (password.length() < 6) {
                        Toast.makeText(SignUp.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!password.matches(".*[A-Z].*")) {
                        Toast.makeText(SignUp.this, "Password must contain at least one uppercase letter.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!password.matches(".*[a-z].*")) {
                        Toast.makeText(SignUp.this, "Password must contain at least one lowercase letter.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!password.matches(".*\\d.*")) {
                        Toast.makeText(SignUp.this, "Password must contain at least one digit.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (progressBar != null) {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(emailTask -> {
                                                        if (emailTask.isSuccessful()) {
                                                            Toast.makeText(SignUp.this, "Registration successful. Please verify your email.", Toast.LENGTH_LONG).show();
                                                            Intent intent = new Intent(SignUp.this, SignIn.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(SignUp.this, "Failed to send verification email. Please try again later.", Toast.LENGTH_LONG).show();
                                                            mAuth.signOut();
                                                        }
                                                    });
                                        }
                                    } else {
                                        String errorMessage = "Registration failed.";
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthUserCollisionException e) {
                                            errorMessage = "This email address is already registered.";
                                        } catch (Exception e) {
                                            errorMessage += " " + e.getMessage();
                                        }
                                        Toast.makeText(SignUp.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
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