package com.example.finalmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {
    private FrameLayout loginPage, signupPage, splashPage;
    String logEmail, logPassword;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        loginPage = findViewById(R.id.login_frame);
        signupPage = findViewById(R.id.signup_frame);
        splashPage = findViewById(R.id.splash_frame);

        splashPage.setVisibility(View.VISIBLE);

        loginPage.setVisibility(View.INVISIBLE);
        signupPage.setVisibility(View.INVISIBLE);

        Button login = findViewById(R.id.login_btn);
        Button signup = findViewById(R.id.signup_btn);

        logEmail = ((EditText) findViewById(R.id.log_email_input)).getText().toString();
        logPassword = ((EditText) findViewById(R.id.log_pass_input)).getText().toString();
        Button  loginBtn = findViewById(R.id.log_log_btn);
        Button loginsignUp = findViewById(R.id.log_sign_btn);

        Button singUpReg = findViewById(R.id.sign__sign_btn);
        Button signUpLog = findViewById(R.id.sign_login_btn);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPage.setVisibility(View.VISIBLE);
                splashPage.setVisibility(View.INVISIBLE);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupPage.setVisibility(View.VISIBLE);
                splashPage.setVisibility(View.INVISIBLE);
            }
        });

        loginsignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupPage.setVisibility(View.VISIBLE);
                loginPage.setVisibility(View.INVISIBLE);
            }
        });

        signUpLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPage.setVisibility(View.VISIBLE);
                signupPage.setVisibility(View.INVISIBLE);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loginfunct();
            }
        });

        singUpReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Singupfunct();
            }
        });
    }
    private void Loginfunct() {
        String logEmail = ((EditText) findViewById(R.id.log_email_input)).getText().toString();
        String logPassword = ((EditText) findViewById(R.id.log_pass_input)).getText().toString();

        if (logEmail.isEmpty() || logPassword.isEmpty()) {
            Toast.makeText(this, "e-mail and password fields must not be empty to complete the login process.", Toast.LENGTH_SHORT).show();
        } else {
            String message = "E-mail: " + logEmail + "\npassword: " + logPassword;
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            CollectionReference usersRef = db.collection("users");
            Query query = usersRef.whereEqualTo("email", logEmail);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String storedPassword = document.getString("password");
                        if (storedPassword != null && storedPassword.equals(logPassword)) {
                            Toast.makeText(this, "Succesfully Entered.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, AboutActivity.class);
                            intent.putExtra("userEmail", document.getString("email"));
                            intent.putExtra("name", document.getString("name"));
                            this.startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Login unsuccessful. Please verify your password.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "Firestore query unsuccessfull.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
    private void Singupfunct() {
        User newUser = new User();
        newUser.setName(((EditText) findViewById(R.id.sign_name_input)).getText().toString());
        newUser.setSurName(((EditText) findViewById(R.id.sign_surname_input)).getText().toString());
        newUser.setEmail(((EditText) findViewById(R.id.sign_email_input)).getText().toString());
        newUser.setPassword(((EditText) findViewById(R.id.sign_password_input)).getText().toString());

        if (newUser.getName().isEmpty() || newUser.getSurName().isEmpty()|| newUser.getEmail().isEmpty() || newUser.getPassword().isEmpty()) {
            Toast.makeText(this, "fields must not be empty to complete the sign up process.", Toast.LENGTH_SHORT).show();
        } else {
            DocumentReference userDocumentRef = db.collection("users").document(newUser.getEmail());

            userDocumentRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Firestore", "User already exists. Showing warning.");
                        Toast.makeText(this, "User already exists.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Firestore", "User does not exist. Performing insert.");
                        db.collection("users")
                                .document(newUser.getEmail())
                                .set(newUser)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Firestore", "User added successfully");
                                        Toast.makeText(MainActivity.this, "successfully registered.", Toast.LENGTH_SHORT).show();
                                        signupPage.setVisibility(View.INVISIBLE);
                                        loginPage.setVisibility(View.VISIBLE);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Firestore", "Error adding user", e);
                                    }
                                });
                    }
                } else {
                    Log.e("Firestore", "Error checking user existence", task.getException());
                }
            });
        }
    }

}
class User {
    private String name;
    private String surName;
    private String email;
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurName() {return surName; }
    public String setSurName(String surName) {return this.surName = surName; }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}