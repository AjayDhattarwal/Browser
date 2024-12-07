package com.ar.webwiz.ui.screen.menu.help.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ar.webwiz.R
import com.ar.webwiz.ui.components.CommonItemFrame

@Composable
fun FAQSection() {
    Column {

        Text(
            "Frequently Asked Questions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
        )

        FAQItem(
            question = "How do I enable or disable the ad blocker?",
            answer = "To manage the ad blocker, go to Settings > Privacy > Ad Blocker, where you can toggle it on or off."
        )

        FAQItem(
            question = "How can I block pop-ups?",
            answer = "You can block pop-ups by going to Settings > Privacy > Pop-up Blocker and enabling it. This will prevent unwanted pop-ups while browsing."
        )

        FAQItem(
            question = "How do I manage downloads?",
            answer = "To manage downloads, open the Downloads section from the main menu. Here, you can view, pause, or delete downloaded files."
        )

        FAQItem(
            question = "How can I clear my browsing data?",
            answer = "Go to Settings > Privacy > Clear Browsing Data, where you can select and delete history, cookies, cache, and saved passwords."
        )

        FAQItem(
            question = "How do I customize my homepage?",
            answer = "Go to Settings > Appearance > Homepage, where you can set your preferred homepage URL or choose from frequently visited sites."
        )

        FAQItem(
            question = "How do I save bookmarks?",
            answer = "While on a page, tap the bookmark icon in the toolbar to save the page to your bookmarks. You can view saved bookmarks from the bookmarks section."
        )

        CommonItemFrame(
            image = R.drawable.ic_feedback,
            imageBackground = MaterialTheme.colorScheme.primary.copy(0.3f),
            title = "Submit Feedback",
            titleStyle = MaterialTheme.typography.titleMedium,
            onClick = {},
            subtitle = "Tell us what you think and help us make it better."
        )

    }
}