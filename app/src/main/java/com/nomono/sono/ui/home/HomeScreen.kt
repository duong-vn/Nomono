package com.nomono.sono.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nomono.sono.data.Debt
import com.nomono.sono.data.DebtRepository
import com.nomono.sono.data.DebtTransaction
import com.nomono.sono.data.DebtType
import com.nomono.sono.data.ThemeMode
import com.nomono.sono.data.ThemePreferences
import com.nomono.sono.data.TransactionKind
import com.nomono.sono.ui.components.Avatar
import com.nomono.sono.ui.edit.DebtEditorSheet
import com.nomono.sono.ui.theme.LocalSonoColors
import com.nomono.sono.util.DebtTotals
import com.nomono.sono.util.VndFormat
import kotlinx.coroutines.launch

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
    var searchActive by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val editingDebt: Debt? = editingId?.let { id -> state.debts.firstOrNull { it.id == id } }

    val editingTransactions by produceState(initialValue = emptyList<DebtTransaction>(), editingId) {
        val id = editingId
        if (id == null) {
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = ::openAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm khoản nợ")
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            HomeTopBar(
                themeMode = themeMode,
                searchActive = searchActive,
                onSearchToggle = {
                    searchActive = !searchActive
                    if (!searchActive) viewModel.setSearchQuery("")
                },
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
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SortButton(current = state.sortMode, onSelect = viewModel::setSortMode)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${state.debts.size} người",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (state.debts.isEmpty()) {
                EmptyState(query = state.searchQuery, onAdd = ::openAdd)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    items(state.debts, key = { it.id }) { debt ->
                        DebtRow(debt = debt, onClick = { openEdit(debt) })
                    }
                }
            }
        }
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
        )
    }
}

@Composable
private fun HomeTopBar(
    themeMode: ThemeMode,
    searchActive: Boolean,
    onSearchToggle: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
) {
    var themeMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Sổ Nợ",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Spacer(Modifier.weight(1f))

        IconButton(onClick = onSearchToggle) {
            Icon(
                imageVector = if (searchActive) Icons.Filled.Close else Icons.Filled.Search,
                contentDescription = if (searchActive) "Đóng tìm kiếm" else "Tìm kiếm",
            )
        }

        Box {
            IconButton(onClick = { themeMenuOpen = true }) {
                Icon(Icons.Filled.Settings, contentDescription = "Giao diện")
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

@Composable
private fun ThemeMenu(
    expanded: Boolean,
    current: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeMode.SYSTEM to "Theo hệ thống",
        ThemeMode.LIGHT to "Sáng",
        ThemeMode.DARK to "Tối",
    )
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        options.forEach { (mode, label) ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(label, Modifier.weight(1f))
                        if (mode == current) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                onClick = {
                    onDismiss()
                    if (mode != current) onSelect(mode)
                },
            )
        }
    }
}

@Composable
private fun SortButton(current: SortMode, onSelect: (SortMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(current.label)
            Spacer(Modifier.width(2.dp))
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(mode.label, Modifier.weight(1f))
                            if (mode == current) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
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
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .focusRequester(focusRequester),
        placeholder = { Text("Tìm theo tên...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Xóa tìm kiếm")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(14.dp),
    )
}

@Composable
private fun SummaryHeader(totals: DebtTotals, hasAny: Boolean) {
    val sono = LocalSonoColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "Họ nợ bạn",
                amount = totals.oweMe,
                sign = "+",
                contentColor = sono.oweMe,
                containerColor = sono.oweMeContainer,
                labelColor = sono.onOweMeContainer,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Bạn nợ",
                amount = totals.iOwe,
                sign = "−",
                contentColor = sono.iOwe,
                containerColor = sono.iOweContainer,
                labelColor = sono.onIOweContainer,
                modifier = Modifier.weight(1f),
            )
        }
        if (hasAny) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Chênh lệch",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = netText,
                    style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.SemiBold,
                    color = netColor,
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    amount: Long,
    sign: String,
    contentColor: Color,
    containerColor: Color,
    labelColor: Color,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$sign${VndFormat.format(amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DebtRow(debt: Debt, onClick: () -> Unit) {
    val sono = LocalSonoColors.current
    val isOweMe = debt.debtType == DebtType.THEY_OWE_ME
    val color = if (isOweMe) sono.oweMe else sono.iOwe
    val sign = if (isOweMe) "+" else "−"
    val direction = if (isOweMe) "Họ nợ bạn" else "Bạn nợ"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Chỉnh sửa ${debt.name}", onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = debt.name, uri = debt.avatarUri, size = 44.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = debt.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = direction,
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
        Text(
            text = "$sign${VndFormat.format(debt.amount)}",
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun EmptyState(query: String, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "₫",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = if (query.isBlank()) "Chưa có khoản nợ nào" else "Không tìm thấy kết quả",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (query.isBlank()) "Thêm người đầu tiên để bắt đầu." else "Thử từ khóa khác.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (query.isBlank()) {
            Spacer(Modifier.height(20.dp))
            TextButton(onClick = onAdd) {
                Text("Thêm khoản nợ")
            }
        }
    }
}
