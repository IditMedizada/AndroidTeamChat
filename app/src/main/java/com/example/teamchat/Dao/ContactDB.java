package com.example.teamchat.Dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.teamchat.entities.contacts.Contact;

@Database(entities = {Contact.class}, version = 6)
public abstract class ContactDB extends RoomDatabase {
    private static final String DATABASE_NAME = "contact_db";
    private static ContactDB instance;

    public static synchronized ContactDB getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), ContactDB.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract ContactDao contactDao();
}
