package com.ar.idm.ui.screen.menu.help

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.tooling.preview.Preview
import com.ar.idm.ui.components.DropDown
import com.ar.idm.ui.components.TopBar
import com.ar.idm.ui.navigation.AppDestination
import com.ar.idm.ui.screen.menu.help.components.ContactSupportSection
import com.ar.idm.ui.screen.menu.help.components.FAQSection

@Composable
fun HelpAndFeedbackScreen(
    onSendFeedback: (String) -> Unit,
    onContactSupport: () -> Unit,
    navigateBack: () -> Unit,
    navigate: (AppDestination) -> Unit
) {
    val scrollState  = rememberScrollState()
    var dropDownState by remember { mutableStateOf(false) }
    val dropDownList = listOf(
        Pair( { navigate(AppDestination.TermsAndConditions) }, "Terms & Conditions"),
        Pair( { navigate(AppDestination.PrivacyPolicy) }, "Privacy Policy"),
    )

    Scaffold(
        topBar = {
            TopBar(
                title = "Help & Feedback",
                navigateBack = navigateBack,
                menuAction = { dropDownState = true }
            ){
                DropDown(
                    expanded = dropDownState,
                    list = dropDownList
                ) {
                    dropDownState = false
                }

            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            FAQSection()

            HorizontalDivider()


            ContactSupportSection(
                onEmailClick = {},
                onChatClick = {},
            )

            HorizontalDivider()

        }
    }
}





@Preview(showBackground = true)
@Composable
fun PreviewHelpAndFeedbackScreen(){
    HelpAndFeedbackScreen({},{},{}, {})
}





//@Composable
//fun FeedbackForm(
//    feedbackText: String,
//    onFeedbackChange: (String) -> Unit,
//    onSendFeedback: () -> Unit
//) {
//    Column {
//
////        BasicTextField(
////            value = feedbackText,
////            onValueChange = onFeedbackChange,
////            modifier = Modifier
////                .fillMaxWidth()
////                .height(100.dp)
////                .border(1.dp, Color.Gray)
////                .padding(8.dp),
////            maxLines = 5,
////            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
////            keyboardActions = KeyboardActions(onDone = { onSendFeedback() })
////        )
////        Button(
////            onClick = onSendFeedback,
////            modifier = Modifier.padding(top = 8.dp),
////            enabled = feedbackText.isNotEmpty()
////        ) {
////            Text("Send Feedback")
////        }
//    }
//}




