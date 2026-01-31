package com.example.hw3database

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.room.*
import coil.compose.rememberAsyncImagePainter
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HW2NavigationTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { MainScreen(navController) }
                    composable("options") { OptionsScreen(navController) }
                    composable("hw3_input") { HW3InputScreen(navController) }
                    composable("hw3_display") { HW3DisplayScreen(navController) }
                }
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Scaffold(containerColor = Color(0xFFE8F5E9)) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Conversation(messages = SampleData.messages)
            Button(
                onClick = {
                    navController.navigate("options") {
                        popUpTo("main") { inclusive = false }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Open Options")
            }
        }
    }
}

@Composable
fun OptionsScreen(navController: NavController) {
    var darkMode by remember { mutableStateOf(false) }
    var fontLarge by remember { mutableStateOf(false) }
    var highlight by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        if (darkMode) Color(0xFF263238) else Color(0xFFF5F5F5)
    )

    Scaffold(containerColor = backgroundColor) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "App Options",
                color = if (darkMode) Color.White else Color.Black,
                fontSize = if (fontLarge) MaterialTheme.typography.headlineMedium.fontSize
                else MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dark Mode", modifier = Modifier.weight(1f))
                Switch(checked = darkMode, onCheckedChange = { darkMode = it })
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Large Text", modifier = Modifier.weight(1f))
                Switch(checked = fontLarge, onCheckedChange = { fontLarge = it })
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Highlight UI", modifier = Modifier.weight(1f))
                Switch(checked = highlight, onCheckedChange = { highlight = it })
            }

            Surface(
                color = if (highlight) Color(0xFF81C784) else Color.Transparent,
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                Text(
                    text = "Live preview of selected options",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black
                )
            }

            Button(
                onClick = { navController.navigate("hw3_input") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile (HW3)")
            }

            Button(
                onClick = { navController.navigate("hw3_display") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Profile")
            }

            Button(
                onClick = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Main Screen")
            }
        }
    }
}

@Composable
fun HW3InputScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }

    var username by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("HW3 Input", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { picker.launch("image/*") }) {
                Text("Pick Image")
            }

            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
            }

            Button(
                onClick = {
                    val file = File(context.filesDir, "profile_image.jpg")
                    imageUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }

                    db.profileDao().save(
                        ProfileEntity(0, username, file.absolutePath)
                    )

                    navController.navigate("hw3_display")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Button(
                onClick = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Home")
            }
        }
    }
}

@Composable
fun HW3DisplayScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val profile = remember { db.profileDao().load() }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    navController.navigate("options") {
                        popUpTo("options") { inclusive = true }
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to App Options")
            }

            if (profile == null) {
                Text("No profile saved. Please enter data.")
            } else {
                Text("Username: ${profile.username}")
                Image(
                    painter = rememberAsyncImagePainter(File(profile.imagePath)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )

            }

            Button(
                onClick = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Home")
            }
        }
    }
}

@Entity
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val username: String,
    val imagePath: String
)

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(profile: ProfileEntity)

    @Query("SELECT * FROM ProfileEntity WHERE id = 0")
    fun load(): ProfileEntity?
}

@Database(
    entities = [ProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao

    companion object {
        fun get(context: android.content.Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "hw3_db")
                .allowMainThreadQueries()
                .build()
    }
}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message) {
    Row(modifier = Modifier.padding(8.dp)) {
        Image(
            painter = painterResource(R.drawable.profile_picture),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface
        )
        Column(
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .fillMaxWidth()
        ) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier.animateContentSize().padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(8.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1
                )
            }
        }
    }
}

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        items(messages) { MessageCard(it) }
    }
}

object SampleData {
    val messages = List(20) {
        Message("Muhannad", "This is message number ${it + 1}. Tap to expand and see more text.")
    }
}

@Composable
fun HW2NavigationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF81C784),
            secondary = Color(0xFF388E3C),
            background = Color(0xFFE8F5E9),
            surface = Color.White
        ),
        content = content
    )
}
