package com.example.lab5_starter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private com.google.firebase.firestore.FirebaseFirestore db;
    private com.google.firebase.firestore.CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

//        addDummyData();
        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener(new com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable com.google.firebase.firestore.QuerySnapshot value,
                                @androidx.annotation.Nullable com.google.firebase.firestore.FirebaseFirestoreException error) {
                if (error != null) {
                    android.util.Log.e("Firestore", error.toString());
                    return;
                }

                if (value != null) {
                    cityArrayList.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                        String name = doc.getString("name");
                        String province = doc.getString("province");
                        cityArrayList.add(new City(name, province));
                    }

                    cityArrayAdapter.notifyDataSetChanged();
                }
            }
        });

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

        // Listen for a Long Click to delete a city
        cityListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                City cityToDelete = cityArrayAdapter.getItem(position);

                deleteCityFromFirestore(cityToDelete);

                return true;
            }
        });

    }

    @Override
    public void updateCity(City city, String newName, String newProvince) {
        // 1. Delete the OLD city from Firestore using its current name
        citiesRef.document(city.getName()).delete();

        // 2. Update the city object with the new details
        city.setName(newName);
        city.setProvince(newProvince);

        // 3. Add the NEW city to Firestore
        citiesRef.document(city.getName()).set(city);

        // Note: We don't need to manually update the ArrayList because our SnapshotListener
        // will automatically detect these changes and refresh the ListView!
    }

    @Override
    public void addCity(City city) {
        citiesRef.document(city.getName())
                .set(city)
                .addOnSuccessListener(aVoid -> android.util.Log.d("Firestore", "City successfully added!"))
                .addOnFailureListener(e -> android.util.Log.w("Firestore", "Error adding document", e));
    }

    // Custom method to delete a city from the cloud database
    private void deleteCityFromFirestore(City city) {
        citiesRef.document(city.getName())
                .delete()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        android.util.Log.d("Firestore", "City successfully deleted!");
                    }
                })
                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        android.util.Log.w("Firestore", "Error deleting city", e);
                    }
                });
    }

    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}