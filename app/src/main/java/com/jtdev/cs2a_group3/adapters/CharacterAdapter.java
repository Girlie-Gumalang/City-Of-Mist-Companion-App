package com.jtdev.cs2a_group3.adapters;

import android.content.Context;
import android.util.Base64; // Added Base64 import for image loading
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jtdev.cs2a_group3.R;
import com.jtdev.cs2a_group3.models.Character;

import java.util.ArrayList;
import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder> {

    private List<Character> characterList;
    private List<Character> filteredCharacterList;
    private OnCharacterClickListener listener;
    private Context context;
    public CharacterAdapter(Context context, List<Character> characterList, OnCharacterClickListener listener) {
        this.context = context;
        this.characterList = new ArrayList<>(characterList);
        this.filteredCharacterList = new ArrayList<>(characterList);
        this.listener = listener;
    }

    public interface OnCharacterClickListener {
        void onCharacterClick(long characterId);
        void onCharacterOptionsClick(long characterId);
    }

    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.character_adapter, parent, false);
        return new CharacterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        Character character = filteredCharacterList.get(position);
        holder.characterName.setText(character.getName());

        // Load image using Base64 string
        if (character.getImageUrl() != null && !character.getImageUrl().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(character.getImageUrl(), Base64.DEFAULT);
                Glide.with(holder.characterImage.getContext())
                        .asBitmap()
                        .load(decodedString)
                        .placeholder(R.drawable.image_char)
                        .error(R.drawable.image_char)
                        .into(holder.characterImage);
            } catch (IllegalArgumentException e) {
                Log.e("CharacterAdapter", "Invalid Base64 string for image: " + e.getMessage());
                holder.characterImage.setImageResource(R.drawable.image_char);
            }
        } else {
            holder.characterImage.setImageResource(R.drawable.image_char);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCharacterClick(character.getId());
            }
        });

        holder.menuButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCharacterOptionsClick(character.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredCharacterList.size();
    }

    public void updateList(List<Character> newList) {
        this.characterList = new ArrayList<>(newList);
        filterList(new ArrayList<>(newList));
    }

    public void filterList(List<Character> filteredList) {
        this.filteredCharacterList.clear();
        this.filteredCharacterList.addAll(filteredList);
        notifyDataSetChanged();
    }


    static class CharacterViewHolder extends RecyclerView.ViewHolder {
        ImageView characterImage;
        TextView characterName;
        ImageView menuButton;

        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            characterImage = itemView.findViewById(R.id.characterImage);
            characterName = itemView.findViewById(R.id.characterName);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}