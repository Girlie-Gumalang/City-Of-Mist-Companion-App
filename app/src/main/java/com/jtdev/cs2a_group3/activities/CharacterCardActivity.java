package com.jtdev.cs2a_group3.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.jtdev.cs2a_group3.R;
import com.jtdev.cs2a_group3.database.DatabaseHelper;
import com.jtdev.cs2a_group3.dialogs.ConfirmationDialogFragment;
import com.jtdev.cs2a_group3.models.Character;
import com.jtdev.cs2a_group3.models.Crewmate;
import com.jtdev.cs2a_group3.models.StoryTag;
import com.jtdev.cs2a_group3.utils.InputFilterMinMax;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CharacterCardActivity extends AppCompatActivity
        implements ConfirmationDialogFragment.ConfirmationDialogListener {

    public static final String EXTRA_CHARACTER_ID = "extra_character_id";
    private long characterId;
    private DatabaseHelper dbHelper;
    private Character currentCharacter;


    private ImageView backButton;
    private ImageView characterImage;
    private TextView characterName;
    private TextView characterLogos;
    private TextView characterMythos;
    private TextView helpEtLabel;
    private TextView hurtEtLabel;
    private LinearLayout crewmatesListContainer;
    private Button addCrewmateBtn;
    private LinearLayout storyTagsListContainer;
    private Button addStoryTagBtn;
    private EditText nemesesEditText;
    private EditText notesEditText;

    private CheckBox[] buildUpCheckboxes = new CheckBox[5];
    private View[] improvementViews = new View[11];
    private CheckBox[] improvementCheckboxes = new CheckBox[11];
    private TextView[] improvementTexts = new TextView[11];
    private String[] improvementDescriptions;

    private ActivityResultLauncher<String> pickImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_card);

        dbHelper = new DatabaseHelper(this);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            String base64Image = encodeImageToBase64(bitmap);

                            if (currentCharacter != null) {
                                currentCharacter.setImageUrl(base64Image);
                                dbHelper.updateCharacter(currentCharacter);
                                Glide.with(this)
                                        .asBitmap() // Tell Glide to expect a bitmap
                                        .load(Base64.decode(base64Image, Base64.DEFAULT))
                                        .placeholder(R.drawable.image_char)
                                        .error(R.drawable.image_char)
                                        .into(characterImage);
                            }
                        } catch (Exception e) {
                            Log.e("CharacterCardActivity", "Error loading image: " + e.getMessage());
                            Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        improvementDescriptions = getResources().getStringArray(R.array.improvement_options);
        if (improvementDescriptions.length != 11) {
            Log.e("CharacterCardActivity", "Mismatch in improvement_options array size. Expected 11, got " + improvementDescriptions.length);
            Toast.makeText(this, "Error: Improvement options not correctly loaded.", Toast.LENGTH_LONG).show();
        }

        backButton = findViewById(R.id.back_button);
        characterImage = findViewById(R.id.character_image);

        characterName = findViewById(R.id.character_name);
        characterLogos = findViewById(R.id.character_logos);
        characterMythos = findViewById(R.id.character_mythos);

        helpEtLabel = findViewById(R.id.help_et);
        hurtEtLabel = findViewById(R.id.hurt_et);
        crewmatesListContainer = findViewById(R.id.crewmates_list_container);
        addCrewmateBtn = findViewById(R.id.add_crewmate_btn);
        storyTagsListContainer = findViewById(R.id.story_tags_list_container);
        addStoryTagBtn = findViewById(R.id.add_story_tag_btn);
        nemesesEditText = findViewById(R.id.nemeses_edittext);
        notesEditText = findViewById(R.id.notes_edittext);

        buildUpCheckboxes[0] = findViewById(R.id.buildup_checkbox_1);
        buildUpCheckboxes[1] = findViewById(R.id.buildup_checkbox_2);
        buildUpCheckboxes[2] = findViewById(R.id.buildup_checkbox_3);
        buildUpCheckboxes[3] = findViewById(R.id.buildup_checkbox_4);
        buildUpCheckboxes[4] = findViewById(R.id.buildup_checkbox_5);

        for (int i = 0; i < 11; i++) {
            int resId = getResources().getIdentifier("improvement_" + (i + 1), "id", getPackageName());
            improvementViews[i] = findViewById(resId);
            if (improvementViews[i] != null) {
                improvementCheckboxes[i] = improvementViews[i].findViewById(R.id.improvement_checkbox);
                improvementTexts[i] = improvementViews[i].findViewById(R.id.improvement_text);

                if (i < improvementDescriptions.length) {
                    improvementTexts[i].setText(improvementDescriptions[i]);
                } else {
                    improvementTexts[i].setText("Improvement " + (i + 1) + " (Description Missing)");
                    Log.w("CharacterCardActivity", "Improvement description missing for index " + i);
                }
            } else {
                Log.w("CharacterCardActivity", "Could not find improvement_" + (i + 1) + " include.");
            }
        }

        characterId = getIntent().getLongExtra(EXTRA_CHARACTER_ID, -1);
        if (characterId == -1) {
            Log.e("CharacterCardActivity", "Received -1L for EXTRA_CHARACTER_ID. This activity should receive a valid ID from CharacterListActivity.");
            Toast.makeText(this, "Error: New character ID not passed correctly.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadCharacterData(characterId);
        setupListeners();
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        characterImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));


        if (characterName instanceof EditText) {
            ((EditText) characterName).addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (currentCharacter != null) {
                        currentCharacter.setName(s.toString());
                        dbHelper.updateCharacter(currentCharacter);
                    }
                }
            });
        }
        if (characterLogos instanceof EditText) {
            ((EditText) characterLogos).addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (currentCharacter != null) {
                        currentCharacter.setLogos(s.toString());
                        dbHelper.updateCharacter(currentCharacter);
                    }
                }
            });
        }
        if (characterMythos instanceof EditText) {
            ((EditText) characterMythos).addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (currentCharacter != null) {
                        currentCharacter.setMythos(s.toString());
                        dbHelper.updateCharacter(currentCharacter);
                    }
                }
            });
        }

        addCrewmateBtn.setOnClickListener(v -> addCrewmate());
        addStoryTagBtn.setOnClickListener(v -> addStoryTag());

        nemesesEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (currentCharacter != null) {
                    currentCharacter.setNemeses(s.toString());
                    dbHelper.updateCharacter(currentCharacter);
                }
            }
        });
        notesEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (currentCharacter != null) {
                    currentCharacter.setNotes(s.toString());
                    dbHelper.updateCharacter(currentCharacter);
                }
            }
        });


        for (int i = 0; i < buildUpCheckboxes.length; i++) {
            final int index = i;
            if (buildUpCheckboxes[i] != null) {
                buildUpCheckboxes[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (currentCharacter != null && currentCharacter.getBuildupStates() != null) {

                        while(currentCharacter.getBuildupStates().size() <= index) {
                            currentCharacter.getBuildupStates().add(false); // Add default value
                        }
                        currentCharacter.getBuildupStates().set(index, isChecked);
                        dbHelper.updateCharacter(currentCharacter);
                    }
                    // Optional: Toast.makeText(this, "Build-Up " + (index + 1) + " checked: " + isChecked, Toast.LENGTH_SHORT).show();
                });
            } else {
                Log.w("CharacterCardActivity", "Build-Up checkbox " + (index + 1) + " not found.");
            }
        }

        // Listeners for Improvement checkboxes
        for (int i = 0; i < improvementCheckboxes.length; i++) {
            if (improvementCheckboxes[i] != null) {
                final int index = i;
                improvementCheckboxes[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (currentCharacter != null && currentCharacter.getImprovementStates() != null) {

                        while(currentCharacter.getImprovementStates().size() <= index) {
                            currentCharacter.getImprovementStates().add(false); // Add default value
                        }
                        currentCharacter.getImprovementStates().set(index, isChecked);
                        dbHelper.updateCharacter(currentCharacter);
                    }
                    // Optional: Toast.makeText(this, "Improvement " + (index + 1) + " checked: " + isChecked, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void loadCharacterData(long id) {
        currentCharacter = dbHelper.getCharacter(id);
        if (currentCharacter != null) {

            if (characterName instanceof EditText) {
                ((EditText) characterName).setText(currentCharacter.getName());
            } else {
                characterName.setText(currentCharacter.getName());
            }
            if (characterLogos instanceof EditText) {
                ((EditText) characterLogos).setText(currentCharacter.getLogos());
            } else {
                characterLogos.setText(currentCharacter.getLogos());
            }
            if (characterMythos instanceof EditText) {
                ((EditText) characterMythos).setText(currentCharacter.getMythos());
            } else {
                characterMythos.setText(currentCharacter.getMythos());
            }

            // Load image using Base64 string
            if (currentCharacter.getImageUrl() != null && !currentCharacter.getImageUrl().isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(currentCharacter.getImageUrl(), Base64.DEFAULT);
                    Glide.with(this)
                            .asBitmap()
                            .load(decodedString)
                            .placeholder(R.drawable.image_char)
                            .error(R.drawable.image_char)
                            .into(characterImage);
                } catch (IllegalArgumentException e) {
                    Log.e("CharacterCardActivity", "Invalid Base64 string for image: " + e.getMessage());
                    characterImage.setImageResource(R.drawable.image_char);
                }
            } else {
                characterImage.setImageResource(R.drawable.image_char);
            }

            nemesesEditText.setText(currentCharacter.getNemeses());
            notesEditText.setText(currentCharacter.getNotes());

            List<Boolean> loadedBuildupStates = currentCharacter.getBuildupStates();
            if (loadedBuildupStates != null) {
                for (int i = 0; i < buildUpCheckboxes.length; i++) {
                    if (buildUpCheckboxes[i] != null) {
                        if (i < loadedBuildupStates.size()) {
                            buildUpCheckboxes[i].setChecked(loadedBuildupStates.get(i));
                        } else {
                            buildUpCheckboxes[i].setChecked(false);
                            Log.w("CharacterCardActivity", "Build-Up state missing for index " + i);
                        }
                    }
                }
            }


            List<Boolean> loadedImprovementStates = currentCharacter.getImprovementStates();
            if (loadedImprovementStates != null) {
                for (int i = 0; i < improvementCheckboxes.length; i++) {
                    if (improvementCheckboxes[i] != null) {
                        if (i < loadedImprovementStates.size()) {
                            improvementCheckboxes[i].setChecked(loadedImprovementStates.get(i));
                        } else {
                            improvementCheckboxes[i].setChecked(false);
                            Log.w("CharacterCardActivity", "Improvement state missing for index " + i);
                        }
                    }
                }
            }

            displayCrewmates(currentCharacter.getCrewmates());
            displayStoryTags(currentCharacter.getStoryTags());

        } else {
            Toast.makeText(this, "Failed to load character data.", Toast.LENGTH_SHORT).show();
            Log.e("CharacterCardActivity", "Character with ID " + id + " not found in database.");
            finish();
        }
    }

    private void addCrewmate() {

        String newCrewmateName = "New Crewmate";
        int newHelpValue = 0;
        int newHurtValue = 0;

        Crewmate newCrewmate = new Crewmate(newCrewmateName, newHelpValue, newHurtValue);
        if (currentCharacter.getCrewmates() == null) {
            currentCharacter.setCrewmates(new ArrayList<>());
        }
        currentCharacter.getCrewmates().add(newCrewmate);
        dbHelper.updateCharacter(currentCharacter);
        displayCrewmates(currentCharacter.getCrewmates());
        Toast.makeText(this, "Crewmate added!", Toast.LENGTH_SHORT).show();
    }


    private void displayCrewmates(List<Crewmate> crewmates) {
        crewmatesListContainer.removeAllViews();
        if (crewmates == null || crewmates.isEmpty()) {
            TextView noCrewmatesText = new TextView(this);
            noCrewmatesText.setText("No crewmates added yet.");
            noCrewmatesText.setTextColor(getResources().getColor(R.color.light_gray));
            noCrewmatesText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            noCrewmatesText.setPadding(0, 16, 0, 0);
            crewmatesListContainer.addView(noCrewmatesText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < crewmates.size(); i++) {
            Crewmate crewmate = crewmates.get(i);
            View crewmateView = inflater.inflate(R.layout.item_crewmate_display, crewmatesListContainer, false);

            ImageView removeCrewmateIcon = crewmateView.findViewById(R.id.remove_crewmate_icon);
            EditText crewmateNameEt = crewmateView.findViewById(R.id.crewmate_name_edittext);
            EditText crewmateHelpEt = crewmateView.findViewById(R.id.crewmate_help_value);
            EditText crewmateHurtEt = crewmateView.findViewById(R.id.crewmate_hurt_value);

            if (removeCrewmateIcon == null || crewmateNameEt == null || crewmateHelpEt == null || crewmateHurtEt == null) {
                Log.e("CharacterCardActivity", "Missing crewmate UI elements in layout. Check item_crewmate_display.xml IDs.");
                Toast.makeText(this, "Error: Missing crewmate UI elements in layout.", Toast.LENGTH_LONG).show();
                continue;
            }


            crewmateNameEt.setText(crewmate.getName());
            crewmateHelpEt.setText(String.valueOf(crewmate.getHelpValue()));
            crewmateHurtEt.setText(String.valueOf(crewmate.getHurtValue()));



            crewmateHelpEt.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "20")});
            crewmateHurtEt.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "20")});
            if (crewmateNameEt.getTag() instanceof TextWatcher) {
                crewmateNameEt.removeTextChangedListener((TextWatcher) crewmateNameEt.getTag());
            }
            if (crewmateHelpEt.getTag() instanceof TextWatcher) {
                crewmateHelpEt.removeTextChangedListener((TextWatcher) crewmateHelpEt.getTag());
            }
            if (crewmateHurtEt.getTag() instanceof TextWatcher) {
                crewmateHurtEt.removeTextChangedListener((TextWatcher) crewmateHurtEt.getTag());
            }


            TextWatcher nameTextWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (currentCharacter != null) {
                        crewmate.setName(s.toString());
                        dbHelper.updateCharacter(currentCharacter);
                    }
                }
            };
            crewmateNameEt.addTextChangedListener(nameTextWatcher);
            crewmateNameEt.setTag(nameTextWatcher);



            TextWatcher helpTextWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (currentCharacter != null) {
                        try {
                            int value = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                            if (value > 20) value = 20;
                            if (value < 0) value = 0;
                            crewmate.setHelpValue(value);
                            dbHelper.updateCharacter(currentCharacter);
                        } catch (NumberFormatException e) {
                            Log.e("CharacterCardActivity", "Invalid number for Crewmate Help: " + s.toString(), e);
                            crewmate.setHelpValue(0);
                            dbHelper.updateCharacter(currentCharacter);
                        }
                    }
                }
            };
            crewmateHelpEt.addTextChangedListener(helpTextWatcher);
            crewmateHelpEt.setTag(helpTextWatcher);

            TextWatcher hurtTextWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (currentCharacter != null) {
                        try {
                            int value = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                            if (value > 20) value = 20;
                            if (value < 0) value = 0;
                            crewmate.setHurtValue(value);
                            dbHelper.updateCharacter(currentCharacter);
                        } catch (NumberFormatException e) {
                            Log.e("CharacterCardActivity", "Invalid number for Crewmate Hurt: " + s.toString(), e);
                            crewmate.setHurtValue(0);
                            dbHelper.updateCharacter(currentCharacter);
                        }
                    }
                }
            };
            crewmateHurtEt.addTextChangedListener(hurtTextWatcher);
            crewmateHurtEt.setTag(hurtTextWatcher);



            final int index = i;
            removeCrewmateIcon.setOnClickListener(v -> {
                removeCrewmate(index);
            });

            crewmatesListContainer.addView(crewmateView);
        }
    }

    private void removeCrewmate(int index) {
        if (currentCharacter != null && currentCharacter.getCrewmates() != null && index >= 0 && index < currentCharacter.getCrewmates().size()) {
            String removedName = currentCharacter.getCrewmates().get(index).getName();
            currentCharacter.getCrewmates().remove(index);
            dbHelper.updateCharacter(currentCharacter);
            displayCrewmates(currentCharacter.getCrewmates());
            Toast.makeText(this, "Crewmate " + removedName + " removed.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("CharacterCardActivity", "Attempted to remove crewmate with invalid index or null list.");
        }
    }

    private void addStoryTag() {

        String newTagName = "New Tag";
        boolean newInvokeChecked = false;

        StoryTag newStoryTag = new StoryTag(newTagName, newInvokeChecked);
        if (currentCharacter.getStoryTags() == null) {
            currentCharacter.setStoryTags(new ArrayList<>());
        }
        currentCharacter.getStoryTags().add(newStoryTag);
        dbHelper.updateCharacter(currentCharacter);
        displayStoryTags(currentCharacter.getStoryTags());
        Toast.makeText(this, "Story Tag added!", Toast.LENGTH_SHORT).show();
    }

    private void displayStoryTags(List<StoryTag> storyTags) {
        storyTagsListContainer.removeAllViews();
        if (storyTags == null || storyTags.isEmpty()) {
            TextView noStoryTagsText = new TextView(this);
            noStoryTagsText.setText("No story tags added yet.");
            noStoryTagsText.setTextColor(getResources().getColor(R.color.light_gray));
            noStoryTagsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            noStoryTagsText.setPadding(0, 16, 0, 0);
            storyTagsListContainer.addView(noStoryTagsText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < storyTags.size(); i++) {
            StoryTag tag = storyTags.get(i);
            View tagView = inflater.inflate(R.layout.item_story_tag_display, storyTagsListContainer, false);

            ImageView removeStoryTagIcon = tagView.findViewById(R.id.remove_story_tag_icon);
            EditText storyTagNameEt = tagView.findViewById(R.id.story_tag_name_edittext);
            CheckBox storyTagInvokeCb = tagView.findViewById(R.id.story_tag_invoke_checkbox);

            if (removeStoryTagIcon == null) Log.e("CharacterCardActivity", "remove_story_tag_icon is NULL in item_story_tag_display.xml");
            if (storyTagNameEt == null) Log.e("CharacterCardActivity", "story_tag_name_edittext is NULL in item_story_tag_display.xml");
            if (storyTagInvokeCb == null) Log.e("CharacterCardActivity", "story_tag_invoke_checkbox is NULL in item_story_tag_display.xml");

            if (storyTagNameEt == null || storyTagInvokeCb == null || removeStoryTagIcon == null) {
                Toast.makeText(this, "Error: Missing story tag UI elements in layout. Check item_story_tag_display.xml IDs.", Toast.LENGTH_LONG).show();
                Log.e("CharacterCardActivity", "Skipping story tag due to missing UI elements.");
                continue;
            }

            storyTagNameEt.setText(tag.getTag());
            storyTagInvokeCb.setChecked(tag.isInvokeChecked());

            if (storyTagNameEt.getTag() instanceof TextWatcher) {
                storyTagNameEt.removeTextChangedListener((TextWatcher) storyTagNameEt.getTag());
            }

            TextWatcher tagNameTextWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (currentCharacter != null) {
                        tag.setTag(s.toString());
                        dbHelper.updateCharacter(currentCharacter);
                    }
                }
            };
            storyTagNameEt.addTextChangedListener(tagNameTextWatcher);
            storyTagNameEt.setTag(tagNameTextWatcher);


            storyTagInvokeCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (currentCharacter != null) {
                    tag.setInvokeChecked(isChecked);
                    dbHelper.updateCharacter(currentCharacter);
                }
            });

            final int index = i;
            removeStoryTagIcon.setOnClickListener(v -> {
                removeStoryTag(index);
            });

            storyTagsListContainer.addView(tagView);
        }
    }

    private void removeStoryTag(int index) {
        if (currentCharacter != null && currentCharacter.getStoryTags() != null && index >= 0 && index < currentCharacter.getStoryTags().size()) {
            String removedName = currentCharacter.getStoryTags().get(index).getTag();
            currentCharacter.getStoryTags().remove(index);
            dbHelper.updateCharacter(currentCharacter);
            displayStoryTags(currentCharacter.getStoryTags());
            Toast.makeText(this, "Story Tag " + removedName + " removed.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("CharacterCardActivity", "Attempted to remove story tag with invalid index or null list.");
        }
    }

    @Override
    public void onConfirmDelete(long id) {
        if (dbHelper.deleteCharacter(id)) {
            Toast.makeText(this, "Character deleted successfully!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete character.", Toast.LENGTH_SHORT).show();
        }
    }
}