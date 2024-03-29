package com.example.teamchat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamchat.R;
import com.example.teamchat.entities.messages.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENDER = 0;
    private static final int VIEW_TYPE_RECEIVER = 1;
    private final LayoutInflater mInflater;
    private List<Message> messages;
    private String me;
    private Bitmap imContact;
    private Bitmap imMy;


    public ChatListAdapter(Context context, String me,Bitmap imContact,Bitmap imMy) {
        mInflater = LayoutInflater.from(context);
        messages = new ArrayList<>();
        this.me = me;
        this.imContact = imContact;
        this.imMy = imMy;
    }

    // get the type of the message- receiver/sender
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSender().getUsername().equals(me)) {
            return VIEW_TYPE_SENDER;
        } else {
            return VIEW_TYPE_RECEIVER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        if (viewType == VIEW_TYPE_SENDER) {
            // move to SentMessageViewHolder. set the design as send message
            itemView = inflater.inflate(R.layout.item_send_message, parent, false);
            return new SentMessageViewHolder(itemView,imMy);
        }
        // move to ReceivedMessageViewHolder- set the design as receiver message
        itemView = inflater.inflate(R.layout.item_receive_message, parent, false);
        return new ReceivedMessageViewHolder(itemView,imContact);
    }

    // bing the message
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (messages != null) {
            final Message message = messages.get(position);
            if (holder instanceof SentMessageViewHolder) {
                ((SentMessageViewHolder) holder).bind(message);
            } else if (holder instanceof ReceivedMessageViewHolder) {
                ((ReceivedMessageViewHolder) holder).bind(message);
            }
        }

    }


    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        } else {
            return 0;
        }
    }

    public void setMessages(List<Message> s) {
        messages = s;
        notifyDataSetChanged();
    }

    private static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView msg;
        private final ImageView profilePic;

        public SentMessageViewHolder(@NonNull View itemView,Bitmap imMy) {
            super(itemView);
            msg = itemView.findViewById(R.id.text_send_message);
            profilePic = itemView.findViewById(R.id.image_send_message);
            profilePic.setImageBitmap(imMy);
        }

        void bind(Message message) {
            if (message.getContent() != null) {
                msg.setText(message.getContent());
            } else {
                msg.setText("");
            }
        }
    }

    private static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView msg;
        private final ImageView profilePic;

        public ReceivedMessageViewHolder(@NonNull View itemView, Bitmap imContact) {
            super(itemView);
            msg = itemView.findViewById(R.id.text_receive_message);
            profilePic = itemView.findViewById(R.id.image_receive_message);
            profilePic.setImageBitmap(imContact);
        }

        void bind(Message message) {
            if (message.getContent() != null) {
                msg.setText(message.getContent());
            } else {
                msg.setText("");
            }
        }
    }


}