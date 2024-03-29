package com.example.teamchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.teamchat.Dao.Chat.ChatDB;
import com.example.teamchat.Dao.ContactDB;
import com.example.teamchat.Dao.Settings.SettingsDB;
import com.example.teamchat.Dao.Settings.SettingsDao;
import com.example.teamchat.api.userApi;
import com.example.teamchat.chats.ContactList;
import com.example.teamchat.entities.SettingsEntity;
import com.example.teamchat.entities.user.UserForLogin;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    public static Context context;
    private SettingsDB settingsDB;
    private SettingsDao settingsDao;
    private String firstLogin;
    private String etName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        //setting DB
        this.settingsDB = SettingsDB.getInstance(getApplicationContext());
        this.settingsDao = settingsDB.settingsDao();


        if (firstLogin == null) {
            //check if room empty (happens only after first connection to the app)
            CompletableFuture.supplyAsync(() -> settingsDao.index())
                    .thenAccept(settingsEntities -> {
                        //if the settings DB is empty- enter default url and light mode
                        if (settingsEntities.size() == 0) {
                            SettingsEntity settingsEntity = new SettingsEntity("http://10.0.2.2:5000/api/", false);
                            settingsDao.insert(settingsEntity);
                        } else {
                            //if the settings not empty - change the mode according to DB
                            if (settingsEntities.get(0).getNightMode()) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            }
                        }
                    });
        }

        firstLogin = "";
        // move to setting activity
        ImageButton settingsBtn = findViewById(R.id.settingsBtnInLogin);
        settingsBtn.setOnClickListener(v -> {
            Intent settingIntent = new Intent(this, Settings.class);
            startActivity(settingIntent);
        });

        //login
        Button btnLogin = findViewById(R.id.LoginButton);
        btnLogin.setOnClickListener(v -> {
            Login();
        });


    }

    @SuppressLint("SetTextI18n")
    private void Login() {
        TextView errorTextView = findViewById(R.id.errorTextView);
        //extract the name and password from the edit texts
        EditText usernameEditText = findViewById(R.id.usernameEditTextInLogin);
        EditText passwordEditText = findViewById(R.id.PasswordEditTextInLogin);
        String edName = usernameEditText.getText().toString();
        String edPassword = passwordEditText.getText().toString();
        this.etName = edName;
        // create object for login
        UserForLogin user = new UserForLogin(edName, edPassword);
        userApi userApi = new userApi(context);
        //Retrieve the Firebase token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fireBaseToken = task.getResult();
                        CompletableFuture<ResponseBody> loginFuture = userApi.onLogin(fireBaseToken, user);
                        loginFuture.thenAccept(responseBody -> {
                            try {
                                String response = responseBody.string();
                                String authorizationHeader = "bearer " + response;
                                //login successfully
                                //clear the DB if it is a new user login
                                List<SettingsEntity> settingsEntities = CompletableFuture.supplyAsync(() -> settingsDao.index())
                                        .join();
                                if (settingsEntities.get(0).getUserConnected() == null) {
                                    // if it is the first connection - change only the UserConnected and AuthorizationHeader
                                    settingsEntities.get(0).setUserConnected(edName);
                                    settingsEntities.get(0).setAuthorizationHeader(authorizationHeader);
                                    settingsDao.update(settingsEntities.get(0));
                                } else if (!(settingsEntities.get(0).getUserConnected().equals(edName))) {
                                    // if we connect from another user then the last time-delete the DB-setting(default), contacts, messages
                                    ContactDB.deleteDatabase(getApplicationContext());
                                    ChatDB.deleteDatabase(getApplicationContext());
                                    //update the new user connection
                                    settingsEntities.get(0).setUserConnected(edName);
                                    settingsEntities.get(0).setAuthorizationHeader(authorizationHeader);
                                    settingsDao.update(settingsEntities.get(0));
                                }

                                // move to the contactList activity
                                Intent i = new Intent(this, ContactList.class);
                                i.putExtra("me", edName);
                                i.putExtra("token", authorizationHeader);
                                startActivity(i);
                                // token variable now contains the token string
                            } catch (Exception e) {
                                e.printStackTrace();
                                errorTextView.setText("Invalid username or password");
                                errorTextView.setVisibility(View.VISIBLE);
                            }
                        }).exceptionally(ex -> {
                            errorTextView.setText("Invalid username or password");
                            errorTextView.setVisibility(View.VISIBLE);
                            return null;
                        });
                    } else {
                        // Handle the failure to retrieve the Firebase token
                        Log.e("Firebase", "Failed to retrieve Firebase token: " + task.getException());
                    }
                });
    }

    public void navigateToRegistrationScreen(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }

}