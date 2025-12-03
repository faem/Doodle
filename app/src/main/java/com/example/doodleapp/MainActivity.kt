package com.example.doodleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.doodleapp.ui.theme.DoodleAppTheme
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.text.color
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap

// The main activity of the application.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoodleAppTheme {
                DrawingScreen()
            }
        }
    }
}

// Data class to hold information about a single path drawn on the canvas.
data class PathData(val path: Path, val strokeWidth: Float, val color: Color)
// Enum to represent the available drawing tools.
enum class DrawingTool { PEN, ERASER }

// The main screen for the drawing application.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen() {
    // A list of paths drawn on the canvas.
    val paths = remember { mutableStateListOf<PathData>() }
    // The current stroke width for the drawing tool.
    var currentStrokeWidth by remember { mutableStateOf(5f) }
    // The current color for the drawing tool.
    var currentColor by remember { mutableStateOf(Color.Black) }
    // Whether to show the color picker dialog.
    var showColorPicker by remember { mutableStateOf(false) }
    // Whether to show the stroke picker dialog.
    var showStrokePicker by remember { mutableStateOf(false) }
    // The currently selected drawing tool.
    var selectedTool by remember { mutableStateOf(DrawingTool.PEN) }
    // Coroutine scope for launching suspend functions.
    val scope = rememberCoroutineScope()
    // State for showing snackbars.
    val snackbarHostState = remember { SnackbarHostState() }
    // The current context.
    val context = LocalContext.current
    // The size of the canvas.
    var canvasSize by remember { mutableStateOf<Size?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        // Show the color picker dialog if showColorPicker is true.
        if (showColorPicker) {
            val colors = listOf(
                Color(0xFFFF0000), Color(0xFFFFA500), Color(0xFFFFFF00), Color(0xFF7FFF00),
                Color(0xFF00FF00), Color(0xFF00FF7F), Color(0xFF00FFFF), Color(0xFF007FFF),
                Color(0xFF0000FF), Color(0xFF7F00FF), Color(0xFFFF00FF), Color(0xFFFF007F),
                Color.Black, Color.White
            )
            AlertDialog(
                onDismissRequest = { showColorPicker = false },
                title = { Text("Select Color") },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(colors) { color ->
                            Button(
                                onClick = {
                                    currentColor = color
                                    selectedTool = DrawingTool.PEN
                                    showColorPicker = false
                                },
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = color)
                            ) {}
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showColorPicker = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Show the stroke picker dialog if showStrokePicker is true.
        if (showStrokePicker) {
            val strokeWidths = listOf(5f, 10f, 15f, 20f, 25f, 30f)
            AlertDialog(
                onDismissRequest = { showStrokePicker = false },
                title = { Text("Select Stroke") },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        strokeWidths.forEach { stroke ->
                            Button(
                                onClick = {
                                    currentStrokeWidth = stroke
                                    showStrokePicker = false
                                },
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color.Gray),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((stroke / 1.5f).dp)
                                        .background(Color.Black, CircleShape)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showStrokePicker = false }) {
                        Text("Close")
                    }
                }
            )
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to open the stroke picker.
                Button(
                    onClick = { showStrokePicker = true },
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Gray),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size((currentStrokeWidth / 2).dp)
                            .background(Color.Black, CircleShape)
                    )
                }

                // Button to open the color picker.
                Button(
                    onClick = { 
                        if (selectedTool == DrawingTool.ERASER) {
                            selectedTool = DrawingTool.PEN
                        } else {
                            showColorPicker = true
                        }
                     },
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = currentColor),
                    border = if (selectedTool == DrawingTool.PEN) BorderStroke(
                        2.dp,
                        Color.Blue
                    ) else BorderStroke(1.dp, Color.Gray)
                ) {}

                // Button to select the eraser tool.
                Button(
                    onClick = { selectedTool = DrawingTool.ERASER },
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = if (selectedTool == DrawingTool.ERASER) BorderStroke(
                        2.dp,
                        Color.Blue
                    ) else BorderStroke(1.dp, Color.Gray),
                    contentPadding = PaddingValues(0.dp)
                )
                {
                    Icon(
                        painter = painterResource(id = R.drawable.eraser),
                        contentDescription = "Eraser",
                        tint = Color.Unspecified
                    )
                }

                // Button to clear the canvas.
                Button(onClick = { paths.clear() }) {
                    Text("Clear")
                }

                // Button to save the drawing.
                Button(onClick = {
                    scope.launch {
                        canvasSize?.let { size ->
                            // Create a bitmap of the drawing.
                            val bitmap = createBitmap(size.width.toInt(), size.height.toInt())
                            val canvas = android.graphics.Canvas(bitmap)
                            canvas.drawColor(android.graphics.Color.WHITE)

                            val paint = android.graphics.Paint().apply {
                                style = android.graphics.Paint.Style.STROKE
                                isAntiAlias = true
                            }

                            paths.forEach { pathData ->
                                paint.color = pathData.color.toArgb()
                                paint.strokeWidth = pathData.strokeWidth
                                canvas.drawPath(pathData.path.asAndroidPath(), paint)
                            }

                            // Save the bitmap to the device's gallery.
                            val values = ContentValues().apply {
                                put(
                                    MediaStore.Images.Media.DISPLAY_NAME,
                                    "Doodle_${System.currentTimeMillis()}.png"
                                )
                                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                                put(
                                    MediaStore.Images.Media.RELATIVE_PATH,
                                    "Pictures/Doodles"
                                )
                            }

                            val uri = context.contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )
                            uri?.let {
                                context.contentResolver.openOutputStream(it)
                                    ?.use { outputStream ->
                                        bitmap.compress(
                                            Bitmap.CompressFormat.PNG,
                                            100,
                                            outputStream
                                        )
                                    }

                                // Show a snackbar with a "View" action.
                                val result = snackbarHostState.showSnackbar(
                                    message = "Drawing saved",
                                    actionLabel = "View",
                                    withDismissAction = true
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "image/png")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }
                }) {
                    Text("Save")
                }
            }
            Divider()
            // The canvas where the drawing takes place.
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
                    .onSizeChanged { canvasSize = it.toSize() }
                    .pointerInput(true) {
                        detectDragGestures(
                            onDragStart = {
                                // Start a new path when a drag gesture begins.
                                paths.add(
                                    PathData(
                                        path = Path().apply { moveTo(it.x, it.y) },
                                        strokeWidth = currentStrokeWidth,
                                        color = if (selectedTool == DrawingTool.ERASER) Color.White else currentColor
                                    )
                                )
                            },
                            onDrag = { change, _ ->
                                // Update the last path with the new drag position.
                                change.consume()
                                val lastPathData = paths.last()
                                val newPath = Path().apply {
                                    addPath(lastPathData.path)
                                    lineTo(change.position.x, change.position.y)
                                }
                                paths[paths.size - 1] = lastPathData.copy(path = newPath)
                            }
                        )
                    }
            ) {
                // Draw all the paths on the canvas.
                paths.forEach { pathData ->
                    drawPath(
                        path = pathData.path,
                        color = pathData.color,
                        style = Stroke(width = pathData.strokeWidth)
                    )
                }
            }
        }
    }
}
