package com.levisstrauss.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import utils.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private Button createAcctButton;

    private AutoCompleteTextView emailAddress;
    private EditText password;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ID = "userId";
    private String username;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.login_progress);


        firebaseAuth = FirebaseAuth.getInstance();
        emailAddress = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.email_sign_in_button);
        createAcctButton = findViewById(R.id.create_acct_button_login);

        if (savedInstanceState != null) {
            // Restore state
            username = savedInstanceState.getString(KEY_USERNAME);
            userId = savedInstanceState.getString(KEY_USER_ID);
        }

        createAcctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginEmailPasswordUser(emailAddress.getText().toString().trim(),
                        password.getText().toString().trim());

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save state
        outState.putString(KEY_USERNAME, username);
        outState.putString(KEY_USER_ID, userId);
    }

    private void loginEmailPasswordUser(String email, String pwd) {

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(pwd)) {
            // Create a new thread
            Thread myThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    firebaseAuth.signInWithEmailAndPassword(email, pwd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    assert user != null;
                                    final String currentUserId = user.getUid();

                                    collectionReference
                                            .whereEqualTo("userId", currentUserId)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                                    @Nullable FirebaseFirestoreException e) {
                                                    if (e != null) {
                                                    }
                                                    assert queryDocumentSnapshots != null;
                                                    if (!queryDocumentSnapshots.isEmpty()) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                            username = snapshot.getString("username");
                                                            userId = snapshot.getString("userId");
                                                            //Go to ListActivity
                                                            Intent intent = new Intent(LoginActivity.this, PostJournalActivity.class);
                                                            intent.putExtra("username", username);
                                                            intent.putExtra("userId", userId);
                                                            startActivity(intent);
                                                        }

                                                    }

                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                }
            });
            // Start the thread
            myThread.start();

        }else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(LoginActivity.this,
                            "Please enter email and password",
                            Toast.LENGTH_LONG).show();
        }
    }
}
