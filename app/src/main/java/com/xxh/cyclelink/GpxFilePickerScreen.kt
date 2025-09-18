package com.xxh.cyclelink

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpxFileSelectorScreen() {
    val context = LocalContext.current
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var filePreview by remember { mutableStateOf<String?>(null) }

    // SAF 文件选择器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                selectedFileName = it.lastPathSegment
                filePreview = readGpxFromUri(context, it)?.take(300) + "..."
                Log.d("GPX", "读取外部 GPX 成功: $selectedFileName")
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPX 文件选择器") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // assets列表
            // 从 assets 里读取
            Button(
                onClick = {
                    val fileName = "20250907户外骑行.gpx" // 假设 assets/20250907户外骑行.gpx
                    selectedFileName = "assets/$fileName"
                    filePreview = readGpxFromAssets(context, fileName)?.take(300) + "..."
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("读取内置 GPX (assets)")
            }

            // 从 SAF 打开外部存储
            Button(
                onClick = {
                    launcher.launch(arrayOf("application/gpx+xml", "application/octet-stream", "text/xml"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("选择外部 GPX (SAF)")
            }

            // 显示结果
            selectedFileName?.let {
                Text("已选择: $it", style = MaterialTheme.typography.titleMedium)
            }

            filePreview?.let {
                Text("文件预览:\n$it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

fun readGpxFromAssets(context: Context, fileName: String): String? {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.e("GPX", "读取 assets 失败: $fileName", e)
        null
    }
}

fun readGpxFromUri(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
    } catch (e: Exception) {
        Log.e("GPX", "读取 SAF 文件失败: $uri", e)
        null
    }
}

fun readGpxFromFile(path: String): String? {
    return try {
        File(path).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.e("GPX", "读取 File 失败: $path", e)
        null
    }
}

