package com.example.secura.assistantchatbotscreen;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secura.R;
import com.example.secura.modulesscreen.chatbot.ChatAdapter;
import com.example.secura.modulesscreen.chatbot.ChatMessage;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AssistantChatbotFragment extends Fragment {

    private static final int TYPING_DELAY = 1500;
    private static final int TYPING_SPEED = 50;

    private RecyclerView chatRecyclerView;
    private TextInputEditText messageInput;
    private ImageButton sendButton;
    private ImageButton clearChatButton;
    private ChipGroup suggestionChipGroup;

    private ChatAdapter chatAdapter;
    private final List<ChatMessage> chatMessageList = new ArrayList<>();

    // Hardcoded Q&A pairs
    private final Map<String, String> qaMap = new LinkedHashMap<>();

    public AssistantChatbotFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assistant_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatRecyclerView     = view.findViewById(R.id.chat_recycler_view);
        messageInput         = view.findViewById(R.id.message_input_edit_text);
        sendButton           = view.findViewById(R.id.send_button);
        clearChatButton      = view.findViewById(R.id.clear_chat_button);
        suggestionChipGroup  = view.findViewById(R.id.suggestion_chip_group);

        chatAdapter = new ChatAdapter(chatMessageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        loadHardcodedQA();    // load all questions & answers directly in code
        greetUserWithTypingAnimation();

        setupListeners();
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        clearChatButton.setOnClickListener(v -> {
            clearChat();
            greetUserWithTypingAnimation();
        });

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { showSuggestions(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ====== Hardcoded Questions & Answers ======
    private void loadHardcodedQA() {
        qaMap.put("How can I use the Malware Scanner?",
                "First, turn on the switch and grant the required permissions. Once permissions are given, the Malware Scanner will scan your apps and files for malware.");

        qaMap.put("Do I need to give permissions to run Malware Scanner?",
                "Yes, the scanner needs permission to check your apps and files for malware. Without permissions, it cannot run.");

        qaMap.put("What happens after scanning is completed?",
                "The Malware Scanner will show you the detected malware, and you can choose to delete it from your device.");

        qaMap.put("Can I delete malware directly from the scanner?",
                "Yes, after scanning, you can delete any detected malware directly from the Malware Scanner.");

        qaMap.put("How can I use the Network Scanner?",
                "First, turn on the switch and grant the required permissions. After that, the Network Scanner will scan your connected Wi-Fi.");

        qaMap.put("What information does the Network Scanner show?",
                "It shows details about your connected Wi-Fi and checks whether it is safe or not.");

        qaMap.put("Can the Network Scanner tell if my Wi-Fi is safe?",
                "Yes, after scanning, it tells you if your connected Wi-Fi is safe or unsafe.");

        qaMap.put("Do I need to give permissions for the Network Scanner to work?",
                "Yes, permissions are required so the Network Scanner can scan and analyze your connected Wi-Fi.");

        qaMap.put("How can I use the Call & SMS Scanner?",
                "First, turn on the switch and grant the required permissions. After that, the scanner will check your call and SMS logs.");

        qaMap.put("What does the Call & SMS Scanner do?",
                "It scans your call and SMS logs for spam numbers and messages, then filters them out.");

        qaMap.put("What happens after scanning?",
                "The scanner shows the spam calls and SMS it detected so you can view them separately.");

        qaMap.put("Do I need to give permissions for this feature to work?",
                "Yes, permissions are required so the scanner can access your call and SMS logs to detect spam.");

        qaMap.put("How do I set up App Lock?",
                "First, turn on the switch and grant the required permissions. Then, set a PIN and choose the app you want to lock from the list.");

        qaMap.put("Can I lock any app on my phone?",
                "Yes, you can select any app from the list and lock it with your PIN.");

        qaMap.put("How do I unlock a locked app?",
                "Simply select the locked app in the list and remove the lock. The process is the same as locking.");

        qaMap.put("Do I need to set a PIN every time?",
                "No, you only need to set the PIN once when you first set up App Lock. After that, you can use the same PIN to lock or unlock apps.");

        qaMap.put("How can I hide my photos or videos?",
                "Open the Gallery Vault, select the photos or videos you want to hide, and they will be hidden from your main gallery.");

        qaMap.put("Where can I see my hidden photos and videos?",
                "You can view them inside the Gallery Vault’s built-in viewer.");

        qaMap.put("Can I unhide my photos or videos?",
                "Yes, just select the media inside the vault and choose Unhide. The files will be restored to their original location.");

        qaMap.put("Will my hidden photos appear in the phone’s gallery?",
                "No, once hidden, they will only be visible inside the Gallery Vault until you unhide them.");

        qaMap.put("How does the Intruder Selfie feature work?",
                "First, turn on the switch and grant the required permissions. If someone enters the wrong phone lock password more than 3 times, the feature will capture their selfie.");

        qaMap.put("Where can I see the captured selfies of intruders?",
                "You can view them in the View Intruder Selfie section. It shows the person’s photo along with the date and time of the attempt.");

        qaMap.put("Can I delete an intruder selfie?",
                "Yes, you have the option to delete any captured selfie directly from the Intruder Selfie section.");

        qaMap.put("Will the intruder know their selfie was taken?",
                "No, the feature works silently in the background without notifying the intruder.");

        qaMap.put("How do I start using Secure Notepad?",
                "The first time you open Secure Notepad, you’ll be asked to set a PIN. After that, you can start creating notes.");

        qaMap.put("Is there a limit on how many notes I can create?",
                "No, you can create as many notes as you want.");

        qaMap.put("How do I access my notes later?",
                "Open Secure Notepad, enter your PIN, and you’ll see all the notes you created.");

        qaMap.put("Can I delete a note?",
                "Yes, you can delete any note you no longer need.");

        qaMap.put("Will it always ask for a PIN?",
                "Yes, every time you reopen Secure Notepad, you’ll need to enter your PIN to access your notes.");

        qaMap.put("How do I use the Security Monitor?",
                "First, turn on the switch and grant the required permissions. Then, click the refresh button to scan your device.");

        qaMap.put("What does the Security Monitor scan for?",
                "It scans for malicious apps, apps trying to access permissions in the background, and apps behaving abnormally or running without your knowledge.");

        qaMap.put("Can I take action on the detected apps?",
                "The Security Monitor shows you the results of the scan and the apps found. You can then decide what to do with those apps.");

        qaMap.put("Do I need to scan manually every time?",
                "Yes, you need to click the refresh button whenever you want to check for abnormal or malicious apps.");

        qaMap.put("What can I do with App Management?",
                "App Management lets you view all installed apps, manage their permissions, check app details, clear data, and monitor background activity.");

        qaMap.put("How can I see all the apps installed on my device?",
                "Go to App Management → you’ll see the full list of installed applications.");

        qaMap.put("Can I manage app permissions from here?",
                "Yes, under Permission Management, you can allow or deny specific permissions for any installed app.");

        qaMap.put("What kind of app information is shown?",
                "You can see detailed app info such as version, size, and permissions. You can also uninstall or force stop apps directly.");

        qaMap.put("Can I clear cache or data of an app?",
                "Yes, under Data Management, you can clear cache or clear all data for any installed app.");

        qaMap.put("Does App Management show background usage of apps?",
                "Yes, it helps monitor app usage and background processes so you can see which apps are running unnecessarily.");

        qaMap.put("How do I use the Battery Monitor?",
                "First, turn on the switch and grant the required permissions. After that, the Battery Monitor will show your battery status.");

        qaMap.put("What information does the Battery Monitor display?",
                "It shows the current battery percentage, charging animations, and basic battery details.");

        qaMap.put("Does the animation change while charging?",
                "Yes, the Battery Monitor displays glowing or different animations while your device is charging.");

        qaMap.put("Can I enable power saver from here?",
                "Yes, there’s a Power Saver button in the Battery Monitor. When you click it, it directs you to the system’s battery saver option.");

        qaMap.put("Do I need to give permissions for Battery Monitor to work?",
                "Yes, required permissions must be granted for it to access and display battery details.");

        qaMap.put("What is the Secure Browser?",
                "The Secure Browser works like a firewall. It blocks unsafe links such as HTTP websites to keep your browsing secure.");

        qaMap.put("Can I add my own links to block?",
                "Yes, you can manually add malicious or unwanted links, and the browser will block them.");

        qaMap.put("How can I see which links were blocked?",
                "The Secure Browser has a section where you can view all blocked links.");

        qaMap.put("Can I unblock a link later?",
                "Yes, you can unblock any previously blocked link whenever you want.");

        qaMap.put("Does it block only HTTP websites?",
                "By default, it blocks insecure links like HTTP, but you can also add other links to block.");

        qaMap.put("What is the Cyber Chatbot?",
                "The Cyber Chatbot is a smart assistant that helps you understand cybersecurity terms in a simple way.");

        qaMap.put("How do I use the Cyber Chatbot?",
                "Just type any cybersecurity term, and the chatbot will suggest related terms. After you send the term, it provides a short description.");

        qaMap.put("How many terms does it know?",
                "The Cyber Chatbot has 200 hardcoded cybersecurity terms with their explanations.");

        qaMap.put("Can I clear the chat history?",
                "Yes, you can clear the chat anytime to start fresh.");

        qaMap.put("Does it explain terms in detail?",
                "The chatbot gives a short and simple description to make cybersecurity terms easy to understand.");

        qaMap.put("What is shown on the Home Screen?", "The Home Screen contains a guide for all the features of the app so you can easily understand how to use them.");
        qaMap.put("Can I access feature guides directly from the Home Screen?", "Yes, the Home Screen provides quick access to guides for every feature in the app.");
        qaMap.put("Is the Home Screen only for navigation?", "No, besides navigation, it also helps you understand the purpose of each feature through its guide.");

        qaMap.put("What does the Modules Screen show?", "The Modules Screen contains all the features of the app in a properly organized manner.");
        qaMap.put("Why is the Modules Screen useful?", "It helps you quickly find and access features without searching through the app.");
        qaMap.put("Is the Modules Screen only for navigation?", "Yes, the Modules Screen is mainly for organizing and navigating all features in one place.");
        qaMap.put("Does the Modules Screen include guides?", "No, the guides are on the Home Screen. The Modules Screen is specifically for accessing features.");

        qaMap.put("What is the Assistant Chatbot?", "The Assistant Chatbot lets you ask questions about the app’s features, and it will give short answers.");
        qaMap.put("What kind of questions can I ask?", "You can ask about any feature in the app such as scanners, privacy protectors, monitors, or utilities.");
        qaMap.put("Does the chatbot give detailed guides?", "No, the chatbot provides short and simple answers about features.");
        qaMap.put("Can the chatbot replace the feature guides?", "Not fully. The chatbot is for quick answers, while the Home Screen guides give detailed explanations.");

        qaMap.put("What can I do in the Profile Screen?", "In the Profile Screen, you can edit your profile by setting a profile picture, updating your name and username, and logging out of your account.");
        qaMap.put("Is my profile picture visible everywhere in the app?", "No, the profile picture is only visible in the Profile Section.");
        qaMap.put("How do I log out of my account?", "You can use the Logout option in the Profile Screen to sign out of your account.");

        qaMap.put("What is shown in the About Screen?", "The About Screen contains the app’s information, including details about its purpose and features.");
        qaMap.put("Can I edit anything in the About Screen?", "No, the About Screen is only for viewing the app’s information.");

    }

    // ====== Chat UI helpers ======
    private void greetUserWithTypingAnimation() {
        addTypingMessage();
        new Handler().postDelayed(() -> {
            removeLastMessage();
            addBotMessageWithTypingEffect("Hi! I’m your Assistant Chatbot. Ask me a question about the app’s features. Start typing to see suggestions.");
        }, TYPING_DELAY);
    }

    private void addUserMessage(String text) {
        chatMessageList.add(new ChatMessage("User: " + text, ChatMessage.TYPE_USER));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
    }

    private void addBotMessage(String text) {
        chatMessageList.add(new ChatMessage("Chatbot: " + text, ChatMessage.TYPE_CHATBOT));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
    }

    private void addTypingMessage() {
        chatMessageList.add(new ChatMessage("Chatbot: Typing...", ChatMessage.TYPE_CHATBOT));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
    }

    private void removeLastMessage() {
        if (!chatMessageList.isEmpty()) {
            int idx = chatMessageList.size() - 1;
            chatMessageList.remove(idx);
            chatAdapter.notifyItemRemoved(idx);
        }
    }

    private void addBotMessageWithTypingEffect(String fullText) {
        final StringBuilder builder = new StringBuilder();
        final Handler handler = new Handler();
        final int[] index = {0};

        chatMessageList.add(new ChatMessage("", ChatMessage.TYPE_CHATBOT));
        chatAdapter.notifyItemInserted(chatMessageList.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);

        final int messageIndex = chatMessageList.size() - 1;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index[0] < fullText.length()) {
                    builder.append(fullText.charAt(index[0]));
                    chatMessageList.get(messageIndex).setText("Chatbot: " + builder.toString());
                    chatAdapter.notifyItemChanged(messageIndex);
                    index[0]++;
                    handler.postDelayed(this, TYPING_SPEED);
                } else {
                    chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
                }
            }
        }, TYPING_SPEED);
    }

    // ====== Suggestions & messaging logic ======
    private void showSuggestions(String input) {
        suggestionChipGroup.removeAllViews();
        if (input.trim().isEmpty()) {
            suggestionChipGroup.setVisibility(View.GONE);
            return;
        }
        String needle = input.trim().toLowerCase(Locale.US);
        List<String> matches = new ArrayList<>();

        for (String question : qaMap.keySet()) {
            if (question.toLowerCase(Locale.US).contains(needle)) {
                matches.add(question);
            }
        }

        Collections.sort(matches);
        if (matches.isEmpty()) {
            suggestionChipGroup.setVisibility(View.GONE);
            return;
        }

        suggestionChipGroup.setVisibility(View.VISIBLE);
        for (String q : matches) {
            Chip chip = new Chip(requireContext());
            chip.setText(q);
            chip.setChipBackgroundColorResource(R.color.blue_light);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setOnClickListener(v -> {
                messageInput.setText(q);
                messageInput.setSelection(q.length());
                suggestionChipGroup.setVisibility(View.GONE);
            });
            suggestionChipGroup.addView(chip);
        }
    }

    private void sendMessage() {
        String messageText = messageInput.getText() != null ? messageInput.getText().toString().trim() : "";
        if (messageText.isEmpty()) return;

        addUserMessage(messageText);
        messageInput.setText("");
        suggestionChipGroup.setVisibility(View.GONE);

        String reply = getBotReply(messageText);

        addTypingMessage();
        new Handler().postDelayed(() -> {
            removeLastMessage();
            addBotMessageWithTypingEffect(reply);
        }, TYPING_DELAY);
    }

    private String getBotReply(String userText) {
        String lc = userText.toLowerCase(Locale.US);

        // Exact match
        for (String q : qaMap.keySet()) {
            if (q.toLowerCase(Locale.US).equals(lc)) {
                return qaMap.get(q);
            }
        }

        // Partial matches
        List<String> partials = new ArrayList<>();
        for (String q : qaMap.keySet()) {
            if (q.toLowerCase(Locale.US).contains(lc)) {
                partials.add(q);
            }
        }
        if (partials.size() == 1) {
            return qaMap.get(partials.get(0));
        } else if (partials.size() > 1) {
            StringBuilder sb = new StringBuilder("I found multiple matching questions:\n");
            int limit = Math.min(5, partials.size());
            for (int i = 0; i < limit; i++) {
                sb.append("• ").append(partials.get(i)).append("\n");
            }
            if (partials.size() > limit) sb.append("…");
            sb.append("\nPlease tap a suggestion or type the full question.");
            return sb.toString();
        }

        return "Sorry, I don’t have an answer for that. Please try another question or pick one from the suggestions.";
    }

    private void clearChat() {
        chatMessageList.clear();
        chatAdapter.notifyDataSetChanged();
    }
}
