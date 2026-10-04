package com.nomono.sono.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nomono.sono.R
import com.nomono.sono.data.Debt
import com.nomono.sono.data.DebtRepository
import com.nomono.sono.data.DebtTransaction
import com.nomono.sono.data.DebtType
import com.nomono.sono.data.ThemeMode
import com.nomono.sono.data.ThemePreferences
import com.nomono.sono.data.TransactionKind
import com.nomono.sono.ui.components.Avatar
import com.nomono.sono.ui.components.GlassCard
import com.nomono.sono.ui.edit.DebtEditorSheet
import com.nomono.sono.ui.theme.LocalSonoColors
import com.nomono.sono.ui.theme.TerminalInk
import com.nomono.sono.ui.theme.TerminalNeg
import com.nomono.sono.ui.theme.TerminalOrange
import com.nomono.sono.ui.theme.TerminalOrangeDim
import com.nomono.sono.ui.theme.TerminalOrangeFaint
import com.nomono.sono.ui.theme.TerminalPage
import com.nomono.sono.ui.theme.TerminalScreen
import com.nomono.sono.ui.theme.VT323FontFamily
import com.nomono.sono.util.CalculatorOperator
import com.nomono.sono.util.CalculatorResult
import com.nomono.sono.util.DebtTotals
import com.nomono.sono.util.VndFormat
import com.nomono.sono.util.applyCalculatorOperation
import com.nomono.sono.util.formatCalculatorValue
import com.nomono.sono.util.parseCalculatorInput
import kotlinx.coroutines.launch
import java.util.Calendar

private val StepOptions = listOf(
    Pair(10_000L, 50_000L),
    Pair(100_000L, 500_000L),
    Pair(1_000L, 10_000L),
)

private fun formatK(amount: Long): String {
    return if (amount >= 1_000_000L) {
        val millions = amount / 1_000_000.0
        if (millions == millions.toLong().toDouble()) "${millions.toLong()}tr" else "${millions}tr"
    } else {
        "${amount / 1_000L}k"
    }
}

private fun getVietnameseDayOfWeek(): String {
    val cal = Calendar.getInstance()
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "CHỦ NHẬT"
        Calendar.MONDAY -> "THỨ HAI"
        Calendar.TUESDAY -> "THỨ BA"
        Calendar.WEDNESDAY -> "THỨ TƯ"
        Calendar.THURSDAY -> "THỨ NĂM"
        Calendar.FRIDAY -> "THỨ SÁU"
        Calendar.SATURDAY -> "THỨ BẢY"
        else -> ""
    }
}

private fun getFormattedDate(): String {
    val cal = Calendar.getInstance()
    val d = cal.get(Calendar.DAY_OF_MONTH)
    val m = cal.get(Calendar.MONTH) + 1
    val y = cal.get(Calendar.YEAR)
    return "%02d.%02d.%04d".format(d, m, y)
}

@Composable
fun HomeScreen(
    repository: DebtRepository,
    themePreferences: ThemePreferences,
    themeMode: ThemeMode,
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
    val state by viewModel.uiState.collectAsState()

    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var showCalculator by rememberSaveable { mutableStateOf(false) }
    var searchActive by rememberSaveable { mutableStateOf(false) }

    // Selected row index for Terminal Mode
    var selectedDebtId by rememberSaveable { mutableStateOf<Long?>(null) }
    var stepModeIndex by rememberSaveable { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    val editingDebt: Debt? = remember(editingId, state.debts) {
        editingId?.let { id -> state.debts.firstOrNull { it.id == id } }
    }

    val editingTransactions by produceState(
        initialValue = emptyList<DebtTransaction>(),
        editingId,
        showEditor,
    ) {
        val id = editingId
        if (id == null || !showEditor) {
            value = emptyList()
        } else {
            repository.observeTransactions(id).collect { value = it }
        }
    }

    fun openAdd() {
        editingId = null
        showEditor = true
    }

    fun openEdit(debt: Debt) {
        editingId = debt.id
        showEditor = true
    }

    val isSystemDark = isSystemInDarkTheme()
    val isTerminal = when (themeMode) {
        ThemeMode.TERMINAL -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }

    if (isTerminal) {
        TerminalHomeScreen(
            state = state,
            themeMode = themeMode,
            searchActive = searchActive,
            selectedDebtId = selectedDebtId,
            stepModeIndex = stepModeIndex,
            onSearchToggle = {
                searchActive = !searchActive
                if (!searchActive) viewModel.setSearchQuery("")
            },
            onSearchQueryChange = viewModel::setSearchQuery,
            onCalculatorClick = { showCalculator = true },
            onThemeChange = { mode -> scope.launch { themePreferences.setThemeMode(mode) } },
            onFilterSelect = viewModel::setDebtFilter,
            onSortSelect = viewModel::setSortMode,
            onCycleStep = {
                stepModeIndex = (stepModeIndex + 1) % StepOptions.size
            },
            onSelectDebt = { debt -> selectedDebtId = debt.id },
            onAdjustDebt = viewModel::adjustDebt,
            onOpenEdit = ::openEdit,
            onOpenAdd = ::openAdd,
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = ::openAdd,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Thêm nợ", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                )
            },
        ) { padding ->
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(LocalSonoColors.current.backgroundGradient)
                        .padding(padding),
                ) {
                    HomeTopBar(
                        themeMode = themeMode,
                        searchActive = searchActive,
                        onSearchToggle = {
                            searchActive = !searchActive
                            if (!searchActive) viewModel.setSearchQuery("")
                        },
                        onCalculatorClick = { showCalculator = true },
                        onThemeChange = { mode -> scope.launch { themePreferences.setThemeMode(mode) } },
                    )

                    AnimatedVisibility(
                        visible = searchActive,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        SearchField(
                            query = state.searchQuery,
                            onQueryChange = viewModel::setSearchQuery,
                            onClose = {
                                searchActive = false
                                viewModel.setSearchQuery("")
                            },
                        )
                    }

                    SummaryHeader(totals = state.totals, hasAny = state.debts.isNotEmpty())

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterButton(current = state.debtFilter, onSelect = viewModel::setDebtFilter)
                        SortButton(current = state.sortMode, onSelect = viewModel::setSortMode)
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${state.debts.size} người",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (state.debts.isEmpty()) {
                        EmptyState(query = state.searchQuery, filter = state.debtFilter, onAdd = ::openAdd)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            itemsIndexed(state.debts, key = { _, it -> it.id }) { _, debt ->
                                DebtRow(
                                    debt = debt,
                                    onClick = { openEdit(debt) },
                                    onAdjustDebt = viewModel::adjustDebt,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCalculator) {
        CalculatorSheet(onDismiss = { showCalculator = false })
    }

    if (showEditor) {
        DebtEditorSheet(
            debt = editingDebt,
            transactions = editingTransactions,
            onDismiss = { showEditor = false },
            onSave = { name, amount, type, avatarUri ->
                scope.launch {
                    val id = editingId
                    if (id == null) {
                        viewModel.add(name, amount, type, avatarUri)
                    } else {
                        viewModel.update(id, name, amount, type, avatarUri)
                    }
                }
                showEditor = false
            },
            onClearRequest = { debt ->
                scope.launch { viewModel.clear(debt.id) }
                showEditor = false
            },
            onDeleteRequest = { debt ->
                scope.launch { viewModel.delete(debt.id) }
                showEditor = false
            },
            onRecordTransaction = { debtId, amount, kind ->
                viewModel.recordTransaction(debtId, amount, kind)
            },
            onDeleteTransaction = viewModel::deleteTransaction,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TERMINAL CRT PHOSPHOR THEME (SỔ NỢ HTML REPLICA)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TerminalHomeScreen(
    state: HomeUiState,
    themeMode: ThemeMode,
    searchActive: Boolean,
    selectedDebtId: Long?,
    stepModeIndex: Int,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCalculatorClick: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onFilterSelect: (DebtFilter) -> Unit,
    onSortSelect: (SortMode) -> Unit,
    onCycleStep: () -> Unit,
    onSelectDebt: (Debt) -> Unit,
    onAdjustDebt: (Debt, Long) -> Unit,
    onOpenEdit: (Debt) -> Unit,
    onOpenAdd: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val currentStep = StepOptions[stepModeIndex]
    val stepLabel = "${formatK(currentStep.first)}/${formatK(currentStep.second)}"

    val infiniteTransition = rememberInfiniteTransition(label = "terminalCursor")
    val cursorVisible by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1100
                1f at 0
                1f at 550
                0f at 551
                0f at 1100
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "cursorBlink",
    )

    // Ensure selectedDebtId points to an active debt if available
    val activeSelectedId = remember(selectedDebtId, state.debts) {
        if (state.debts.any { it.id == selectedDebtId }) selectedDebtId else state.debts.firstOrNull()?.id
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalPage)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // CRT Screen Container with 24dp grid pattern & double borders
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .background(TerminalScreen)
                .drawBehind {
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
                .border(2.dp, TerminalOrange)
                .padding(4.dp)
                .border(1.dp, TerminalOrangeDim)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            // ── Top Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SỔ NỢ",
                            fontFamily = VT323FontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp,
                            color = TerminalOrange,
                        )
                        Text(
                            text = "////",
                            fontFamily = VT323FontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            color = TerminalOrangeDim,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                    Text(
                        text = getVietnameseDayOfWeek(),
                        fontFamily = VT323FontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = TerminalOrangeDim,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Search toggle button
                    TerminalKeyButton(
                        text = if (searchActive) "[X]" else "TÌM",
                        active = searchActive,
                        onClick = onSearchToggle,
                    )

                    // Calculator button
                    TerminalKeyButton(
                        text = "MÁY TÍNH",
                        active = false,
                        onClick = onCalculatorClick,
                    )

                    // Theme button
                    TerminalThemeMenuButton(
                        current = themeMode,
                        onSelect = onThemeChange,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Search Input ──
            if (searchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TerminalOrangeDim)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "> ",
                        fontFamily = VT323FontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TerminalOrangeDim,
                    )
                    BasicTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = VT323FontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = TerminalInk,
                        ),
                        cursorBrush = SolidColor(TerminalOrange),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (state.searchQuery.isEmpty()) {
                                Text(
                                    text = "NHẬP TÊN...",
                                    fontFamily = VT323FontFamily,
                                    fontSize = 13.sp,
                                    color = TerminalOrangeDim,
                                )
                            }
                            innerTextField()
                        },
                    )
                    if (state.searchQuery.isNotEmpty()) {
                        Text(
                            text = "[XÓA]",
                            fontFamily = VT323FontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TerminalOrange,
                            modifier = Modifier.clickable { onSearchQueryChange("") },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Readout Summary ──
            val net = state.totals.net
            val netIsNeg = net < 0
            val netColor = if (netIsNeg) TerminalNeg else TerminalOrange
            val netText = when {
                net > 0 -> "+${VndFormat.format(net)}"
                net < 0 -> "−${VndFormat.format(-net)}"
                else -> "0 ₫"
            }
            val netHint = when {
                net > 0 -> "BẠN ĐANG CHO VAY RÒNG"
                net < 0 -> "BẠN ĐANG NỢ RÒNG"
                else -> "KHÔNG AI NỢ AI"
            }

            Text(
                text = "CHÊNH LỆCH",
                fontFamily = VT323FontFamily,
                fontSize = 11.sp,
                letterSpacing = 1.8.sp,
                color = TerminalOrangeDim,
            )

            Text(
                text = netText,
                fontFamily = VT323FontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = netColor,
                lineHeight = 36.sp,
            )

            Text(
                text = netHint,
                fontFamily = VT323FontFamily,
                fontSize = 11.5.sp,
                letterSpacing = 0.8.sp,
                color = TerminalOrangeDim,
            )

            Spacer(Modifier.height(6.dp))

            // ── 28-cell bar ──
            TerminalCellBar(
                pos = state.totals.oweMe,
                neg = state.totals.iOwe,
            )

            Spacer(Modifier.height(8.dp))

            // ── Pair numbers ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // dashed line at top
                        drawLine(
                            color = TerminalOrangeDim,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1f,
                        )
                    }
                    .padding(top = 6.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HỌ NỢ BẠN",
                        fontFamily = VT323FontFamily,
                        fontSize = 10.5.sp,
                        letterSpacing = 1.2.sp,
                        color = TerminalOrangeDim,
                    )
                    Text(
                        text = "+${VndFormat.format(state.totals.oweMe)}",
                        fontFamily = VT323FontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalOrange,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = "BẠN NỢ",
                        fontFamily = VT323FontFamily,
                        fontSize = 10.5.sp,
                        letterSpacing = 1.2.sp,
                        color = TerminalOrangeDim,
                    )
                    Text(
                        text = "−${VndFormat.format(state.totals.iOwe)}",
                        fontFamily = VT323FontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalNeg,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Divider ──
            Text(
                text = "////////////////////////////////////////////////////////////",
                fontFamily = VT323FontFamily,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = TerminalOrangeDim,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )

            Spacer(Modifier.height(4.dp))

            // ── Tabs ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TerminalTabItem(
                    title = "TẤT CẢ",
                    selected = state.debtFilter == DebtFilter.ALL,
                    onClick = { onFilterSelect(DebtFilter.ALL) },
                )
                Spacer(Modifier.width(8.dp))
                TerminalTabItem(
                    title = "HỌ NỢ BẠN",
                    selected = state.debtFilter == DebtFilter.THEY_OWE_ME,
                    onClick = { onFilterSelect(DebtFilter.THEY_OWE_ME) },
                )
                Spacer(Modifier.width(8.dp))
                TerminalTabItem(
                    title = "BẠN NỢ",
                    selected = state.debtFilter == DebtFilter.I_OWE_THEM,
                    onClick = { onFilterSelect(DebtFilter.I_OWE_THEM) },
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${state.debts.size} NGƯỜI",
                    fontFamily = VT323FontFamily,
                    fontSize = 11.sp,
                    color = TerminalOrangeDim,
                    letterSpacing = 1.sp,
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Options Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var sortMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    Text(
                        text = "XẾP: [${state.sortMode.label.uppercase()}]",
                        fontFamily = VT323FontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = TerminalOrangeDim,
                        modifier = Modifier.clickable { sortMenuExpanded = true },
                    )
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                    ) {
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = mode.label.uppercase(),
                                        fontFamily = VT323FontFamily,
                                        fontWeight = if (mode == state.sortMode) FontWeight.Bold else FontWeight.Normal,
                                    )
                                },
                                onClick = {
                                    sortMenuExpanded = false
                                    onSortSelect(mode)
                                },
                            )
                        }
                    }
                }

                Text(
                    text = "MỨC BẤM: [$stepLabel]",
                    fontFamily = VT323FontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = TerminalOrange,
                    modifier = Modifier.clickable { onCycleStep() },
                )
            }

            Spacer(Modifier.height(6.dp))

            // ── Debt List ──
            if (state.debts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (state.searchQuery.isNotBlank()) "KHÔNG CÓ AI KHỚP BỘ LỌC" else "CHƯA CÓ AI. BẤM THÊM NGƯỜI ĐỂ BẮT ĐẦU",
                        fontFamily = VT323FontFamily,
                        fontSize = 12.sp,
                        color = TerminalOrangeDim,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    itemsIndexed(state.debts, key = { _, debt -> debt.id }) { index, debt ->
                        val isSelected = debt.id == activeSelectedId
                        TerminalDebtRow(
                            index = index + 1,
                            debt = debt,
                            isSelected = isSelected,
                            stepFirst = currentStep.first,
                            stepSecond = currentStep.second,
                            onClick = { onSelectDebt(debt) },
                            onAdjust = { delta ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAdjustDebt(debt, delta)
                            },
                            onOpenEdit = { onOpenEdit(debt) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── Status Line ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, TerminalOrangeDim), shape = RoundedCornerShape(0.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = getFormattedDate(),
                    fontFamily = VT323FontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = TerminalOrange,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CÒN LẠI $netText",
                        fontFamily = VT323FontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalOrange,
                    )
                    // Blinking cursor
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(width = 7.dp, height = 12.dp)
                            .background(if (cursorVisible > 0.5f) TerminalOrange else Color.Transparent),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Bottom Dock: Add Person Button ──
            Surface(
                onClick = onOpenAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                color = TerminalPage,
                border = BorderStroke(2.dp, TerminalOrange),
                shape = RoundedCornerShape(0.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = "+ THÊM NGƯỜI",
                        fontFamily = VT323FontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp,
                        color = TerminalOrange,
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalKeyButton(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (active) TerminalOrange else Color.Transparent,
        border = BorderStroke(1.dp, if (active) TerminalOrange else TerminalOrangeDim),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.height(30.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text(
                text = text,
                fontFamily = VT323FontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = if (active) TerminalScreen else TerminalOrange,
            )
        }
    }
}

@Composable
private fun TerminalThemeMenuButton(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
            color = Color.Transparent,
            border = BorderStroke(1.dp, TerminalOrangeDim),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.height(30.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = "THEME",
                    fontFamily = VT323FontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = TerminalOrange,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            val options = listOf(
                ThemeOption(ThemeMode.TERMINAL, "Terminal CRT", R.drawable.ic_terminal),
                ThemeOption(ThemeMode.LIGHT, "Sáng", R.drawable.ic_light_mode),
                ThemeOption(ThemeMode.SYSTEM, "Theo hệ thống", R.drawable.ic_theme_system),
            )
            options.forEach { option ->
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(option.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.label,
                                fontFamily = VT323FontFamily,
                                modifier = Modifier.weight(1f),
                                fontWeight = if (option.mode == current) FontWeight.Bold else FontWeight.Normal,
                            )
                            if (option.mode == current) {
                                Text("✓", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(option.mode)
                    },
                )
            }
        }
    }
}

@Composable
private fun TerminalCellBar(pos: Long, neg: Long) {
    val total = pos + neg
    val nCells = 28
    var nPos = if (total > 0L) ((pos.toDouble() / total) * nCells).toInt() else 0
    if (total > 0L && pos > 0L && nPos == 0) nPos = 1
    if (total > 0L && neg > 0L && nPos == nCells) nPos = nCells - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(11.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        for (i in 0 until nCells) {
            val cellBg = when {
                total == 0L -> TerminalOrangeFaint
                i < nPos -> TerminalOrange
                else -> TerminalNeg
            }
            val cellBorder = when {
                total == 0L -> TerminalOrangeDim
                i < nPos -> TerminalOrange
                else -> TerminalNeg
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(cellBg)
                    .border(0.8.dp, cellBorder),
            )
        }
    }
}

@Composable
private fun TerminalTabItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = if (selected) "[$title]" else title,
        fontFamily = VT323FontFamily,
        fontSize = 11.5.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        letterSpacing = 0.8.sp,
        color = if (selected) TerminalOrange else TerminalOrangeDim,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun TerminalDebtRow(
    index: Int,
    debt: Debt,
    isSelected: Boolean,
    stepFirst: Long,
    stepSecond: Long,
    onClick: () -> Unit,
    onAdjust: (Long) -> Unit,
    onOpenEdit: () -> Unit,
) {
    val isSettled = debt.amount == 0L
    val isOweMe = debt.debtType == DebtType.THEY_OWE_ME
    val isNeg = !isSettled && !isOweMe
    val glyph = when {
        isSettled -> ""
        isOweMe -> "▲ "
        else -> "▼ "
    }
    val amtText = when {
        isSettled -> "0 ₫"
        isOweMe -> "+${VndFormat.format(debt.amount)}"
        else -> "−${VndFormat.format(debt.amount)}"
    }
    val stateLabel = when {
        isSettled -> "KHÔNG NỢ NẦN GÌ"
        isOweMe -> "NỢ BẠN ${VndFormat.format(debt.amount)}"
        else -> "BẠN NỢ ${VndFormat.format(debt.amount)}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color(0x669A4A17),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1f,
                )
            }
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Index & selector pointer
            val pointer = if (isSelected) "▸ " else "  "
            val indexFormatted = "%02d".format(index)
            Text(
                text = "$pointer$indexFormatted",
                fontFamily = VT323FontFamily,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) TerminalOrange else TerminalOrangeDim,
            )

            Spacer(Modifier.width(6.dp))

            // Name
            Text(
                text = debt.name.uppercase(),
                fontFamily = VT323FontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp,
                textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None,
                color = if (isSelected) TerminalOrange else TerminalOrangeDim,
                maxLines = 1,
            )

            Spacer(Modifier.width(6.dp))

            // Dotted Leader Line (kéo dài nối tên với số tiền)
            Text(
                text = "......................................................................",
                fontFamily = VT323FontFamily,
                fontSize = 11.sp,
                color = TerminalOrangeDim,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.weight(1f),
            )

            Spacer(Modifier.width(6.dp))

            // Amount
            Text(
                text = "$glyph$amtText",
                fontFamily = VT323FontFamily,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNeg) TerminalNeg else if (isSelected) TerminalOrange else TerminalOrange.copy(alpha = 0.8f),
            )
        }

        // Expanded Control Panel when selected
        if (isSelected) {
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 2.dp, bottom = 4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stateLabel,
                        fontFamily = VT323FontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = TerminalOrangeDim,
                    )

                    // Edit button to open details
                    Text(
                        text = "[SỬA]",
                        fontFamily = VT323FontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalOrange,
                        modifier = Modifier.clickable(onClick = onOpenEdit),
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TerminalActButton(
                        label = "−${formatK(stepSecond)}",
                        isPlus = false,
                        modifier = Modifier.weight(1f),
                        onClick = { onAdjust(-stepSecond) },
                    )
                    TerminalActButton(
                        label = "−${formatK(stepFirst)}",
                        isPlus = false,
                        modifier = Modifier.weight(1f),
                        onClick = { onAdjust(-stepFirst) },
                    )
                    TerminalActButton(
                        label = "+${formatK(stepFirst)}",
                        isPlus = true,
                        modifier = Modifier.weight(1f),
                        onClick = { onAdjust(stepFirst) },
                    )
                    TerminalActButton(
                        label = "+${formatK(stepSecond)}",
                        isPlus = true,
                        modifier = Modifier.weight(1f),
                        onClick = { onAdjust(stepSecond) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalActButton(
    label: String,
    isPlus: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val borderColor = if (isPlus) TerminalOrange else TerminalNeg
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(0.dp),
        modifier = modifier.height(38.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = label,
                fontFamily = VT323FontFamily,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = borderColor,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REGULAR MODERN / LOKI THEME COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(
    themeMode: ThemeMode,
    searchActive: Boolean,
    onSearchToggle: () -> Unit,
    onCalculatorClick: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
) {
    var themeMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LocalSonoColors.current.glassColor)
            .border(1.dp, LocalSonoColors.current.glassBorder, RoundedCornerShape(20.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "⚡ SỔ NỢ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onSearchToggle) {
            Icon(
                imageVector = if (searchActive) Icons.Filled.Close else Icons.Filled.Search,
                contentDescription = if (searchActive) "Đóng tìm kiếm" else "Tìm kiếm",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        IconButton(
            onClick = onCalculatorClick,
            modifier = Modifier.semantics { contentDescription = "Máy tính" },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_calculator),
                contentDescription = "Máy tính",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }

        Box {
            val (iconRes, desc) = when (themeMode) {
                ThemeMode.TERMINAL -> R.drawable.ic_terminal to "Terminal CRT"
                ThemeMode.LIGHT -> R.drawable.ic_light_mode to "Chế độ sáng"
                ThemeMode.SYSTEM -> R.drawable.ic_theme_system to "Theo hệ thống"
            }
            IconButton(onClick = { themeMenuOpen = true }) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = desc,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            ThemeMenu(
                expanded = themeMenuOpen,
                current = themeMode,
                onDismiss = { themeMenuOpen = false },
                onSelect = onThemeChange,
            )
        }
    }
}

private val CalculatorRows = listOf(
    listOf("AC", "⌫", "±", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "−"),
    listOf("1", "2", "3", "+"),
    listOf("0", ".", "=", ""),
)
private val CalculatorEmphasized = setOf("÷", "×", "−", "+", "=")

@Composable
private fun CalculatorSheet(onDismiss: () -> Unit) {
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var accumulator by remember { mutableStateOf<java.math.BigDecimal?>(null) }
    var pendingOperator by remember { mutableStateOf<CalculatorOperator?>(null) }
    var replaceDisplay by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun reset() {
        display = "0"
        expression = ""
        accumulator = null
        pendingOperator = null
        replaceDisplay = false
        error = null
    }

    fun prepareInput() {
        if (error != null || (replaceDisplay && pendingOperator == null)) {
            accumulator = null
            pendingOperator = null
            expression = ""
            error = null
        }
        if (replaceDisplay) display = "0"
        replaceDisplay = false
    }

    fun inputDigit(digit: String) {
        prepareInput()
        val candidate = if (display == "0") digit else display + digit
        if (candidate.substringBefore('.').removePrefix("-").length <= 15) display = candidate
    }

    fun inputDecimal() {
        prepareInput()
        if ('.' !in display) display += "."
    }

    fun toggleSign() {
        if (error != null) return
        display = when (display) {
            "0" -> "0"
            else -> if (display.startsWith('-')) display.drop(1) else "-$display"
        }
    }

    fun apply(operator: CalculatorOperator?) {
        val currentText = display.removeSuffix(".")
        val current = parseCalculatorInput(currentText) ?: return
        val previous = accumulator
        val pending = pendingOperator
        if (previous != null && pending != null) {
            val currentExpression = "$expression $currentText"
            when (val result = applyCalculatorOperation(previous, pending, current)) {
                is CalculatorResult.Error -> {
                    error = result.message
                    return
                }
                is CalculatorResult.Value -> {
                    accumulator = result.value
                    display = formatCalculatorValue(result.value)
                    expression = if (operator == null) "$currentExpression =" else "${formatCalculatorValue(result.value)} ${operator.symbol}"
                }
            }
        } else {
            accumulator = current
            expression = if (operator == null) "" else "$currentText ${operator.symbol}"
        }
        error = null
        pendingOperator = operator
        replaceDisplay = true
        if (operator == null) accumulator = null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Máy tính",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Đóng máy tính",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = expression,
                    style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error ?: display,
                    style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"),
                    color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(24.dp))
            CalculatorRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(Modifier.weight(1f))
                        } else {
                            CalculatorKey(
                                label = key,
                                emphasized = key in CalculatorEmphasized,
                                onClick = {
                                    when (key) {
                                        "AC" -> reset()
                                        "⌫" -> if (error == null && !replaceDisplay) {
                                            display = display.dropLast(1).ifEmpty { "0" }
                                        }
                                        "±" -> toggleSign()
                                        "." -> inputDecimal()
                                        "÷" -> apply(CalculatorOperator.DIVIDE)
                                        "×" -> apply(CalculatorOperator.MULTIPLY)
                                        "−" -> apply(CalculatorOperator.SUBTRACT)
                                        "+" -> apply(CalculatorOperator.ADD)
                                        "=" -> apply(null)
                                        else -> inputDigit(key)
                                    }
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun RowScope.CalculatorKey(label: String, emphasized: Boolean, onClick: () -> Unit) {
    val modifier = Modifier
        .weight(1f)
        .height(64.dp)
    if (emphasized) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ),
        ) {
            Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    } else {
        FilledTonalButton(onClick = onClick, modifier = modifier) {
            Text(label, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

private data class ThemeOption(val mode: ThemeMode, val label: String, val iconRes: Int)

@Composable
private fun ThemeMenu(
    expanded: Boolean,
    current: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeOption(ThemeMode.TERMINAL, "Terminal CRT", R.drawable.ic_terminal),
        ThemeOption(ThemeMode.LIGHT, "Sáng", R.drawable.ic_light_mode),
        ThemeOption(ThemeMode.SYSTEM, "Theo hệ thống", R.drawable.ic_theme_system),
    )

    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        options.forEach { option ->
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(option.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (option.mode == current) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = option.label,
                            modifier = Modifier.weight(1f),
                            fontWeight = if (option.mode == current) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (option.mode == current) {
                            Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                onClick = {
                    onDismiss()
                    if (option.mode != current) onSelect(option.mode)
                },
            )
        }
    }
}

@Composable
private fun FilterButton(current: DebtFilter, onSelect: (DebtFilter) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(current.label, fontWeight = FontWeight.Bold) },
            trailingIcon = {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                trailingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            border = BorderStroke(1.dp, LocalSonoColors.current.glassBorder),
            shape = RoundedCornerShape(12.dp),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DebtFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                filter.label,
                                Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (filter == current) FontWeight.Bold else FontWeight.Normal,
                            )
                            if (filter == current) {
                                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(filter)
                    },
                )
            }
        }
    }
}

@Composable
private fun SortButton(current: SortMode, onSelect: (SortMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(current.label, fontWeight = FontWeight.SemiBold) },
            trailingIcon = {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = LocalSonoColors.current.glassColor,
                labelColor = MaterialTheme.colorScheme.onSurface,
                trailingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            border = BorderStroke(1.dp, LocalSonoColors.current.glassBorder),
            shape = RoundedCornerShape(12.dp),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                mode.label,
                                Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (mode == current) FontWeight.Bold else FontWeight.Normal,
                            )
                            if (mode == current) {
                                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(mode)
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .focusRequester(focusRequester),
        placeholder = { Text("Tìm theo tên...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Xóa tìm kiếm", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = LocalSonoColors.current.glassColor,
            unfocusedContainerColor = LocalSonoColors.current.glassColor,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = LocalSonoColors.current.glassBorder,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun SummaryHeader(totals: DebtTotals, hasAny: Boolean) {
    if (!hasAny) return
    val sono = LocalSonoColors.current
    val net = totals.net
    val netColor = when {
        net > 0 -> sono.oweMe
        net < 0 -> sono.iOwe
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val netText = when {
        net > 0 -> "+${VndFormat.format(net)}"
        net < 0 -> "−${VndFormat.format(-net)}"
        else -> "0 ₫"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(sono.glassColor)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        sono.glassBorder,
                        sono.glassGoldBorder,
                        sono.glassBorder,
                    ),
                ),
                shape = RoundedCornerShape(22.dp),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Họ nợ bạn",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "+${VndFormat.format(totals.oweMe)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Black,
                    color = sono.oweMe,
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(sono.glassBorder),
            )

            Column {
                Text(
                    text = "Bạn nợ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "−${VndFormat.format(totals.iOwe)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Black,
                    color = sono.iOwe,
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(sono.glassBorder),
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Chênh lệch",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = netText,
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Black,
                    color = netColor,
                )
            }
        }
    }
}

@Composable
private fun DebtRow(
    debt: Debt,
    onClick: () -> Unit,
    onAdjustDebt: (Debt, Long) -> Unit,
) {
    val sono = LocalSonoColors.current
    val haptic = LocalHapticFeedback.current
    val isSettled = debt.amount == 0L
    val isOweMe = debt.debtType == DebtType.THEY_OWE_ME

    val color = when {
        isSettled -> MaterialTheme.colorScheme.onSurfaceVariant
        isOweMe -> sono.oweMe
        else -> sono.iOwe
    }
    val direction = when {
        isSettled -> "Không nợ nần gì"
        isOweMe -> "Họ nợ bạn"
        else -> "Bạn nợ"
    }
    val amountText = when {
        isSettled -> "0 ₫"
        isOweMe -> "+${VndFormat.format(debt.amount)}"
        else -> "−${VndFormat.format(debt.amount)}"
    }

    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        glassColor = sono.glassColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        borderColor = sono.glassBorder,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(name = debt.name, uri = debt.avatarUri, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = debt.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Black,
                        color = color,
                        maxLines = 1,
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = direction,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = color.copy(alpha = 0.9f),
                        maxLines = 1,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        QuickMiniButton(
                            label = "−10k",
                            contentColor = sono.iOwe,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAdjustDebt(debt, -10_000L)
                            },
                        )
                        QuickMiniButton(
                            label = "−1k",
                            contentColor = sono.iOwe,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAdjustDebt(debt, -1_000L)
                            },
                        )
                        QuickMiniButton(
                            label = "+1k",
                            contentColor = sono.oweMe,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAdjustDebt(debt, 1_000L)
                            },
                        )
                        QuickMiniButton(
                            label = "+10k",
                            contentColor = sono.oweMe,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAdjustDebt(debt, 10_000L)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickMiniButton(
    label: String,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = contentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.35f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .height(28.dp)
            .width(40.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun EmptyState(query: String, filter: DebtFilter, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        ),
                    ),
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                        ),
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "₫",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Spacer(Modifier.height(24.dp))
        val (title, subtitle) = if (query.isNotBlank()) {
            "Không tìm thấy kết quả" to "Thử tìm kiếm với từ khóa khác."
        } else when (filter) {
            DebtFilter.THEY_OWE_ME -> "Không có khoản họ nợ bạn" to "Chuyển bộ lọc để kiểm tra các khoản nợ khác."
            DebtFilter.I_OWE_THEM -> "Không có khoản bạn nợ" to "Chuyển bộ lọc để kiểm tra các khoản nợ khác."
            DebtFilter.SETTLED -> "Chưa có khoản tất toán" to "Khoản nợ 0 ₫ sẽ xuất hiện ở đây."
            DebtFilter.ALL -> "Chưa có khoản nợ nào" to "Bấm nút bên dưới để ghi nhận người đầu tiên."
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (query.isBlank() && filter == DebtFilter.ALL) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Thêm khoản nợ", fontWeight = FontWeight.Bold)
            }
        }
    }
}
