package com.example.dailybite;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class meal_input extends AppCompatActivity implements foodAdapter.OnFoodItemDeletedListener {

    private TextView caloriesText, proteinsText, fatsText, carbsText, meal_title;
    private RecyclerView foodRecyclerView;
    private foodAdapter foodAdapter;
    private Button saveButton;
    private ImageButton addButton, closeButton;
    private String mealName;

    //added
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_input);

        //Username for FIRESTORE saving
        Intent i = getIntent();
        username = i.getStringExtra("USERNAME");

        // Initialize views
        caloriesText = findViewById(R.id.caloriesText);
        proteinsText = findViewById(R.id.proteinsText);
        fatsText = findViewById(R.id.fatsText);
        carbsText = findViewById(R.id.carbsText);
        foodRecyclerView = findViewById(R.id.foodRecyclerView);
        saveButton = findViewById(R.id.saveButton);
        addButton = findViewById(R.id.add_button);
        closeButton = findViewById(R.id.close_button);
        meal_title = findViewById(R.id.meal_title);

        // Get meal name from the intent
        mealName = getIntent().getStringExtra("MEAL_NAME");
        if (mealName == null || mealName.trim().isEmpty()) {
            mealName = "New Meal";
        }
        meal_title.setText(mealName);

        // Set up RecyclerView
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new foodAdapter(this, getSampleFoodItems(), this); // Pass the sample food items and context
        foodRecyclerView.setAdapter(foodAdapter);

        // Add button click opens FoodSearchActivity
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(meal_input.this, FoodSearchActivity.class);
            startActivity(intent);
        });

        // Close button to finish activity
        closeButton.setOnClickListener(v -> finish());

        // Save button logic
        saveButton.setOnClickListener(v -> saveMeal());

        // Calculate total nutrients
        calculateTotalNutrients();
    }

    private void saveMeal() {
        String calories = caloriesText.getText().toString();
        String proteins = proteinsText.getText().toString();
        String fats = fatsText.getText().toString();
        String carbs = carbsText.getText().toString();
        String newMealName = meal_title.getText().toString();

        // Create an Intent to hold the meal data
        Intent resultIntent = new Intent();
        resultIntent.putExtra("MEAL_NAME", newMealName);
        resultIntent.putExtra("MEAL_CALORIES", calories);
        resultIntent.putExtra("MEAL_PROTEINS", proteins);
        resultIntent.putExtra("MEAL_FATS", fats);
        resultIntent.putExtra("MEAL_CARBS", carbs);

        //ADDED TO STORE TO FIRESTORE

        Meal meal = new Meal(newMealName, getCurrentTime(), Float.parseFloat(calories), Float.parseFloat(proteins), Float.parseFloat(fats), Float.parseFloat(carbs));
        // Add meal to the "meals" sub-collection under the user's document
        db.collection("users").document(username).collection("meals")
                .add(meal) // Automatically generates a unique ID for the meal
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Meal saved to Firestore!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Set the result and finish the activity
        setResult(RESULT_OK, resultIntent);
        Toast.makeText(this, "Meal saved: " + newMealName + " Calories: " + calories +
                " Proteins: " + proteins + " Fats: " + fats + " Carbs: " + carbs, Toast.LENGTH_SHORT).show();

        finish();
    }

    // Helper method to get the current time in the required format
    private String getCurrentTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    // Sample data for food items
    private List<foodItem> getSampleFoodItems() {
        List<foodItem> foodItems = new ArrayList<>();
        foodItems.add(new foodItem("Fried eggs", 100, 6, 1, 20));
        foodItems.add(new foodItem("Apple", 116, 1, 30, 0));
        foodItems.add(new foodItem("Banana", 90, 1, 22, 0));
        return foodItems;
    }

    // Calculate total nutrients
    private void calculateTotalNutrients() {
        int totalCalories = 0;
        float totalProteins = 0;
        float totalFats = 0;
        float totalCarbs = 0;

        for (foodItem item : foodAdapter.getFoodList()) {
            totalCalories += item.getCalories();
            totalProteins += item.getProteins();
            totalFats += item.getFats();
            totalCarbs += item.getCarbs();
        }

        caloriesText.setText(String.valueOf(totalCalories));
        proteinsText.setText(String.valueOf(totalProteins));
        fatsText.setText(String.valueOf(totalFats));
        carbsText.setText(String.valueOf(totalCarbs));
    }

    // Callback when food item is deleted
    @Override
    public void onFoodItemDeleted() {
        calculateTotalNutrients();
    }


}
