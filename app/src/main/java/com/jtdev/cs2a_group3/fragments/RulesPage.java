package com.jtdev.cs2a_group3.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jtdev.cs2a_group3.R;
import com.jtdev.cs2a_group3.adapters.RulesAdapter;
import com.jtdev.cs2a_group3.models.RuleModel;
import java.util.ArrayList;
import java.util.List;

public class RulesPage extends Fragment {
    RecyclerView recyclerView;
    RulesAdapter adapter;
    List<RuleModel> ruleList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rules_page, container, false);
        recyclerView = view.findViewById(R.id.rulesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ruleList = new ArrayList<>();
        ruleList.add(new RuleModel("Change the Game", "Gain an advantage for you and your allies, gaining Juice and extra effects on a success."));
        ruleList.add(new RuleModel("Investigate", "Gain information by searching an area or interrogation, gaining Clues on a success."));

        adapter = new RulesAdapter(ruleList, getActivity());
        recyclerView.setAdapter(adapter);

        return view;
    }
}
