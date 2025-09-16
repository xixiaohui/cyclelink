package com.xxh.cyclelink


import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetFileList() {
    val context = LocalContext.current
    var files by remember { mutableStateOf(listOf<String>()) }

    // 读取 assets 文件列表
    LaunchedEffect(Unit) {
        files = context.assets
            .list("")      // 列出 assets 根目录下的所有文件
            ?.filter { it.endsWith(".gpx", ignoreCase = true) } // 只保留 .gpx
            ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assets 文件列表") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(files) { fileName ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = {
                            if (fileName.endsWith(".gpx", ignoreCase = true)) {
                                val intent = Intent(context, GpxViewerActivity::class.java)
                                intent.putExtra("fileName", fileName)
                                context.startActivity(intent)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "打开 $fileName"
                            )
                        }
                    }
                }
            }
        }
    }
}
