package com.example.teamchat.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.teamchat.Dao.ContactDB;
import com.example.teamchat.Dao.ContactDao;
import com.example.teamchat.api.ContactApi;
import com.example.teamchat.entities.contacts.Contact;
import com.example.teamchat.entities.contacts.ContactNoMsg;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ContactRepository {
    private ContactDao contactDao;
    private ContactApi contactApi;
    private ContactListData contactListData;

    private Context context;

    private ContactDB db;
    private static ContactRepository contactRepository;
    private String authorizationHeader;

    private ContactRepository(Context context, String authorizationHeader) {
        this.db = ContactDB.getInstance(context);
        this.contactDao = db.contactDao();
        this.contactListData = new ContactListData();
        this.contactApi = new ContactApi(context, authorizationHeader);
        this.authorizationHeader = authorizationHeader;
        this.context = context;

    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setAuthorizationHeader(String authorizationHeader){
        this.authorizationHeader = authorizationHeader;
    }

    public static ContactRepository getRepository(Context context, String authorizationHeader) {
        if (contactRepository == null) {
            contactRepository = new ContactRepository(context, authorizationHeader);
        } else {
            contactRepository.setContext(context);
            contactRepository.setAuthorizationHeader(authorizationHeader);
        }
        return contactRepository;
    }

    public LiveData<List<Contact>> getAll() {
        CompletableFuture.supplyAsync(() -> contactDao.index())
                .thenAccept(contactList -> contactListData.postValue(contactList));
        return contactListData;
    }


    public void add(final String contact) {
        CompletableFuture<ContactNoMsg> future = contactApi.onAddContact(contact);

        future.thenAccept(contactNoMsg -> {
            int id = contactNoMsg.getId();
            Contact newContact = new Contact(id ,contactNoMsg.getUserNoPass(), null);
            new Thread(() -> {
                contactDao.insert(newContact);
                List<Contact> contacts = contactDao.index();
                contactListData.postValue(contacts);
            }).start();
        });
    }


//    public Contact getContact(int id){
//        return contactApi.onGetContactDetails(id);
//    }

//    public void delete(final Contact contact) {
//        contactApi.onDeleteContact();
//    }

    class ContactListData extends MutableLiveData<List<Contact>> {
        public ContactListData() {
            super();
            new Thread(() -> {
                List<Contact> contacts = contactDao.index();
                contactListData.postValue(contacts);
            }).start();

        }

        @Override
        protected void onActive() {
            super.onActive();
        }
    }
}

