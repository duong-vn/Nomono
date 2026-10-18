package com.nomono.sono.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nomono.sono.ui.theme.TerminalOrange
import com.nomono.sono.ui.theme.TerminalOrangeDim
import com.nomono.sono.ui.theme.TerminalOrangeFaint
import com.nomono.sono.ui.theme.TerminalScreen
import com.nomono.sono.ui.theme.VT323FontFamily

@Composable
fun TerminalKeyButton(
    text: String,
    active: Boolean = false,
    color: Color = TerminalOrange,
    activeColor: Color = TerminalOrange,
    borderColor: Color = if (active) TerminalOrange else TerminalOrangeDim,
    fontSize: TextUnit = 11.sp,
    height: Dp = 30.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (active) activeColor else Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(0.dp),
        modifier = modifier.height(height),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text(
                text = text,
                fontFamily = VT323FontFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = if (active) TerminalScreen else color,
            )
        }
    }
}

fun Modifier.drawTerminalPhosphorGrid(): Modifier = drawBehind {
    val step = 24.dp.toPx()
    val lineColor = TerminalOrangeFaint
    var x = 0f
    while (x <= size.width) {
        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += step
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
}

@Composable
fun TerminalSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    extraAction: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = VT323FontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                color = TerminalOrange,
            )
            Text(
                text = "////",
                fontFamily = VT323FontFamily,
                fontSize = 18.sp,
                color = TerminalOrangeDim,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        if (extraAction != null) {
            extraAction()
            Spacer(Modifier.width(6.dp))
        }
        TerminalKeyButton(
            text = "[X]",
            active = false,
            onClick = onDismiss,
        )
    }
}
