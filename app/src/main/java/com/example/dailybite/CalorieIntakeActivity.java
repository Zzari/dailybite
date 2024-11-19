package com.example.dailybite;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CalorieIntakeActivity extends AppCompatActivity {

    private TextView caloriesText, proteinsText, fatsText, carbsText, waterText;
    private Button saveButton;
    private ImageButton backButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "CalorieIntakeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_intake);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        caloriesText = findViewById(R.id.caloriesText);
        proteinsText = findViewById(R.id.proteinsText);
        fatsText = findViewById(R.id.fatsText);
        carbsText = findViewById(R.id.carbsText);
        waterText = findViewById(R.id.waterText);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.back_button_calorie_intake);

        backButton.setOnClickListener(v -> finish());

        // Fetch intake data from Firestore and display it
        fetchIntakeData();

        caloriesText.setOnClickListener(v -> showEditDialog("Edit Calories", caloriesText, "cal"));
        proteinsText.setOnClickListener(v -> showEditDialog("Edit Proteins", proteinsText, "g"));
        fatsText.setOnClickListener(v -> showEditDialog("Edit Fats", fatsText, "g"));
        carbsText.setOnClickListener(v -> showEditDialog("Edit Carbs", carbsText, "g"));
        waterText.setOnClickListener(v -> showEditDialog("Edit Water", waterText, "ml"));

        saveButton.setOnClickListener(v -> saveIntakeData());

    }

    private void fetchIntakeData() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> intake = (Map<String, Object>) documentSnapshot.get("intake");
                        if (intake != null) {
                            caloriesText.setText(intake.get("calories") + " cal");
                            proteinsText.setText(intake.get("proteins") + " g");
                            fatsText.setText(intake.get("fats") + " g");
                            carbsText.setText(intake.get("carbs") + " g");
                            waterText.setText("2500 ml"); // Default value, could be customized if needed
                        }
                    } else {
                        Log.d(TAG, "No intake data found for this user.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error fetching intake data", e);
                    Toast.makeText(CalorieIntakeActivity.this, "Failed to load intake data", Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditDialog(String title, TextView textViewToUpdate, String unit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CalorieIntakeActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_value, null);
        builder.setView(dialogView);

        EditText valueInput = dialogView.findViewById(R.id.editValueInput);
        valueInput.setText(textViewToUpdate.getText().toString().replace(unit, "").trim());

        builder.setTitle(title)
                .setPositiveButton("Done", (dialog, id) -> {
                    String newValue = valueInput.getText().toString() + " " + unit;
                    textViewToUpdate.setText(newValue);
                    Toast.makeText(CalorieIntakeActivity.this, title + " updated to: " + newValue, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void saveIntakeData() {
        String userId = mAuth.getCurrentUser ().getUid();
        Map<String, Object> intakeData = new HashMap<>();
        intakeData.put("calories", caloriesText.getText().toString().replace(" cal", ""));
        intakeData.put("proteins", proteinsText.getText().toString().replace(" g", ""));
        intakeData.put("fats", fatsText.getText().toString().replace(" g", ""));
        intakeData.put("carbs", carbsText.getText().toString().replace(" g", ""));
        intakeData.put("water", waterText.getText().toString().replace(" ml", ""));

        db.collection("users").document(userId).update("intake", intakeData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CalorieIntakeActivity.this, "Intake data saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving intake data", e);
                    Toast.makeText(CalorieIntakeActivity.this, "Failed to save intake data", Toast.LENGTH_SHORT).show();
                });
    }
}