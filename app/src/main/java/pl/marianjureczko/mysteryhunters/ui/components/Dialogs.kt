package pl.marianjureczko.mysteryhunters.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pl.marianjureczko.mysteryhunters.R
import pl.marianjureczko.mysteryhunters.ui.theme.Shapes
import pl.marianjureczko.mysteryhunters.ui.theme.buttonColors

const val OK_DIALOG = "Ok dialog"
const val YES_NO_DIALOG = "Yes no dialog"

@Composable
fun OkDialog(
    visible: Boolean,
    hideIt: () -> Unit,
    text: String,
    description: String = OK_DIALOG
) {
    if (!visible) {
        return
    }
    AlertDialog(
        onDismissRequest = hideIt,
        modifier = Modifier.semantics { contentDescription = description },
        shape = Shapes.large,
        text = { Text(text) },
        confirmButton = {
            Row(
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(shape = Shapes.large, colors = buttonColors(), onClick = hideIt) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        }
    )
}

@Composable
fun YesNoDialog(
    visible: Boolean,
    hideIt: () -> Unit,
    text: String,
    description: String = YES_NO_DIALOG,
    onConfirm: () -> Unit
) {
    if (!visible) {
        return
    }
    AlertDialog(
        onDismissRequest = hideIt,
        modifier = Modifier.semantics { contentDescription = description },
        shape = Shapes.large,
        text = { Text(text) },
        confirmButton = {
            Button(shape = Shapes.large, colors = buttonColors(), onClick = onConfirm) {
                Text(text = stringResource(R.string.yes))
            }
        },
        dismissButton = {
            Button(shape = Shapes.large, colors = buttonColors(), onClick = hideIt) {
                Text(text = stringResource(R.string.no))
            }
        }
    )
}
