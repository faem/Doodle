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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

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

data class PathData(val path: Path, val strokeWidth: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen() {
    val paths = remember { mutableStateListOf<PathData>() }
    var currentStrokeWidth by remember { mutableStateOf(5f) }
    var showStrokePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doodle") },
                actions = {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(onClick = { paths.clear() }) {
                            Text("Clear")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
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

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .pointerInput(true) {
                    detectDragGestures(
                        onDragStart = {
                            paths.add(
                                PathData(
                                    path = Path().apply { moveTo(it.x, it.y) },
                                    strokeWidth = currentStrokeWidth
                                )
                            )
                        },
                        onDrag = { change, _ ->
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
            paths.forEach { pathData ->
                drawPath(
                    path = pathData.path,
                    color = Color.Black,
                    style = Stroke(width = pathData.strokeWidth)
                )
            }
        }
    }
}
