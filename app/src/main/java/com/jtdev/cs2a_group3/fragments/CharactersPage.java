package com.jtdev.cs2a_group3.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jtdev.cs2a_group3.R;
import com.jtdev.cs2a_group3.activities.CharacterCardActivity;
import com.jtdev.cs2a_group3.adapters.CharacterAdapter;
import com.jtdev.cs2a_group3.database.DatabaseHelper;
import com.jtdev.cs2a_group3.dialogs.CharacterOptionsDialogFragment;
import com.jtdev.cs2a_group3.dialogs.ConfirmationDialogFragment;
import com.jtdev.cs2a_group3.dialogs.SelectOptionDialogFragment;
import com.jtdev.cs2a_group3.dialogs.CreateCharacterDialogFragment;
import com.jtdev.cs2a_group3.dialogs.ImportCharacterDialogFragment;
import com.jtdev.cs2a_group3.models.Character;
import com.jtdev.cs2a_group3.utils.FileImporterFromLinkOrCode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CharactersPage extends Fragment
        implements CharacterOptionsDialogFragment.CharacterOptionListener,
        ConfirmationDialogFragment.ConfirmationDialogListener,
        SelectOptionDialogFragment.SelectOptionListener,
        CreateCharacterDialogFragment.CharacterCreationListener,
        ImportCharacterDialogFragment.ImportCharacterListener {

    private static final String TAG = "CharactersPage";

    private static final int PICK_JSON_FILE_REQUEST_CODE = 1001;

    private DatabaseHelper dbHelper;
    private RecyclerView characterRecyclerView;
    private CharacterAdapter characterAdapter;
    private List<Character> characterList;
    private EditText searchEditText;
    private FloatingActionButton addCharacterFab;
    private ProgressBar progressBarCharacters;
    private TextView pleaseWaitText;
    private TextView emptyStateTextView;

    public CharactersPage() {
    }

    public static CharactersPage newInstance() {
        CharactersPage fragment = new CharactersPage();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private String getLoggedInUserId() {
        return "exampleUserId123";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_characters_page, container, false);

        dbHelper = new DatabaseHelper(getContext());

        characterRecyclerView = view.findViewById(R.id.recyclerViewCharacters);
        addCharacterFab = view.findViewById(R.id.fab_main);
        searchEditText = view.findViewById(R.id.searchViewCharacters);
        progressBarCharacters = view.findViewById(R.id.progressBarCharacters);
        pleaseWaitText = view.findViewById(R.id.pleaseWaitText);
        emptyStateTextView = view.findViewById(R.id.empty_state_textview);

        characterRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadCharacterList();

        addCharacterFab.setOnClickListener(v -> showSelectOptionDialog());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCharacters(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void showSelectOptionDialog() {
        SelectOptionDialogFragment dialogFragment = new SelectOptionDialogFragment();
        dialogFragment.setSelectOptionListener(this);
        if (getParentFragmentManager() != null) {
            dialogFragment.show(getParentFragmentManager(), "SelectOptionDialog");
        }
    }
    @Override
    public void onCreateCharacterSelected() {
        showCreateCharacterDialog();
    }

    @Override
    public void onImportCharacterSelected() {
        showImportCharacterDialog();
    }

    private void showCreateCharacterDialog() {
        CreateCharacterDialogFragment dialogFragment = new CreateCharacterDialogFragment();
        dialogFragment.setCharacterCreationListener(this);
        if (getParentFragmentManager() != null) {
            dialogFragment.show(getParentFragmentManager(), "CreateCharacterDialog");
        }
    }

    private void showImportCharacterDialog() {
        ImportCharacterDialogFragment dialogFragment = new ImportCharacterDialogFragment();
        Bundle args = new Bundle();
        args.putString(ImportCharacterDialogFragment.ARG_USER_ID, getLoggedInUserId());
        dialogFragment.setArguments(args);
        dialogFragment.setImportCharacterListener(this);
        if (getParentFragmentManager() != null) {
            dialogFragment.show(getParentFragmentManager(), "ImportCharacterDialog");
        }
    }

    @Override
    public void onCharacterCreated(long characterId) {
        loadCharacterList();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), CharacterCardActivity.class);
            intent.putExtra(CharacterCardActivity.EXTRA_CHARACTER_ID, characterId);
            startActivity(intent);
        }
    }

    @Override
    public void onCharacterImportedSuccessfully() {
        loadCharacterList();
        Toast.makeText(getContext(), "Character imported successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPickFileForImport() {
        importCharacterJson();
    }

    private void importCharacterJson() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        if (getActivity() != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Character JSON"), PICK_JSON_FILE_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_JSON_FILE_REQUEST_CODE && getActivity() != null && resultCode == getActivity().RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri jsonUri = data.getData();
                if (getContext() == null) {
                    Log.e(TAG, "Context is null in onActivityResult, cannot import file.");
                    Toast.makeText(getContext(), "Error: Context not available for import.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FileImporterFromLinkOrCode.importFromFile(getContext(), jsonUri, getLoggedInUserId(), new FileImporterFromLinkOrCode.ImportCallback() {
                    @Override
                    public void onImportSuccess(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                loadCharacterList();
                            });
                        }
                    }

                    @Override
                    public void onImportFailure(String errorMessage) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                                Log.e(TAG, "File import failure: " + errorMessage);
                            });
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCharacterList();
    }

    private void loadCharacterList() {
        if (progressBarCharacters != null) progressBarCharacters.setVisibility(View.VISIBLE);
        if (pleaseWaitText != null) pleaseWaitText.setVisibility(View.VISIBLE);
        if (emptyStateTextView != null) emptyStateTextView.setVisibility(View.GONE);
        if (characterRecyclerView != null) characterRecyclerView.setVisibility(View.GONE);

        characterList = dbHelper.getAllCharacters();

        if (characterList.isEmpty()) {
            if (emptyStateTextView != null) emptyStateTextView.setVisibility(View.VISIBLE);
            if (characterRecyclerView != null) characterRecyclerView.setVisibility(View.GONE);
        } else {
            if (emptyStateTextView != null) emptyStateTextView.setVisibility(View.GONE);
            if (characterRecyclerView != null) characterRecyclerView.setVisibility(View.VISIBLE);
        }

        if (characterAdapter == null) {
            if (getContext() != null) {
                characterAdapter = new CharacterAdapter(getContext(), characterList, new CharacterAdapter.OnCharacterClickListener() {
                    @Override
                    public void onCharacterClick(long characterId) {
                        if (getActivity() != null) {
                            Intent intent = new Intent(getActivity(), CharacterCardActivity.class);
                            intent.putExtra(CharacterCardActivity.EXTRA_CHARACTER_ID, characterId);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCharacterOptionsClick(long characterId) {
                        CharacterOptionsDialogFragment dialogFragment = new CharacterOptionsDialogFragment();
                        Bundle args = new Bundle();
                        args.putLong(CharacterOptionsDialogFragment.ARG_CHARACTER_ID, characterId);
                        dialogFragment.setArguments(args);
                        dialogFragment.setCharacterOptionListener(CharactersPage.this);
                        if (getParentFragmentManager() != null) {
                            dialogFragment.show(getParentFragmentManager(), "CharacterOptionsDialog");
                        }
                    }
                });
                characterRecyclerView.setAdapter(characterAdapter);
            }
        } else {
            characterAdapter.updateList(characterList);
        }

        if (progressBarCharacters != null) progressBarCharacters.setVisibility(View.GONE);
        if (pleaseWaitText != null) pleaseWaitText.setVisibility(View.GONE);
    }

    private void filterCharacters(String searchText) {
        List<Character> filteredList = new ArrayList<>();
        List<Character> allCharacters = dbHelper.getAllCharacters();
        if (searchText.isEmpty()) {
            filteredList.addAll(allCharacters);
        } else {
            for (Character character : allCharacters) {
                if (character.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(character);
                }
            }
        }
        if (characterAdapter != null) {
            characterAdapter.filterList(filteredList);
        }
        if (emptyStateTextView != null) {
            emptyStateTextView.setVisibility(filteredList.isEmpty() && searchText.isEmpty() ? View.VISIBLE : View.GONE);
            if (!searchText.isEmpty() && filteredList.isEmpty()) {
                emptyStateTextView.setText("No characters found matching your search.");
                emptyStateTextView.setVisibility(View.VISIBLE);
            } else if (searchText.isEmpty() && filteredList.isEmpty()){
                emptyStateTextView.setText("You don't have any characters yet. Create one!");
                emptyStateTextView.setVisibility(View.VISIBLE);
            } else {
                emptyStateTextView.setVisibility(View.GONE);
            }
        }
        if (characterRecyclerView != null) {
            characterRecyclerView.setVisibility(filteredList.isEmpty() && searchText.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onDeleteSelected(long charId) {
        Log.d("CharactersPage", "onDeleteSelected triggered for ID: " + charId);
        ConfirmationDialogFragment confirmationDialog = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ConfirmationDialogFragment.ARG_CHARACTER_ID, charId);
        args.putString(ConfirmationDialogFragment.ARG_TITLE, "Confirm Deletion");
        if (getContext() != null) {
            args.putString(ConfirmationDialogFragment.ARG_MESSAGE, getContext().getString(R.string.confirmation_message_delete_character));
        }
        args.putString(ConfirmationDialogFragment.ARG_CONFIRM_TEXT, "DELETE");
        confirmationDialog.setArguments(args);
        confirmationDialog.setConfirmationDialogListener(this);
        if (getParentFragmentManager() != null) {
            confirmationDialog.show(getParentFragmentManager(), "ConfirmationDialog");
        }
    }

    @Override
    public void onExportSelected(long charId) {
        Log.d("CharactersPage", "onExportSelected triggered for ID: " + charId);
        exportCharacterById(charId);
    }

    @Override
    public void onConfirmDelete(long id) {
        if (dbHelper.deleteCharacter(id)) {
            Toast.makeText(getContext(), "Character deleted successfully!", Toast.LENGTH_SHORT).show();
            loadCharacterList();
        } else {
            Toast.makeText(getContext(), "Failed to delete character.", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportCharacterById(long charId) {
        Character characterToExport = dbHelper.getCharacter(charId);
        if (characterToExport != null) {
            com.jtdev.cs2a_group3.utils.FileExporterImporter.exportCharacter(getContext(), characterToExport, new com.jtdev.cs2a_group3.utils.FileExporterImporter.ExportCallback() {
                @Override
                public void onExportSuccess(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                            Log.d(TAG, message);
                        });
                    }
                }

                @Override
                public void onExportFailure(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Export failed: " + error, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Export failure: " + error);
                        });
                    }
                }
            });
        } else {
            Log.e("CharactersPage", "Character with ID " + charId + " not found for export.");
            Toast.makeText(getContext(), "Failed to find character for export.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareExportedFile(File file) {
        if (file == null || !file.exists()) {
            Toast.makeText(getContext(), "Exported file not found to share.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Attempted to share a non-existent file: " + (file != null ? file.getAbsolutePath() : "null"));
            return;
        }

        if (getContext() == null) {
            Log.e(TAG, "Context is null, cannot share file.");
            return;
        }

        Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                getContext(),
                getContext().getApplicationContext().getPackageName() + ".fileprovider",
                file
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/json");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(shareIntent, "Share Character Sheet"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "No app available to share file.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error sharing file: " + e.getMessage(), e);
        }
    }
}