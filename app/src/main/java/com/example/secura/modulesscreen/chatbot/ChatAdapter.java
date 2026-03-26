package com.example.secura.modulesscreen.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secura.R;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessageList;

    public ChatAdapter(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessageList.get(position).getSenderType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.TYPE_CHATBOT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_chatbot, parent, false);
            return new ChatbotMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessageList.get(position);
        if (holder.getItemViewType() == ChatMessage.TYPE_CHATBOT) {
            ((ChatbotMessageViewHolder) holder).bind(message);
        } else {
            ((UserMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    static class ChatbotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView chatbotMessageText;

        ChatbotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            chatbotMessageText = itemView.findViewById(R.id.chatbot_message_text);
        }

        void bind(ChatMessage message) {
            chatbotMessageText.setText(message.getText());
        }
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView userMessageText;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessageText = itemView.findViewById(R.id.user_message_text);
        }

        void bind(ChatMessage message) {
            userMessageText.setText(message.getText());
        }
    }
}