package com.example.teamchat.api;

import android.content.Context;

import com.example.teamchat.Dao.Settings.SettingsDB;
import com.example.teamchat.Dao.Settings.SettingsDao;
import com.example.teamchat.entities.SettingsEntity;
import com.example.teamchat.entities.messages.Message;
import com.example.teamchat.entities.messages.MessageString;
import com.example.teamchat.entities.user.UserNoPass;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatApi {
    private Retrofit retrofit;
    private ChatsApiService chatsApiService;
    private String authorizationToken;
    private int id;
    private SettingsDB settingsDB;
    private SettingsDao settingsDao;
    private List<SettingsEntity> settingsEntityList;

    public ChatApi(Context context, String authorizationToken, int id) {
        this.authorizationToken = authorizationToken;
        this.settingsDB = SettingsDB.getInstance(context);
        this.settingsDao = settingsDB.settingsDao();
        this.settingsEntityList = settingsDao.index();
        String url = this.settingsEntityList.get(0).getUrl();
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        chatsApiService = retrofit.create(ChatsApiService.class);
        this.id = id;
    }

    //the function gets username and return all the user details from the server
    public CompletableFuture<UserNoPass> onGetUserDetails(String username) {
        CompletableFuture<UserNoPass> completableFuture = new CompletableFuture<>();
        Call<UserNoPass> call = chatsApiService.getUserDetails(authorizationToken, username);
        call.enqueue(new Callback<UserNoPass>() {
            @Override
            public void onResponse(Call<UserNoPass> call, Response<UserNoPass> response) {
                if (response.isSuccessful()) {
                    UserNoPass user = response.body();
                    completableFuture.complete(user);
                } else {
                    // API request failed
                    completableFuture.completeExceptionally(new Exception("API request failed with status code: " + response.code()));
                }
            }
            @Override
            public void onFailure(Call<UserNoPass> call, Throwable t) {
                completableFuture.completeExceptionally(t);
            }
        });

        return completableFuture;
    }


    // the function sends to the server contact id and returns all the messages between the user and contact
    public CompletableFuture<List<Message>> onGetMessages() {
        CompletableFuture<List<Message>> completableFuture = new CompletableFuture<>();
        Call<List<Message>> call = chatsApiService.getMessages(authorizationToken, id);
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if(response.isSuccessful()){
                    List<Message> messages = response.body();
                    completableFuture.complete(messages);
                }else{
                    // API request failed
                    completableFuture.completeExceptionally(new Exception("API request failed"));
                }
            }
            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                // Handle network or other errors
                completableFuture.completeExceptionally(t);
            }
        });
        return completableFuture;
    }

    // the function get message string- and return the message format from the server
    public CompletableFuture<Message> onAddMessage(String message) {
        CompletableFuture<Message> completableFuture = new CompletableFuture<>();
        MessageString messageString = new MessageString(message);
        // Make the API call to add the message
        Call<Message> call = chatsApiService.addMessage(authorizationToken, id, messageString);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful()) {
                    // API request succeeded
                    Message addedMessage = response.body();
                    completableFuture.complete(addedMessage);
                } else {
                    // API request failed
                    completableFuture.completeExceptionally(new Exception("API request failed"));
                }
            }
            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                // Handle network or other errors
                completableFuture.completeExceptionally(t);
            }
        });

        return completableFuture;

    }
}

