package com.jtdev.cs2a_group3.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jtdev.cs2a_group3.R;
import com.jtdev.cs2a_group3.activities.AccountInfoActivity;
import com.jtdev.cs2a_group3.activities.ContactSupportActivity;
import com.jtdev.cs2a_group3.activities.SupportUsActivity;

public class AccountPage extends Fragment {
    ImageButton accountInfoBtn, contactSupportBtn, supportUsBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_page, container, false);

        accountInfoBtn = view.findViewById(R.id.account_info_btn);
        contactSupportBtn = view.findViewById(R.id.contact_support_btn);
        supportUsBtn = view.findViewById(R.id.support_us_btn);

        accountInfoBtn.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AccountInfoActivity.class));
        });

        contactSupportBtn.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ContactSupportActivity.class));
        });

        supportUsBtn.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SupportUsActivity.class));
        });
        return view;
    }
}
