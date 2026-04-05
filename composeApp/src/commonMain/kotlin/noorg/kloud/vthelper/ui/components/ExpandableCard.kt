package noorg.kloud.vthelper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skydoves.compose.stability.runtime.TraceRecomposition
import org.jetbrains.compose.resources.painterResource
import vthelper.composeapp.generated.resources.Res
import vthelper.composeapp.generated.resources.keyboard_arrow_left_24px

@Composable
fun BoxScope.ExpandIcon(expandedState: MutableState<Boolean>) {

    var expanded by expandedState

    val arrowAngle by animateFloatAsState(
        targetValue = if (expanded) -90F else 0F,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow) // Same as in expandVertically for consistency
    )

    Icon(
        painter = painterResource(Res.drawable.keyboard_arrow_left_24px),
        modifier = Modifier
            .align(Alignment.TopEnd)
            .rotate(arrowAngle)
            .clip(CircleShape)
            .clickable {
                expanded = !expanded
            }.size(32.dp),
        contentDescription = "expand"
    )
}

@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    internalPadding: Dp = 0.dp,
    expandedByDefault: Boolean = false,
    collapsedContent: @Composable ColumnScope.() -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit,
) {

    val expandedState = remember { mutableStateOf(expandedByDefault) }
    var expanded by expandedState

    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(internalPadding)
                .clickable(enabled = !expanded, interactionSource = null, indication = null) {
                    expanded = true
                },
            contentAlignment = Alignment.CenterStart
        ) {
            ExpandIcon(expandedState)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // https://developer.android.com/develop/ui/compose/animation/composables-modifiers#animatedcontent
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        initialAlpha = 0.3f
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top
                    ) + fadeOut()
                ) {
                    expandedContent()
                }
                AnimatedVisibility(
                    visible = !expanded,
                    enter = expandVertically(
                        expandFrom = Alignment.Bottom
                    ) + fadeIn(
                        initialAlpha = 0.3f
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Bottom
                    ) + fadeOut()
                ) {
                    collapsedContent()
                }
            }
        }
    }
}