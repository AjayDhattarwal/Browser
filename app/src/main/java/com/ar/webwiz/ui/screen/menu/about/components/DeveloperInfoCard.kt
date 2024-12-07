package com.ar.webwiz.ui.screen.menu.about.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ar.webwiz.R

@Composable
fun DeveloperInfoCard(
    avatar: Painter,
    developerName: String,
    role: String,
    linkedInUrl: String,
    githubUrl: String,
    otherProjectUrl: String
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2193b0), Color(0xFF6dd5ed))
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Image(
                painter = avatar,
                contentDescription = "Developer Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )

            // Developer Name and Role
            Text(
                text = developerName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = role,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFe0f7fa)
            )

            // Social Media Links
            Spacer(modifier = Modifier.height(10.dp))

            // Link Row
            SocialMediaLink(
                label = "LinkedIn",
                icon = painterResource(id = R.drawable.google_icon),
                url = linkedInUrl,
                context = context
            )
            SocialMediaLink(
                label = "GitHub",
                icon = painterResource(id = R.drawable.google_icon),
                url = githubUrl,
                context = context
            )
            SocialMediaLink(
                label = "Other Projects",
                icon = painterResource(id = R.drawable.google_icon),
                url = otherProjectUrl,
                context = context
            )
        }
    }
}


@Preview
@Composable
fun PreviewCard(){
    DeveloperInfoCard(
        avatar = painterResource(id = R.drawable.google_icon),
        developerName = "John Doe",
        role = "Lead Android Developer",
        linkedInUrl = "https://www.linkedin.com/in/johndoe",
        githubUrl = "https://github.com/johndoe",
        otherProjectUrl = "https://johndoe.com/projects"
    )
}
