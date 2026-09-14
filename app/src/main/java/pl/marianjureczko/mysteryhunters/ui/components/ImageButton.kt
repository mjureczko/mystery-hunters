package pl.marianjureczko.mysteryhunters.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app prefers image buttons over text buttons, this is the one used everywhere.
 *
 * @param description doubles as the test id
 */
@Composable
fun ImageButton(
    drawableId: Int,
    description: String,
    modifier: Modifier = Modifier,
    padding: Dp = 10.dp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(drawableId),
        contentDescription = description,
        contentScale = ContentScale.Inside,
        modifier = modifier
            .padding(padding)
            .alpha(if (enabled) 1f else 0.4f)
            .semantics { contentDescription = description }
            .clickable(enabled = enabled) { onClick() }
    )
}
