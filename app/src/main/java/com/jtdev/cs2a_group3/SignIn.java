package com.jtdev.cs2a_group3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.AuthResult;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class SignIn extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Button signInWithGoogle, signInWithFacebook, signInWithEmail;
    private Button VerifyAccount;
    private Button loginButtonInSignIn;

    private ActivityResultLauncher<Intent> signInWithGoogleLauncher;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        signInWithGoogleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(SignIn.this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignIn.this, "Google sign in cancelled.", Toast.LENGTH_SHORT).show();
                    }
                });

        mCallbackManager = CallbackManager.Factory.create();


        signInWithGoogle = findViewById(R.id.signInWithGoogle);
        signInWithFacebook = findViewById(R.id.signInWithFacebook);
        signInWithEmail = findViewById(R.id.signInWithEmail);
        VerifyAccount = findViewById(R.id.VerifyAccount);
        loginButtonInSignIn = findViewById(R.id.loginButtonInSignIn);

        setupClickListeners();
        setupFacebookLoginCallback();
    }

    private void setupClickListeners() {
        if (signInWithGoogle != null) {
            signInWithGoogle.setOnClickListener(v -> {

                mGoogleSignInClient.signOut().addOnCompleteListener(this,
                        task -> {

                            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                            signInWithGoogleLauncher.launch(signInIntent);
                        });
            });
        }

        if (signInWithFacebook != null) {
            signInWithFacebook.setOnClickListener(v -> {

                LoginManager.getInstance().logInWithReadPermissions(SignIn.this, Arrays.asList("email", "public_profile"));
            });
        }

        if (signInWithEmail != null) {

            signInWithEmail.setOnClickListener(v -> {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            });
        }


        if (VerifyAccount != null) {
            VerifyAccount.setOnClickListener(v -> {
                Intent intent = new Intent(SignIn.this, Login.class);
                startActivity(intent);
            });
        }


        if (loginButtonInSignIn != null) {
            loginButtonInSignIn.setOnClickListener(v -> {
                Intent intent = new Intent(SignIn.this, Login.class);
                startActivity(intent);
            });
        }
    }

    private void setupFacebookLoginCallback() {
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(SignIn.this, "Facebook login successful, authenticating with Firebase...", Toast.LENGTH_SHORT).show();
                firebaseAuthWithFacebook(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(SignIn.this, "Facebook login cancelled.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(SignIn.this, "Facebook login error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Required for Facebook SDK to process login results
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                Toast.makeText(SignIn.this, "Google Sign-In successful. Welcome!", Toast.LENGTH_SHORT).show();
                                navigateToHomeScreen();
                            } else {
                                Toast.makeText(SignIn.this, "Google Sign-In successful, but email not verified. Please verify your email.", Toast.LENGTH_LONG).show();
                                user.sendEmailVerification();
                                mAuth.signOut();
                            }
                        }
                    } else {
                        Toast.makeText(SignIn.this, "Authentication failed with Google: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void firebaseAuthWithFacebook(String token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(SignIn.this, "Facebook Sign-In successful. Welcome!", Toast.LENGTH_SHORT).show();
                            navigateToHomeScreen();
                        }
                    } else {
                        Toast.makeText(SignIn.this, "Authentication failed with Facebook: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHomeScreen() {
        Intent intent = new Intent(SignIn.this, HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && currentUser.isEmailVerified()) {
                    navigateToHomeScreen();
                } else {
                    mAuth.signOut();
                    Toast.makeText(SignIn.this, "Please verify your email to log in.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}