package com.example.ui

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.Challenge
import com.example.data.ChallengeDay
import com.example.viewmodel.TrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val challenges by viewModel.allChallenges.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showAddChallengeDialog by remember { mutableStateOf(false) }
    var selectedChallengeForGrid by remember { mutableStateOf<Challenge?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddChallengeDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_challenge_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "New Challenge"
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Token balance bar
            TokenBalanceBar(tokenBalance = profile?.tokenBalance ?: 0)

            if (selectedChallengeForGrid != null) {
                // Focus View of a Single Challenge Calendar Grid
                ChallengeGridDetail(
                    challenge = selectedChallengeForGrid!!,
                    viewModel = viewModel,
                    onBack = { selectedChallengeForGrid = null }
                )
            } else {
                // List of all active Challenges
                if (challenges.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No active challenges!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Create a goal challenge of minimum 2 days to start tracking micro-habits.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(challenges) { challenge ->
                            ChallengeCard(
                                challenge = challenge,
                                viewModel = viewModel,
                                onClick = { selectedChallengeForGrid = challenge }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Challenge inputs Dialog
    if (showAddChallengeDialog) {
        AddChallengeDialog(
            onDismiss = { showAddChallengeDialog = false },
            onConfirm = { name, duration, colorHex ->
                if (name.isBlank()) {
                    Toast.makeText(context, context.getString(R.string.error_empty_name), Toast.LENGTH_SHORT).show()
                } else if (duration < 2) {
                    Toast.makeText(context, context.getString(R.string.error_duration), Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addChallenge(name, duration, colorHex)
                    showAddChallengeDialog = false
                    Toast.makeText(context, "Challenge Created!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun TokenBalanceBar(tokenBalance: Int) {
    Card(
        shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("token_balance_bar")
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🪙",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = stringResource(R.string.coins_balance, tokenBalance),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Sync Cloud Level",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeCard(
    challenge: Challenge,
    viewModel: TrackerViewModel,
    onClick: () -> Unit
) {
    val daysFlow = remember(challenge.id) { viewModel.getDaysForChallenge(challenge.id) }
    val days by daysFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val totalDays = challenge.durationDays
    val completedDays = days.count { it.isLogged }
    val progress = if (totalDays > 0) completedDays.toFloat() / totalDays else 0f

    val cardColor = Color(android.graphics.Color.parseColor(challenge.baseThemeColorHex))

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .testTag("challenge_container_${challenge.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(cardColor, CircleShape)
                    )
                    Text(
                        text = challenge.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { viewModel.removeChallenge(challenge.id) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$completedDays / $totalDays completed days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = cardColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = cardColor,
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun ChallengeGridDetail(
    challenge: Challenge,
    viewModel: TrackerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val daysFlow = remember(challenge.id) { viewModel.getDaysForChallenge(challenge.id) }
    val days by daysFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val challengeColor = Color(android.graphics.Color.parseColor(challenge.baseThemeColorHex))

    var activeLogDay by remember { mutableStateOf<ChallengeDay?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toolbar back
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = challenge.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Track your days below. Click a cell to choose mood and snap a sticker to earn coin tokens!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Grid representing challenge duration
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(days) { index, day ->
                DayCell(
                    day = day,
                    index = index + 1,
                    onLogClick = { activeLogDay = day }
                )
            }
        }
    }

    // Modal Sheet / Dialog to insert stickers and pick emojis
    if (activeLogDay != null) {
        DayLoggingDialog(
            day = activeLogDay!!,
            onDismiss = { activeLogDay = null },
            onSave = { updatedDay ->
                viewModel.logChallengeDay(updatedDay)
                activeLogDay = null
                Toast.makeText(context, context.getString(R.string.logged_day_msg), Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun DayCell(
    day: ChallengeDay,
    index: Int,
    onLogClick: () -> Unit
) {
    val cellBorderColor = if (day.isLogged) {
        Color(android.graphics.Color.parseColor(day.dayColorHex ?: "#3B82F6"))
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val cellBgColor = if (day.isLogged) {
        Color(android.graphics.Color.parseColor(day.dayColorHex ?: "#3B82F6")).copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(cellBgColor)
            .border(2.dp, cellBorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onLogClick)
            .testTag("day_cell_$index"),
        contentAlignment = Alignment.Center
    ) {
        // Main mood emoji / Day index layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (day.isLogged && !day.moodEmoji.isNullOrEmpty()) {
                Text(
                    text = day.moodEmoji,
                    fontSize = 26.sp
                )
            } else {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Circular or square sticker thumbnail attached preview
        if (day.isLogged && !day.stickerBase64.isNullOrEmpty()) {
            val bitmap = remember(day.stickerBase64) {
                StickerEngine.base64ToBitmap(day.stickerBase64)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Day Sticker",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayLoggingDialog(
    day: ChallengeDay,
    onDismiss: () -> Unit,
    onSave: (ChallengeDay) -> Unit
) {
    val context = LocalContext.current

    var selectedMood by remember { mutableStateOf(day.moodEmoji ?: "😊") }
    var selectedColorHex by remember { mutableStateOf(day.dayColorHex ?: "#3B82F6") }
    var stickerBase64 by remember { mutableStateOf<String?>(day.stickerBase64) }
    var stickerBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Initialize sticker bitmap if available
    LaunchedEffect(day.stickerBase64) {
        day.stickerBase64?.let {
            stickerBitmap = StickerEngine.base64ToBitmap(it)
        }
    }

    // Camera photo launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val cropped = StickerEngine.cropToShape(it, isCircle = true)
            stickerBitmap = cropped
            stickerBase64 = StickerEngine.bitmapToBase64(cropped)
        }
    }

    // Gallery selector
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = StickerEngine.uriToBitmap(context, it)
            if (bitmap != null) {
                val cropped = StickerEngine.cropToShape(bitmap, isCircle = true)
                stickerBitmap = cropped
                stickerBase64 = StickerEngine.bitmapToBase64(cropped)
            }
        }
    }

    val moodEmojis = listOf("😊", "😌", "😔", "😤", "😴", "🔥")
    val gridColorThemes = listOf("#3B82F6", "#10B981", "#059669", "#F59E0B", "#8B5CF6")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.day_log_title, day.dayIndex + 1),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mood Selection
                Text(
                    text = stringResource(R.string.select_mood),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    moodEmojis.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    if (selectedMood == emoji) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .border(
                                    2.dp,
                                    if (selectedMood == emoji) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .clickable { selectedMood = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Color Selector
                Text(
                    text = stringResource(R.string.select_color),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.wrapContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    gridColorThemes.forEach { hex ->
                        val themeColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(themeColor)
                                .border(
                                    3.dp,
                                    if (selectedColorHex == hex) MaterialTheme.colorScheme.onSurface
                                    else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Camera/Gallery local attachments
                Text(
                    text = stringResource(R.string.attach_sticker),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Preview box of sticker
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (stickerBitmap != null) {
                            Image(
                                bitmap = stickerBitmap!!.asImageBitmap(),
                                contentDescription = "Active sticker",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera Snap", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick Gallery", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Control actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            val updated = day.copy(
                                moodEmoji = selectedMood,
                                dayColorHex = selectedColorHex,
                                stickerBase64 = stickerBase64,
                                isLogged = true,
                                loggedTimestamp = System.currentTimeMillis()
                            )
                            onSave(updated)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(stringResource(R.string.save_log))
                    }
                }
            }
        }
    }
}

@Composable
fun AddChallengeDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, duration: Int, colorHex: String) -> Unit
) {
    var challengeName by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("7") }
    var selectedThemeColorHex by remember { mutableStateOf("#3B82F6") }

    val themeOptions = listOf("#3B82F6", "#10B981", "#8B5CF6", "#EF4444", "#F59E0B")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.add_challenge),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = challengeName,
                    onValueChange = { challengeName = it },
                    label = { Text(stringResource(R.string.challenge_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("challenge_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text(stringResource(R.string.challenge_duration)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Theme selection
                Text(
                    text = "Pick Base Challenge Color",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    themeOptions.forEach { hex ->
                        val col = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    3.dp,
                                    if (selectedThemeColorHex == hex) MaterialTheme.colorScheme.onSurface
                                    else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedThemeColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            val durationVal = durationText.toIntOrNull() ?: 2
                            onConfirm(challengeName, durationVal, selectedThemeColorHex)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_challenge_button")
                    ) {
                        Text(stringResource(R.string.create))
                    }
                }
            }
        }
    }
}
