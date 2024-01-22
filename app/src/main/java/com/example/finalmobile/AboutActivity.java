package com.example.finalmobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.finalmobile.Visible.Utilities;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AboutActivity extends AppCompatActivity {

    byte[] dataPhoto;
    LinearLayout labelLineer, labelPhotolineer;
    String loggedIn, name;
    ArrayList<String> selectedLabels = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageView imageView;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Button btnOpenDrawer;
    private FrameLayout addPhoto, addLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Intent intent = getIntent();
        if (intent != null) {
            loggedIn = intent.getStringExtra("userEmail");
            name = intent.getStringExtra("name");
        } else {
            Log.e("IntentError", "Intent is null");
        }

        displayLabels();
        displayphotoLabels();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnOpenDrawer = findViewById(R.id.btnOpenDrawer);
        addPhoto = findViewById(R.id.addphotolayout);
        addLabel = findViewById(R.id.addlabellayout);
        imageView = findViewById(R.id.imageView4);
        WebView webView = findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.linkedin.com/in/eren-%C3%B6zt%C3%BCrk-623982263/");

        Button camera = findViewById(R.id.camera_btn);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        Button save =findViewById(R.id.photo_save_btn);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String randomID = generateRandomID(10);
                StorageReference storageReference = storageRef.child(randomID+".jpg");
                UploadTask uploadTask = storageReference.putBytes(dataPhoto);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                            }
                        });
                    }
                });

                Map<String, Object> galleryData = new HashMap<>();
                galleryData.put("label", selectedLabels);
                galleryData.put("email", loggedIn);
                galleryData.put("name", name);
                galleryData.put("photo", randomID);

                CollectionReference labelsCollectionRef = db.collection("gallery");

                labelsCollectionRef.add(galleryData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("Firestore", "Label added successfully with ID: " + documentReference.getId());
                                Toast.makeText(AboutActivity.this, "Succesfully Registered", Toast.LENGTH_SHORT).show();
                                displayLabels();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Firestore", "Error adding label", e);
                            }
                        });
            }
        });

        Button labelAddBTN =findViewById(R.id.add_label_btn);
        labelAddBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String labelAdd = ((EditText) findViewById(R.id.label_name)).getText().toString();
                String Addlabeldescription = ((EditText) findViewById(R.id.label_description)).getText().toString();
                Map<String, Object> labelData = new HashMap<>();
                labelData.put("description", !Addlabeldescription.isEmpty() ? Addlabeldescription : "labels");
                labelData.put("label", labelAdd);
                labelData.put("email", loggedIn);

                CollectionReference labelsCollectionRef = db.collection("labels");

                labelsCollectionRef.add(labelData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("Firestore", "Label added successfully with ID: " + documentReference.getId());
                                Toast.makeText(AboutActivity.this, "Successfully saved.", Toast.LENGTH_SHORT).show();
                                displayLabels();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Firestore", "Error adding label", e);
                            }
                        });
            }

        });

        addPhoto.setVisibility(View.INVISIBLE);
        addLabel.setVisibility(View.INVISIBLE);

        Utilities.init(this, addPhoto, addLabel);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        btnOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawer.isDrawerOpen(navigationView)) {
                    drawer.openDrawer(navigationView);
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Utilities.handleNavSelected(menuItem, loggedIn);
                menuItem.setChecked(true);
                displayLabels();
                displayphotoLabels();
                drawer.closeDrawers();
                return true;
            }
        });
    }

    private void displayLabels() {
        labelLineer =findViewById(R.id.labelslineer);
        labelLineer.removeAllViews();
        db.collection("labels")
                .whereEqualTo("email", loggedIn)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String labelDescription = document.getString("description");
                            String labelName = document.getString("label");
                            ConstraintLayout labelLayout = new ConstraintLayout(this);
                            TextView textView = new TextView(this);
                            textView.setId(View.generateViewId());
                            textView.setText("Label: " + labelName + ", Description: " + labelDescription);
                            Button deleteButton = new Button(this);
                            deleteButton.setId(View.generateViewId());
                            deleteButton.setText("Delete");
                            deleteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteLabelFonk(document.getId());
                                    labelLineer.removeView(labelLayout);
                                }
                            });

                            labelLayout.addView(textView);
                            labelLayout.addView(deleteButton);

                            labelLineer.addView(labelLayout);

                            ConstraintSet constraintSet = new ConstraintSet();
                            constraintSet.clone(labelLayout);

                            constraintSet.connect(textView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                            constraintSet.connect(textView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);

                            constraintSet.connect(deleteButton.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                            constraintSet.connect(deleteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);


                            constraintSet.applyTo(labelLayout);
                        }
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }
    private void deleteLabelFonk(String documentId) {
        db.collection("labels")
                .document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Label deleted successfully");
                        Toast.makeText(AboutActivity.this, "Label deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error deleting label", e);
                        Toast.makeText(AboutActivity.this, "Error deleting label", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static String generateRandomID(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomID = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            randomID.append(characters.charAt(index));
        }
        return randomID.toString();
    }
    private void displayphotoLabels() {
        labelPhotolineer =findViewById(R.id.label_show);
        labelPhotolineer.removeAllViews();
        db.collection("labels")
                .whereEqualTo("email", loggedIn)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String labelDescription = document.getString("description");
                            String labelName = document.getString("label");

                            ConstraintLayout labelLayout = new ConstraintLayout(this);

                            TextView textView = new TextView(this);
                            textView.setId(View.generateViewId());
                            textView.setText("Label: " + labelName + ", Description: " + labelDescription);

                            CheckBox checkBox = new CheckBox(this);
                            checkBox.setId(View.generateViewId());

                            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        selectedLabels.add(labelName);
                                    }
                                }
                            });

                            labelLayout.addView(textView);
                            labelLayout.addView(checkBox);

                            labelPhotolineer.addView(labelLayout);

                            ConstraintSet constraintSet = new ConstraintSet();
                            constraintSet.clone(labelLayout);

                            constraintSet.connect(textView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                            constraintSet.connect(textView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);

                            constraintSet.connect(checkBox.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                            constraintSet.connect(checkBox.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);


                            constraintSet.applyTo(labelLayout);
                        }
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 33);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 33 && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(imageBitmap);

            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            dataPhoto = byteArrayOutputStream.toByteArray();
        }
    }
}