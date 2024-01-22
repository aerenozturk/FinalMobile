package com.example.finalmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.finalmobile.Visible.Utilities;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {
    String loggedIn;
    private DrawerLayout drawer;
    private Button btnOpenDrawer;
    Spinner spinner;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private NavigationView navigationView;
    LinearLayout galleryShow;
    ArrayList<String> labelsList = new ArrayList<>();
    Set<String> uniqueLabelsSet = new HashSet<>();
    String[] labelsArray = uniqueLabelsSet.toArray(new String[0]);
    String selectedLabel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Intent intent = getIntent();
        if (intent != null) {
            loggedIn = intent.getStringExtra("userEmail");
        } else {
            Log.e("IntentError", "Intent is null");
        }
        drawer = findViewById(R.id.drawer_layout1);
        navigationView = findViewById(R.id.nav_view1);
        btnOpenDrawer = findViewById(R.id.btnOpenDrawer1);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Utilities.handleNavSelected(menuItem, loggedIn);

                menuItem.setChecked(true);

                drawer.closeDrawers();
                return true;
            }
        });

        btnOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawer.isDrawerOpen(navigationView)) {
                    drawer.openDrawer(navigationView);
                }
            }
        });

        displayLabels();
        SpinnerView();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
    }
    private void displayLabels() {
        galleryShow = findViewById(R.id.galeri_show);

        db.collection("gallery")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String user = document.getString("name");
                            String photoname = document.getString("photo");


                            String id = document.getId();

                            Object labelObject = document.get("label");

                            if (labelObject instanceof List<?>) {
                                List<?> labelList = (List<?>) labelObject;

                                for (Object item : labelList) {
                                    if (item instanceof String) {
                                        String label = (String) item;
                                        labelsList.add(label);
                                    } else {
                                        Log.e("Firestore", "a object is not a string in Label List: " + item);
                                        continue;
                                    }
                                }
                            } else {
                                Log.e("Firestore", "Label section is not a List: " + labelObject);
                                continue;
                            }

                            ConstraintLayout GalleryLayout = new ConstraintLayout(this);

                            TextView textViewuser = new TextView(this);
                            textViewuser.setId(View.generateViewId());
                            textViewuser.setText(user);

                            TextView textView = new TextView(this);
                            textView.setId(View.generateViewId());
                            textView.setText("Labels: \n" + labelsList);
                            labelsList.clear();


                            ImageView imageView = new ImageView(this);
                            imageView.setId(View.generateViewId());
                            String photoUrl = "https://firebasestorage.googleapis.com/v0/b/finalmobile-86d60.appspot.com/o/"+photoname +".jpg?alt=media";
                            Picasso.get().load(photoUrl).into(imageView);
                            int width = 500;
                            int height = 500;

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                            imageView.setLayoutParams(layoutParams);

                            GalleryLayout.addView(imageView);
                            GalleryLayout.addView(textViewuser);
                            GalleryLayout.addView(textView);

                            galleryShow.addView(GalleryLayout);

                            ConstraintSet constraintSet = new ConstraintSet();
                            constraintSet.clone(GalleryLayout);

                            constraintSet.connect(imageView.getId(), ConstraintSet.TOP, GalleryLayout.getId(), ConstraintSet.TOP);
                            constraintSet.connect(imageView.getId(), ConstraintSet.START, GalleryLayout.getId(), ConstraintSet.START);
                            constraintSet.connect(imageView.getId(), ConstraintSet.END, GalleryLayout.getId(), ConstraintSet.END);

                            constraintSet.connect(textViewuser.getId(), ConstraintSet.TOP, imageView.getId(), ConstraintSet.TOP);
                            constraintSet.connect(textViewuser.getId(), ConstraintSet.START, imageView.getId(), ConstraintSet.END);

                            constraintSet.connect(textView.getId(), ConstraintSet.TOP, textViewuser.getId(), ConstraintSet.BOTTOM);
                            constraintSet.connect(textView.getId(), ConstraintSet.START, imageView.getId(), ConstraintSet.END);


                            constraintSet.applyTo(GalleryLayout);

                            Space space = new Space(this);
                            space.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    20));
                            galleryShow.addView(space);

                        }

                    } else {
                        Log.e("Firestore", "Document's don't exist: ", task.getException());
                    }
                });
    }

    private void refreshUI() {
        galleryShow.removeAllViews();
        displayLabels();
    }

    private void SpinnerView() {
        db.collection("gallery")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Set<String> uniqueLabelsSet = new HashSet<>();
                        uniqueLabelsSet.add("all");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Object labelObject = document.get("label");
                            if (labelObject instanceof List<?>) {
                                List<?> labelList = (List<?>) labelObject;
                                for (Object item : labelList) {
                                    if (item instanceof String) {
                                        String label = (String) item;
                                        uniqueLabelsSet.add(label);
                                    } else {
                                        Log.e("Firestore", "an object isn't a string in label list: " + item);
                                        continue;
                                    }
                                }
                            } else {
                                Log.e("Firestore", "Label section is not a List: " + labelObject);
                                continue;
                            }
                        }

                        String[] uniqueLabelsArray = uniqueLabelsSet.toArray(new String[0]);

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(GalleryActivity.this, android.R.layout.simple_spinner_item, uniqueLabelsArray);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinner = findViewById(R.id.spinner);
                        spinner.setAdapter(adapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                selectedLabel = (String) parentView.getItemAtPosition(position);
                                Toast.makeText(GalleryActivity.this, "Selected Label: " + selectedLabel, Toast.LENGTH_SHORT).show();
                                if ("all".equals(selectedLabel)) {
                                    galleryShow.removeAllViews();
                                    displayLabels();
                                }
                                galleryShow.removeAllViews();
                                db.collection("gallery")
                                        .whereArrayContains("label", selectedLabel)
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                    String user = document.getString("name");
                                                    String photoname = document.getString("photo");

                                                    Object labelObject = document.get("label");

                                                    if (labelObject instanceof List<?>) {
                                                        List<?> labelList = (List<?>) labelObject;

                                                        for (Object item : labelList) {
                                                            if (item instanceof String) {
                                                                String label = (String) item;
                                                                labelsList.add(label);
                                                            } else {
                                                                Log.e("Firestore", "an object isn't a string in label list: " + item);
                                                                continue;
                                                            }
                                                        }
                                                    } else {
                                                        Log.e("Firestore", "Label section isn't a list: " + labelObject);
                                                        continue;
                                                    }


                                                    ConstraintLayout GaleriLayout = new ConstraintLayout(GalleryActivity.this);

                                                    TextView textViewuser = new TextView(GalleryActivity.this);
                                                    textViewuser.setId(View.generateViewId());
                                                    textViewuser.setText(user);
                                                    TextView textView = new TextView(GalleryActivity.this);
                                                    textView.setId(View.generateViewId());
                                                    textView.setText("Labels: \n" + labelsList);
                                                    labelsList.clear();

                                                    ImageView imageView = new ImageView(GalleryActivity.this);
                                                    imageView.setId(View.generateViewId());
                                                    String photoUrl = "https://firebasestorage.googleapis.com/v0/b/finalmobile-86d60.appspot.com/o/"+photoname +".jpg?alt=media";
                                                    Picasso.get().load(photoUrl).into(imageView);
                                                    int width =500;
                                                    int height =500;

                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                                                    imageView.setLayoutParams(layoutParams);


                                                    GaleriLayout.addView(imageView);
                                                    GaleriLayout.addView(textViewuser);
                                                    GaleriLayout.addView(textView);

                                                    galleryShow.addView(GaleriLayout);

                                                    ConstraintSet constraintSet = new ConstraintSet();
                                                    constraintSet.clone(GaleriLayout);

                                                    constraintSet.connect(imageView.getId(), ConstraintSet.TOP, GaleriLayout.getId(), ConstraintSet.TOP);
                                                    constraintSet.connect(imageView.getId(), ConstraintSet.START, GaleriLayout.getId(), ConstraintSet.START);
                                                    constraintSet.connect(imageView.getId(), ConstraintSet.END, GaleriLayout.getId(), ConstraintSet.END);

                                                    constraintSet.connect(textViewuser.getId(), ConstraintSet.TOP, imageView.getId(), ConstraintSet.TOP);
                                                    constraintSet.connect(textViewuser.getId(), ConstraintSet.START, imageView.getId(), ConstraintSet.END);

                                                    constraintSet.connect(textView.getId(), ConstraintSet.TOP, textViewuser.getId(), ConstraintSet.BOTTOM);
                                                    constraintSet.connect(textView.getId(), ConstraintSet.START, imageView.getId(), ConstraintSet.END);


                                                    constraintSet.applyTo(GaleriLayout);

                                                    Space space = new Space(GalleryActivity.this);
                                                    space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 20));
                                                    galleryShow.addView(space);
                                                }

                                            } else {
                                                Log.e("Firestore", "Isn't successfull: ", task.getException());
                                            }
                                        });


                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {

                            }
                        });
                    } else {
                        Log.e("Firestore", "Isn't successfull: ", task.getException());
                    }
                });
    }
}