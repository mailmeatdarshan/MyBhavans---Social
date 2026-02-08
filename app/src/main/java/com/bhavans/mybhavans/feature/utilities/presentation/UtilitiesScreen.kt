package com.bhavans.mybhavans.feature.utilities.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhavans.mybhavans.core.ui.theme.BhavansAccent
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.core.ui.theme.BhavansSecondary
import com.bhavans.mybhavans.core.ui.theme.SuccessColor

data class UtilityItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilitiesScreen(
    onNavigateToLostFound: () -> Unit = {},
    onNavigateToCanteen: () -> Unit = {},
    onNavigateToSkillSwap: () -> Unit = {},
    onNavigateToSafeWalk: () -> Unit = {}
) {
    val utilities = listOf(
        UtilityItem(
            title = "Lost & Found",
            description = "Report or find lost items",
            icon = Icons.Outlined.Search,
            color = BhavansPrimary,
            onClick = onNavigateToLostFound
        ),
        UtilityItem(
            title = "Canteen Tracker",
            description = "Check crowd levels",
            icon = Icons.Outlined.Fastfood,
            color = BhavansAccent,
            onClick = onNavigateToCanteen
        ),
        UtilityItem(
            title = "Skill Swap",
            description = "Learn from peers",
            icon = Icons.Outlined.School,
            color = BhavansSecondary,
            onClick = onNavigateToSkillSwap
        ),
        UtilityItem(
            title = "Safe Walk",
            description = "Find a buddy",
            icon = Icons.Outlined.NightsStay,
            color = SuccessColor,
            onClick = onNavigateToSafeWalk
        )
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Student Hub",
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(utilities) { utility ->
                UtilityCard(utility = utility)
            }
        }
    }
}

@Composable
fun UtilityCard(utility: UtilityItem) {
    Card(
        onClick = { utility.onClick?.invoke() },
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = utility.color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = utility.icon,
                contentDescription = utility.title,
                modifier = Modifier.size(40.dp),
                tint = utility.color
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = utility.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = utility.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
