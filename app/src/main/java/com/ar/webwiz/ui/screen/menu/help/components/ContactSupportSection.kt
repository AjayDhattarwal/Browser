package com.ar.webwiz.ui.screen.menu.help.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun ContactSupportSection(
    onEmailClick: () -> Unit,
    onChatClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Contact Support",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        SupportOptionCard(
            title = "Chat ",
            description = "Need quick answers? Chat with our AI bot for assistance.",
            onClick = onChatClick
        )

        SupportOptionCard(
            title = "Email Support",
            description = "Send us an email, and we'll respond as soon as possible.",
            onClick = onEmailClick
        )



    }
}