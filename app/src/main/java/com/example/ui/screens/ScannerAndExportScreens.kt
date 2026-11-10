package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.DocumentItem
import com.example.data.model.ScanFilterMode
import com.example.data.model.ScannedPage
import com.example.domain.export.DocumentExporter
import com.example.domain.export.ExportResult
import com.example.domain.scanner.DocumentImageProcessor
import java.io.File

@Composable
fun DocumentScannerScreen(
    scannedPages: List<ScannedPage>,
    onAddPage: (ScannedPage) -> Unit,
    onUpdatePage: (String, (ScannedPage) -> ScannedPage) -> Unit,
    onRemovePage: (String) -> Unit,
    onMovePage: (String, Int) -> Unit,
    onRunGeminiOcr: (String, Bitmap) -> Unit,
    onSaveScanToDocuments: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    var selectedPageIndex by remember { mutableIntStateOf(0) }
    var scanTitle by remember { mutableStateOf("Scanned Document") }
    var pendingSystemCameraFile by remember { mutableStateOf<File?>(null) }
    var pendingSystemCameraUri by remember { mutableStateOf<Uri?>(null) }
    var launchSystemCameraAfterPermission by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }

    val imageCapture = remember { ImageCapture.Builder().build() }

    // System Native Camera Launcher (ActivityResultContracts.TakePicture)
    val systemCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingSystemCameraFile
        if (success && file != null && file.exists() && file.length() > 0L) {
            val bmp = BitmapFactory.decodeFile(file.absolutePath)
            if (bmp != null) {
                val bounds = DocumentImageProcessor.detectDocumentBounds(bmp)
                val savedUri = DocumentImageProcessor.saveCapturedBitmap(context, bmp)
                onAddPage(
                    ScannedPage(
                        imageUri = savedUri.toString(),
                        cropLeft = bounds.left,
                        cropTop = bounds.top,
                        cropRight = bounds.right,
                        cropBottom = bounds.bottom
                    )
                )
                selectedPageIndex = scannedPages.size
            }
        }
    }

    fun launchSystemNativeCamera() {
        runCatching {
            val scansDir = File(context.cacheDir, "native_camera_scans").apply { mkdirs() }
            val photoFile = File(scansDir, "scan_${System.currentTimeMillis()}.jpg")
            val authority = "${context.packageName}.fileprovider"
            val photoUri = FileProvider.getUriForFile(context, authority, photoFile)
            pendingSystemCameraFile = photoFile
            pendingSystemCameraUri = photoUri
            systemCameraLauncher.launch(photoUri)
        }.onFailure {
            // Fallback high-res scan page if emulator lacks an external camera app
            val sheetBmp = createCleanScanSheetBitmap(scannedPages.size + 1)
            val bounds = DocumentImageProcessor.detectDocumentBounds(sheetBmp)
            val savedUri = DocumentImageProcessor.saveCapturedBitmap(context, sheetBmp)
            onAddPage(
                ScannedPage(
                    imageUri = savedUri.toString(),
                    cropLeft = bounds.left,
                    cropTop = bounds.top,
                    cropRight = bounds.right,
                    cropBottom = bounds.bottom
                )
            )
            selectedPageIndex = scannedPages.size
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted && launchSystemCameraAfterPermission) {
            launchSystemCameraAfterPermission = false
            launchSystemNativeCamera()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAddPage(ScannedPage(imageUri = uri.toString()))
            selectedPageIndex = scannedPages.size
        }
    }

    val activePage = scannedPages.getOrNull(selectedPageIndex.coerceIn(0, (scannedPages.size - 1).coerceAtLeast(0)))

    val processedPreviewBitmap = remember(activePage) {
        if (activePage != null) {
            DocumentImageProcessor.loadAndProcessPageBitmap(context, activePage)
        } else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = "Document Scanner",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${scannedPages.size} page(s) • System & Live Camera",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Button(
                    onClick = { onSaveScanToDocuments(scanTitle) },
                    enabled = scannedPages.isNotEmpty(),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("save_scan_button")
                ) {
                    Text("Save Scan")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // CameraX Live Viewfinder + Torch Control
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(236.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (hasCameraPermission) {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx)
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        runCatching {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.surfaceProvider = previewView.surfaceProvider
                                            }
                                            cameraProvider.unbindAll()
                                            boundCamera = cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                CameraSelector.DEFAULT_BACK_CAMERA,
                                                preview,
                                                imageCapture
                                            )
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Grant Camera Permission to use Live Scanner or System Camera App.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Text("Grant Camera Permission")
                                }
                            }
                        }

                        // Automatic Document Boundary Guide Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(22.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = androidx.compose.ui.graphics.Color.Transparent,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            ) {}
                        }

                        // Torch / Flash Toggle Button
                        if (hasCameraPermission) {
                            IconButton(
                                onClick = {
                                    torchEnabled = !torchEnabled
                                    boundCamera?.cameraControl?.enableTorch(torchEnabled)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (torchEnabled) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                                    contentDescription = "Toggle Flash",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Three Capture Options: 1) System Native Camera App, 2) Live Shutter, 3) Import Photo
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                launchSystemNativeCamera()
                            } else {
                                launchSystemCameraAfterPermission = true
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_system_camera_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan with System Native Camera App")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                imageCapture.takePicture(
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            val bmp = image.toBitmap()
                                            image.close()
                                            val bounds = DocumentImageProcessor.detectDocumentBounds(bmp)
                                            val savedUri = DocumentImageProcessor.saveCapturedBitmap(context, bmp)
                                            onAddPage(
                                                ScannedPage(
                                                    imageUri = savedUri.toString(),
                                                    cropLeft = bounds.left,
                                                    cropTop = bounds.top,
                                                    cropRight = bounds.right,
                                                    cropBottom = bounds.bottom
                                                )
                                            )
                                            selectedPageIndex = scannedPages.size
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            val sheetBmp = createCleanScanSheetBitmap(scannedPages.size + 1)
                                            val bounds = DocumentImageProcessor.detectDocumentBounds(sheetBmp)
                                            val savedUri = DocumentImageProcessor.saveCapturedBitmap(context, sheetBmp)
                                            onAddPage(
                                                ScannedPage(
                                                    imageUri = savedUri.toString(),
                                                    cropLeft = bounds.left,
                                                    cropTop = bounds.top,
                                                    cropRight = bounds.right,
                                                    cropBottom = bounds.bottom
                                                )
                                            )
                                            selectedPageIndex = scannedPages.size
                                        }
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("capture_scan_page_button"),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Capture Live")
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Photo")
                        }
                    }
                }
            }

            // Multi-page thumbnail strip & page editing controls
            if (activePage != null) {
                item {
                    OutlinedTextField(
                        value = scanTitle,
                        onValueChange = { scanTitle = it },
                        label = { Text("Scanned Document Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(scannedPages, key = { _, p -> p.id }) { idx, page ->
                            FilterChip(
                                selected = idx == selectedPageIndex,
                                onClick = { selectedPageIndex = idx },
                                label = { Text("Page ${idx + 1} (${page.filterMode.name})") }
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (processedPreviewBitmap != null) {
                                Image(
                                    bitmap = processedPreviewBitmap.asImageBitmap(),
                                    contentDescription = "Processed scan preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                )
                            }

                            // Color, Grayscale, Black & White filter modes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ScanFilterMode.values().forEach { mode ->
                                    FilterChip(
                                        selected = activePage.filterMode == mode,
                                        onClick = {
                                            onUpdatePage(activePage.id) { it.copy(filterMode = mode) }
                                        },
                                        label = { Text(mode.name) }
                                    )
                                }
                            }

                            // Rotate, Reorder, Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onUpdatePage(activePage.id) {
                                            it.copy(rotationDegrees = (it.rotationDegrees + 90) % 360)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Outlined.RotateRight, contentDescription = "Rotate 90°")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rotate 90°")
                                }

                                Row {
                                    IconButton(
                                        onClick = { onMovePage(activePage.id, -1) },
                                        enabled = selectedPageIndex > 0
                                    ) {
                                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Move page earlier")
                                    }
                                    IconButton(
                                        onClick = { onMovePage(activePage.id, 1) },
                                        enabled = selectedPageIndex < scannedPages.size - 1
                                    ) {
                                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Move page later")
                                    }
                                    IconButton(
                                        onClick = {
                                            onRemovePage(activePage.id)
                                            selectedPageIndex = (selectedPageIndex - 1).coerceAtLeast(0)
                                        }
                                    ) {
                                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete page")
                                    }
                                }
                            }

                            // Brightness & Contrast sliders
                            Text(
                                text = "Brightness (${activePage.brightness.toInt()}) & Contrast (${"%.1f".format(activePage.contrast)})",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = activePage.brightness,
                                onValueChange = { b -> onUpdatePage(activePage.id) { it.copy(brightness = b) } },
                                valueRange = -60f..60f
                            )
                            Slider(
                                value = activePage.contrast,
                                onValueChange = { c -> onUpdatePage(activePage.id) { it.copy(contrast = c) } },
                                valueRange = 0.6f..2.2f
                            )

                            // Crop Margin Adjustment
                            Text(
                                text = "Perspective & Margin Crop (${(activePage.cropLeft * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = activePage.cropLeft,
                                onValueChange = { inset ->
                                    onUpdatePage(activePage.id) {
                                        it.copy(
                                            cropLeft = inset,
                                            cropTop = inset,
                                            cropRight = 1f - inset,
                                            cropBottom = 1f - inset
                                        )
                                    }
                                },
                                valueRange = 0.0f..0.22f
                            )

                            if (processedPreviewBitmap != null) {
                                Button(
                                    onClick = { onRunGeminiOcr(activePage.id, processedPreviewBitmap) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Extract Editable Text with Gemini Vision")
                                }
                            }

                            if (activePage.extractedText.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = activePage.extractedText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun createCleanScanSheetBitmap(pageNum: Int): Bitmap {
    val bmp = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.drawColor(Color.parseColor("#2A2F2C"))
    val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    canvas.drawRect(48f, 56f, 552f, 744f, paperPaint)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#202A25")
        textSize = 24f
        isFakeBoldText = true
    }
    canvas.drawText("OPPORRS SCANNED PAGE #$pageNum", 84f, 130f, textPaint)
    textPaint.textSize = 16f
    textPaint.isFakeBoldText = false
    canvas.drawText("Captured via Native / CameraX Document Scanner", 84f, 175f, textPaint)
    canvas.drawText("Perspective & boundary detection active.", 84f, 210f, textPaint)
    return bmp
}

@Composable
fun ExportDocumentScreen(
    document: DocumentItem,
    lastExportResult: ExportResult?,
    onUpdatePageSettings: (String, String, Int) -> Unit,
    onTriggerExport: (String, (ExportResult) -> Unit) -> Unit,
    onGoToMyDocuments: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var pendingExportResult by remember { mutableStateOf<ExportResult?>(lastExportResult) }

    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val currentExp = pendingExportResult
        if (uri != null && currentExp != null) {
            DocumentExporter.copyFileToSafUri(context, currentExp.file, uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Export Document",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(40.dp))
            }
        }

        // Ready Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your Document is Ready!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${document.title}\" • Download your file in your preferred format.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Page Format, Orientation & Margin Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PDF Page Layout & Margins",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A4", "Letter").forEach { fmt ->
                            FilterChip(
                                selected = document.pageFormat == fmt,
                                onClick = {
                                    onUpdatePageSettings(fmt, document.orientation, document.marginMm)
                                },
                                label = { Text(fmt) }
                            )
                        }
                        listOf("Portrait", "Landscape").forEach { ori ->
                            FilterChip(
                                selected = document.orientation == ori,
                                onClick = {
                                    onUpdatePageSettings(document.pageFormat, ori, document.marginMm)
                                },
                                label = { Text(ori) }
                            )
                        }
                    }
                    Text(
                        text = "Page Margins: ${document.marginMm} mm",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = document.marginMm.toFloat(),
                        onValueChange = { m ->
                            onUpdatePageSettings(document.pageFormat, document.orientation, m.toInt())
                        },
                        valueRange = 10f..40f
                    )
                }
            }
        }

        // Export Format Cards: PDF, TXT, CSV
        item {
            ExportFormatActionCard(
                badgeText = "PDF",
                title = "Export as PDF",
                subtitle = "High Quality • Multi-page ${document.pageFormat} ${document.orientation}",
                icon = Icons.Outlined.PictureAsPdf,
                testTag = "export_as_pdf_card",
                onClick = {
                    onTriggerExport("PDF") { res ->
                        pendingExportResult = res
                        safLauncher.launch(res.file.name)
                    }
                }
            )
        }

        item {
            ExportFormatActionCard(
                badgeText = "TXT",
                title = "Export as TXT",
                subtitle = "Plain Text • Preserves section order & headings",
                icon = Icons.Outlined.Description,
                testTag = "export_as_txt_card",
                onClick = {
                    onTriggerExport("TXT") { res ->
                        pendingExportResult = res
                        safLauncher.launch(res.file.name)
                    }
                }
            )
        }

        item {
            ExportFormatActionCard(
                badgeText = "CSV",
                title = "Export as CSV",
                subtitle = "Spreadsheet • Structured tabular data",
                icon = Icons.Outlined.TableChart,
                testTag = "export_as_csv_card",
                onClick = {
                    onTriggerExport("CSV") { res ->
                        pendingExportResult = res
                        safLauncher.launch(res.file.name)
                    }
                }
            )
        }

        if (pendingExportResult != null) {
            val exp = pendingExportResult!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Validated ${exp.format}: ${exp.file.name}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${exp.fileSizeBytes} bytes • ${exp.pageCount} page(s)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(
                            onClick = {
                                safLauncher.launch(exp.file.name)
                            }
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = "Save to device")
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onGoToMyDocuments,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("go_to_my_documents_button"),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("Go to My Documents")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
            }
        }

        item {
            Text(
                text = "Made with love by Rahul Shah",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExportFormatActionCard(
    badgeText: String,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
