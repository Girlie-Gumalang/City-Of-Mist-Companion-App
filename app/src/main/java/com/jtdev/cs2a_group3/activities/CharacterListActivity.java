package com.jtdev.cs2a_group3.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jtdev.cs2a_group3.R;
import com.jtdev.cs2a_group3.adapters.CharacterAdapter;
import com.jtdev.cs2a_group3.database.DatabaseHelper;
import com.jtdev.cs2a_group3.dialogs.CharacterOptionsDialogFragment;
import com.jtdev.cs2a_group3.dialogs.ConfirmationDialogFragment;
import com.jtdev.cs2a_group3.models.Character;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterListActivity extends AppCompatActivity
        implements CharacterOptionsDialogFragment.CharacterOptionListener,
        ConfirmationDialogFragment.ConfirmationDialogListener {

    private DatabaseHelper dbHelper;
    private RecyclerView characterRecyclerView;
    private CharacterAdapter characterAdapter;
    private List<Character> characterList;
    private ImageView addCharacterFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_characters_page);

        dbHelper = new DatabaseHelper(this);

        characterRecyclerView = findViewById(R.id.recyclerViewCharacters);
        addCharacterFab = findViewById(R.id.fab_main);

        characterRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadCharacterList();

        addCharacterFab.setOnClickListener(v -> {

            Character newCharacter = new Character();

            long newCharacterId = dbHelper.addCharacter(newCharacter);

            if (newCharacterId != -1) {
                Intent intent = new Intent(CharacterListActivity.this, CharacterCardActivity.class);
                intent.putExtra(CharacterCardActivity.EXTRA_CHARACTER_ID, newCharacterId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Failed to create new character.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCharacterList();
    }

    private void loadCharacterList() {
        characterList = dbHelper.getAllCharacters();
        if (characterAdapter == null) {

            characterAdapter = new CharacterAdapter(this, characterList, new CharacterAdapter.OnCharacterClickListener() {
                @Override
                public void onCharacterClick(long characterId) {
                    Intent intent = new Intent(CharacterListActivity.this, CharacterCardActivity.class);
                    intent.putExtra(CharacterCardActivity.EXTRA_CHARACTER_ID, characterId);
                    startActivity(intent);
                }

                @Override
                public void onCharacterOptionsClick(long characterId) {
                    CharacterOptionsDialogFragment dialogFragment = new CharacterOptionsDialogFragment();
                    Bundle args = new Bundle();
                    args.putLong(CharacterOptionsDialogFragment.ARG_CHARACTER_ID, characterId);
                    dialogFragment.setArguments(args);
                    dialogFragment.setCharacterOptionListener(CharacterListActivity.this);
                    dialogFragment.show(getSupportFragmentManager(), "CharacterOptionsDialog");
                }
            });
            characterRecyclerView.setAdapter(characterAdapter);
        } else {
            characterAdapter.updateList(characterList);
        }
    }

    @Override
    public void onDeleteSelected(long charId) {
        Log.d("CharacterListActivity", "onDeleteSelected triggered for ID: " + charId);

        ConfirmationDialogFragment confirmationDialog = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ConfirmationDialogFragment.ARG_CHARACTER_ID, charId);
        args.putString(ConfirmationDialogFragment.ARG_TITLE, "Confirm Deletion");
        args.putString(ConfirmationDialogFragment.ARG_MESSAGE, getString(R.string.confirmation_message_delete_character));
        args.putString(ConfirmationDialogFragment.ARG_CONFIRM_TEXT, "DELETE");
        confirmationDialog.setArguments(args);
        confirmationDialog.setConfirmationDialogListener(this);
        confirmationDialog.show(getSupportFragmentManager(), "ConfirmationDialog");
    }

    @Override
    public void onExportSelected(long charId) {
        Log.d("CharacterListActivity", "onExportSelected triggered for ID: " + charId);
        exportCharacterById(charId);
    }

    @Override
    public void onConfirmDelete(long id) {

        if (dbHelper.deleteCharacter(id)) {
            Toast.makeText(this, "Character deleted successfully!", Toast.LENGTH_SHORT).show();
            loadCharacterList(); // Refresh the list after deletion
        } else {
            Toast.makeText(this, "Failed to delete character.", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportCharacterById(long charId) {
        Character characterToExport = dbHelper.getCharacter(charId);
        if (characterToExport != null) {
            performCharacterExport(characterToExport);
        } else {
            Toast.makeText(this, "Failed to find character for export.", Toast.LENGTH_SHORT).show();
            Log.e("CharacterListActivity", "Character with ID " + charId + " not found for export.");
        }
    }

    private void performCharacterExport(Character characterToExport) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(characterToExport);

        File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "CharacterExports");
        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                Log.e("CharacterListActivity", "Failed to create directory: " + exportDir.getAbsolutePath());
                Toast.makeText(this, "Failed to create export directory.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        String fileName = characterToExport.getName().replaceAll("[^a-zA-Z0-9.\\-]", "_") + "_" + characterToExport.getId() + ".json";
        File exportFile = new File(exportDir, fileName);

        try {
            FileWriter writer = new FileWriter(exportFile);
            writer.write(jsonString);
            writer.flush();
            writer.close();

            Toast.makeText(this, "Sheet exported to: " + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.d("CharacterListActivity", "Sheet exported to: " + exportFile.getAbsolutePath());

            shareExportedFile(exportFile);

        } catch (IOException e) {
            Log.e("CharacterListActivity", "Error exporting sheet: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to export sheet: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void shareExportedFile(File file) {
        if (file == null || !file.exists()) {
            Toast.makeText(this, "Exported file not found to share.", Toast.LENGTH_SHORT).show();
            Log.e("CharacterListActivity", "Attempted to share a non-existent file: " + (file != null ? file.getAbsolutePath() : "null"));
            return;
        }

        Uri fileUri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                file
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/json");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(shareIntent, "Share Character Sheet"));
        } catch (Exception e) {
            Toast.makeText(this, "No app available to share file.", Toast.LENGTH_LONG).show();
            Log.e("CharacterListActivity", "Error sharing file: " + e.getMessage(), e);
        }
    }
}