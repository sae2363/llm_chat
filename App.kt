import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.random.Random

const val urlBase = "http://127.0.0.1:5001//"

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        var output by remember { mutableStateOf("pending output") }
        var base by remember { mutableStateOf("") }
        var text by remember { mutableStateOf("") }
        var messageList by remember {
            mutableStateOf(listOf(Message("Hello!", "bot")).toMutableList())
        }
        ChatScreen(messageList)
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                textBox(text = text, "Enter text", "Input") { newText ->
                    text = newText
                }

                Button(
                    onClick = {
                        Thread.sleep(250)
                        messageList.add(Message(text, "you"))
                        sendInput(text, base,messageList.size) { newOutput ->
                            output = newOutput
                            messageList.add(Message(output, "bot"))
                            messageList.toSet().toList()
                        }

                    },
                ) {
                    Icon(Icons.Filled.Add, "hi")
                }
            }
            enterBasePrompt(base) { newOutput2 ->
                base = newOutput2
            }
            Box(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        topStart = 48f, topEnd = 48f, bottomStart = 48f, bottomEnd = 48f
                    )
                ).background(Color.Green).padding(12.dp)
                    .size(200.dp,100.dp)
            ) {
                Text(color = Color.White, text = base, style = TextStyle(fontSize = 14.sp))
            }
            Button(
                onClick = {
                    (urlBase + "ClearData").httpGet()
                    Fuel.get(urlBase+"ClearData")
                    messageList.clear() // Clear the list
                    messageList.add(Message("Hello!","bot"))
                    output = "History Cleared"
                },
            ) { Text("Clear history") }
            Text(text)
            Text("out $output", modifier = Modifier.width(300.dp), fontSize = 6.sp)
            Text(messageList.toString(), modifier = Modifier.width(200.dp), fontSize = 10.sp)


        }
    }
}

@Composable
fun enterBasePrompt(base: String, onOutput: (String) -> Unit) {
    var prompt by remember { mutableStateOf(base) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(200.dp)
    ) {
        textBox(prompt, "Enter Base Prompt", "input",) { newText ->
            prompt = newText
        }
        Button(
            onClick = {
                onOutput(prompt)
            },
        ) {
            Icon(Icons.Filled.Add, "hi")
        }
    }
}

fun sendInput(inputText: String, BasePrompt: String,msgSize:Int, onOutput: (String) -> Unit) {
    val token = msgSize*2
    var text = inputText//"hi how are you"

    val map = mapOf(
        "token" to token, "prompt" to text, "basePrompt" to BasePrompt
    )

    //onOutput(text + "work")

    val gson = Gson()
    val jsonContent2 = gson.toJson(map)
    (urlBase + "SendData").httpPost().header("Content-Type" to "application/json")
        .body(jsonContent2).response { request, response, result ->
            when (result) {
                is com.github.kittinunf.result.Result.Success -> {
                    //val responseData = result.get()
                }

                is com.github.kittinunf.result.Result.Failure -> {
                    val ex = result.getException()
                    // Handle error
                }
            }
        }

    val parameterName = "?token"

    val url = urlBase + "getdata" + parameterName + "=" + token

    var i = 0
    while (i < 60) {
        Thread.sleep(1500)
        if(i>25){
            Thread.sleep(3000)
        }
        if(i%2==0){
            Thread.sleep(1000)
        }
        (url).httpGet().response { request, response, result ->
            when (result) {
                is com.github.kittinunf.result.Result.Success -> {
                    var byteArray = result.value
                    val jsonString = String(byteArray, Charsets.UTF_8)

                    // Parse string as JSON
                    val gson = Gson()
                    var outputJson = gson.fromJson(jsonString, Any::class.java)
                    if (outputJson.toString() != "waiting" && outputJson.toString() != "") {
                        onOutput(outputJson.toString())

                        i += 10000
                    }
                }

                is com.github.kittinunf.result.Result.Failure -> {
                    val ex = result.error
                    println("Request failed: $ex")
                }
            }
        }
        i++
    }
}

@Composable
fun textBox(text: String, text2: String, text3: String, onTextChange: (String) -> Unit) {
    OutlinedTextField(modifier = Modifier.padding(vertical = 10.dp).width(250.dp),
        value = text,
        onValueChange = onTextChange,
        label = { Text(text2) },
        placeholder = { Text(text3) })
}

@Composable
fun TextBubble(message: Message) {
    Box(
        modifier = Modifier.clip(
            RoundedCornerShape(
                topStart = 48f,
                topEnd = 48f,
                bottomStart = if (message.isBot()) 48f else 0f,
                bottomEnd = if (message.isBot()) 0f else 48f
            )
        ).background(Color.Blue).padding(12.dp)
    ) {
        Text(color = Color.White, text = message.text, style = TextStyle(fontSize = 14.sp))
        //if (message.isBot()) Alignment.End else Alignment.Start
    }
}

@Composable
fun ChatScreen(messages2: List<Message>) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp) // Adjust padding as needed
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight().width(400.dp).align(Alignment.CenterEnd)
        ) {
            var i = 0
            var messages = messages2.toMutableList()
            while (i < messages.size - 1) {
                if (messages[i].text == messages[i + 1].text) {
                    messages.removeAt(i)
                    i--
                }
                i++
            }
            items(messages) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.isBot()) Arrangement.End else Arrangement.Start
                ) {
                    TextBubble(message)
                }
            }
        }
    }
}


data class Message(
    val text: String,
    val author: String,
) {
    fun isBot(): Boolean {
        return author == "bot"
    }
}