package com.bhavans.mybhavans.feature.safewalk.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bhavans.mybhavans.core.ui.theme.SuccessColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWalkRequestScreen(
    onNavigateBack: () -> Unit,
    viewModel: SafeWalkViewModel = hiltViewModel()
) {
    val createState by viewModel.createState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

    LaunchedEffect(createState.isSuccess) {
        if (createState.isSuccess) {
            snackbarHostState.showSnackbar("Walk request created!")
            viewModel.onEvent(SafeWalkEvent.ClearCreateState)
            onNavigateBack()
        }
    }

    LaunchedEffect(createState.error) {
        createState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SafeWalkEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Request Walk Buddy",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Safety info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SuccessColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🌙 Safe Walk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SuccessColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Find a buddy to walk with you during late hours. Stay safe together!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // From location
            OutlinedTextField(
                value = createState.fromLocation,
                onValueChange = { viewModel.onEvent(SafeWalkEvent.UpdateFromLocation(it)) },
                label = { Text("Starting Point") },
                placeholder = { Text("e.g., Library, Main Building") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = SuccessColor
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // To location
            OutlinedTextField(
                value = createState.toLocation,
                onValueChange = { viewModel.onEvent(SafeWalkEvent.UpdateToLocation(it)) },
                label = { Text("Destination") },
                placeholder = { Text("e.g., Hostel Block A, Campus Gate") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SuccessColor
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scheduled time display
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Scheduled Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = dateFormat.format(Date(createState.scheduledTime)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Default: 30 minutes from now",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message
            OutlinedTextField(
                value = createState.message,
                onValueChange = { viewModel.onEvent(SafeWalkEvent.UpdateMessage(it)) },
                label = { Text("Additional Message (Optional)") },
                placeholder = { Text("Any details for your buddy...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone number
            OutlinedTextField(
                value = createState.phoneNumber,
                onValueChange = { viewModel.onEvent(SafeWalkEvent.UpdatePhoneNumber(it)) },
                label = { Text("Phone Number (Optional)") },
                placeholder = { Text("For quick contact") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = { viewModel.onEvent(SafeWalkEvent.CreateRequest) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !createState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessColor
                )
            ) {
                if (createState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Find Walk Buddy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
