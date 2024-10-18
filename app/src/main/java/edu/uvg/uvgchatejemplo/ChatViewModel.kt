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

data class Message(val content: String, val author: String, val isEncrypted: Boolean)

class ChatViewModel : androidx.lifecycle.ViewModel() {
    private val chatPasswords = mutableMapOf<String, String>()
    private val chatMessages = mutableMapOf<String, List<Message>>()

    fun addMessage(roomId: String, message: Message) {
        chatMessages[roomId] = chatMessages.getOrDefault(roomId, emptyList()) + message
    }

    fun getMessages(roomId: String): List<Message> {
        return chatMessages.getOrDefault(roomId, emptyList())
    }

    fun setPassword(roomId: String, password: String) {
        chatPasswords[roomId] = password
    }

    fun isPasswordCorrect(roomId: String, password: String): Boolean {
        return chatPasswords[roomId] == password
    }

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
        PasswordPrompt(
            roomId = roomId,
            onPasswordEntered = { password ->
                if (chatViewModel.isPasswordCorrect(roomId, password)) {
                    correctPasswordEntered = true
                }
            },
            setPassword = { password ->
                chatViewModel.setPassword(roomId, password)
                correctPasswordEntered = true
            }
        )
    } else {
        ChatContent(messages, chatViewModel::encryptMessage)
    }
}

@Composable
fun ChatContent(messages: List<Message>, encryptMessage: (String) -> String) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        messages.forEach { message ->
            Text(
                text = if (message.isEncrypted) encryptMessage(message.content) else message.content,
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
    setPassword: (String) -> Unit
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
            Button(onClick = { onPasswordEntered(password) }) {
                Text("Enter Chat")
            }
            Button(onClick = { isSetPasswordVisible = true }) {
                Text("Set New Password")
            }
        }

        if (isSetPasswordVisible) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Set New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            Button(onClick = { setPassword(password) }) {
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
