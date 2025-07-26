package com.jtdev.cs2a_group3.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jtdev.cs2a_group3.R;
import com.jtdev.cs2a_group3.activities.GameRulesActivity;
import com.jtdev.cs2a_group3.models.RuleModel;

import java.util.List;

public class RulesAdapter extends RecyclerView.Adapter<RulesAdapter.RuleViewHolder> {

    List<RuleModel> rules;
    Context context;

    public RulesAdapter(List<RuleModel> rules, Context context) {
        this.rules = rules;
        this.context = context;
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule, parent, false);
        return new RuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        RuleModel rule = rules.get(position);


        holder.ruleTitle.setText(rule.getTitle());
        holder.ruleDesc.setText(rule.getDescription());

        if ("Change the Game".equals(rule.getTitle())) {

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, GameRulesActivity.class);
                intent.putExtra("RULE_NAME", rule.getTitle());
                context.startActivity(intent);
            });
        } else {
            holder.itemView.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }


    public static class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView ruleTitle, ruleDesc;

        public RuleViewHolder(View itemView) {
            super(itemView);
            ruleTitle = itemView.findViewById(R.id.ruleTitle);
            ruleDesc = itemView.findViewById(R.id.ruleDesc);
        }
    }
}