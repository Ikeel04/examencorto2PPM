package edu.uvg.uvgchatejemplo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Data class for a message, with a flag for encryption
data class Message(val content: String, val author: String, val isEncrypted: Boolean)

// ViewModel for chat logic
class ChatViewModel : androidx.lifecycle.ViewModel() {
    private val chatPasswords = mutableMapOf<String, String>()
    private val chatMessages = mutableMapOf<String, List<Message>>()

    // Add a new message to the chat
    fun addMessage(roomId: String, message: Message) {
        chatMessages[roomId] = chatMessages.getOrDefault(roomId, emptyList()) + message
    }

    // Retrieve messages for a specific chat room
    fun getMessages(roomId: String): List<Message> {
        return chatMessages.getOrDefault(roomId, emptyList())
    }

    // Set a password for a chat room
    fun setPassword(roomId: String, password: String) {
        chatPasswords[roomId] = password
    }

    // Check if the entered password is correct
    fun isPasswordCorrect(roomId: String, password: String): Boolean {
        return chatPasswords[roomId] == password
    }

    // Simple encryption by reversing the message content
    fun encryptMessage(content: String): String {
        return content.reversed()
    }
}

@Composable
fun ChatViewModelScreen(
    roomId: String,
    chatViewModel: ChatViewModel = viewModel(),
    onBack: () -> Unit
) {
    var enteredPassword by remember { mutableStateOf("") }
    var correctPasswordEntered by remember { mutableStateOf(false) }
    val messages = chatViewModel.getMessages(roomId)

    if (!correctPasswordEntered) {
        // Show the password prompt before entering the chat
        PasswordPrompt(
            roomId = roomId,
            onPasswordEntered = { password ->
                // Check if the password is correct
                if (chatViewModel.isPasswordCorrect(roomId, password)) {
                    correctPasswordEntered = true
                }
            },
            setPassword = { password ->
                // Set a new password for the room
                chatViewModel.setPassword(roomId, password)
                correctPasswordEntered = true
            },
            onEnterChat = {
                // Once the password is set or correct, navigate to chat view
                correctPasswordEntered = true
            }
        )
    } else {
        // Show the chat content after entering the correct password
        ChatContent(messages, chatViewModel::encryptMessage, enteredPassword, chatViewModel.isPasswordCorrect(roomId, enteredPassword))
    }
}

@Composable
fun ChatContent(messages: List<Message>, encryptMessage: (String) -> String, password: String, isPasswordCorrect: Boolean) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Display each message, encrypt if the password is incorrect
        messages.forEach { message ->
            Text(
                text = if (!isPasswordCorrect && message.isEncrypted) encryptMessage(message.content) else message.content,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordPrompt(
    roomId: String,
    onPasswordEntered: (String) -> Unit,
    setPassword: (String) -> Unit,
    onEnterChat: () -> Unit // New parameter for navigating to the chat screen
) {
    var password by remember { mutableStateOf("") }
    var isSetPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter password for chat $roomId")
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    onPasswordEntered(password)
                    onEnterChat() // Navigate to chat once password is entered
                }
            ) {
                Text("Enter Chat")
            }
            Button(onClick = { isSetPasswordVisible = true }) {
                Text("Set New Password")
            }
        }

        // Optionally show UI to set a new password
        if (isSetPasswordVisible) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Set New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            Button(
                onClick = {
                    setPassword(password)
                    onEnterChat() // Navigate to chat once the password is set
                }
            ) {
                Text("Set Password")
            }
        }
    }
}

@Preview
@Composable
fun ChatScreenPreview() {
    ChatViewModelScreen(roomId = "roomId", onBack = {})
}
