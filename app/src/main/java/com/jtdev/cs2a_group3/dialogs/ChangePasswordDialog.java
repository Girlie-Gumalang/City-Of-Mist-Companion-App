package com.jtdev.cs2a_group3.dialogs;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Add Log import
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Changed ImageButton to Button if your change_password_png is used as background for a Button
import android.widget.EditText;
import android.widget.ImageButton; // Keep ImageButton if that's what change_password_png is applied to
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jtdev.cs2a_group3.R;

public class ChangePasswordDialog extends DialogFragment {

    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText repeatNewPasswordEditText;
    private ImageButton changePasswordButton;
    private TextView forgotPasswordTextView;

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_layout_changepassword, container, false);

        mAuth = FirebaseAuth.getInstance();

        currentPasswordEditText = view.findViewById(R.id.current_password_edittext);
        newPasswordEditText = view.findViewById(R.id.new_password_edittext);
        repeatNewPasswordEditText = view.findViewById(R.id.repeat_password_edittext);
        changePasswordButton = view.findViewById(R.id.change_password_btn);
        forgotPasswordTextView = view.findViewById(R.id.forgot_password_textview);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        changePasswordButton.setOnClickListener(v -> changePassword());

        forgotPasswordTextView.setOnClickListener(v -> sendPasswordResetEmail());

        return view;
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String repeatPassword = repeatNewPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordEditText.setError("Current password is required.");
            currentPasswordEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("New password is required.");
            newPasswordEditText.requestFocus();
            return;
        }
        if (newPassword.length() < 6) {
            newPasswordEditText.setError("Password must be at least 6 characters.");
            newPasswordEditText.requestFocus();
            return;
        }
        if (!newPassword.equals(repeatPassword)) {
            repeatNewPasswordEditText.setError("Passwords do not match.");
            repeatNewPasswordEditText.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                    .addOnCompleteListener(reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            Log.d("ChangePasswordDialog", "User re-authenticated.");
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                            dismiss();
                                        } else {
                                            String errorMessage = "Failed to update password.";
                                            if (updateTask.getException() != null) {
                                                errorMessage += " " + updateTask.getException().getMessage();
                                            }
                                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                                            Log.e("ChangePasswordDialog", "Password update failed: " + errorMessage, updateTask.getException());
                                        }
                                    });
                        } else {
                            String errorMessage = "Authentication failed. Check your current password.";
                            if (reauthTask.getException() != null) {
                                errorMessage += " " + reauthTask.getException().getMessage();
                            }
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            Log.e("ChangePasswordDialog", "Re-authentication failed: " + errorMessage, reauthTask.getException());
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No user logged in or email not found.", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private void sendPasswordResetEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            mAuth.sendPasswordResetEmail(user.getEmail())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Password reset email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
                            dismiss();
                        } else {
                            String errorMessage = "Failed to send reset email.";
                            if (task.getException() != null) {
                                errorMessage += " " + task.getException().getMessage();
                            }
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            Log.e("ChangePasswordDialog", "Send reset email failed: " + errorMessage, task.getException());
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No user logged in to send reset email.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}