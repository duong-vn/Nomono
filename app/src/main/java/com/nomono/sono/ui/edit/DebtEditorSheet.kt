package com.nomono.sono.ui.edit

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nomono.sono.data.Debt
import com.nomono.sono.data.DebtTransaction
import com.nomono.sono.data.DebtType
import com.nomono.sono.data.TransactionKind
import com.nomono.sono.ui.components.Avatar
import com.nomono.sono.ui.theme.LocalSonoColors
import com.nomono.sono.util.Validation
import com.nomono.sono.util.VndFormat
import com.nomono.sono.util.VndGroupingTransformation
import com.nomono.sono.util.applyTransaction
import com.nomono.sono.util.reverseStoredTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private const val MAX_DIGITS = 15

private data class KindLabels(val payment: String, val add: String)

private fun kindLabels(type: DebtType): KindLabels = when (type) {
    DebtType.THEY_OWE_ME -> KindLabels("Họ trả tôi", "Cho vay thêm")
    DebtType.I_OWE_THEM -> KindLabels("Tôi trả họ", "Mượn thêm")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtEditorSheet(
    debt: Debt?,
    transactions: List<DebtTransaction>,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Long, debtType: DebtType, avatarUri: String?) -> Unit,
    onClearRequest: (Debt) -> Unit,
    onDeleteRequest: (Debt) -> Unit,
    onRecordTransaction: (debtId: Long, amount: Long, kind: TransactionKind) -> Unit,
    onDeleteTransaction: (transactionId: Long) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(debt?.name ?: "") }
    var amountText by rememberSaveable { mutableStateOf(debt?.amount?.toString() ?: "") }
    var debtTypeName by rememberSaveable { mutableStateOf((debt?.debtType ?: DebtType.THEY_OWE_ME).name) }
    var avatarUri by rememberSaveable { mutableStateOf(debt?.avatarUri) }

    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPersonMenu by remember { mutableStateOf(false) }
    var showTransactionDialog by remember { mutableStateOf(false) }
    var editIdentity by rememberSaveable { mutableStateOf(debt == null) }

    val debtType = remember(debtTypeName) {
        runCatching { DebtType.valueOf(debtTypeName) }.getOrDefault(DebtType.THEY_OWE_ME)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val nameFocus = remember { FocusRequester() }
    val amountFocus = remember { FocusRequester() }

    LaunchedEffect(debt) {
        if (debt == null) {
            delay(150)
            nameFocus.requestFocus()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (debt != null) "Chi tiết nợ" else "Thêm khoản nợ",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (debt != null) {
                    Box {
                        IconButton(onClick = { showPersonMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Thêm tùy chọn")
                        }
                        DropdownMenu(
                            expanded = showPersonMenu,
                            onDismissRequest = { showPersonMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Xóa người", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showPersonMenu = false
                                    showDeleteConfirm = true
                                },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            if (editIdentity) {
                AvatarField(uri = avatarUri, name = name, onChange = { avatarUri = it })
                Spacer(Modifier.height(12.dp))
                NameField(
                    value = name,
                    isError = nameError,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    onDone = { amountFocus.requestFocus() },
                    focusRequester = nameFocus,
                )
            } else {
                IdentityHeader(
                    name = name,
                    avatarUri = avatarUri,
                    direction = if (debtType == DebtType.THEY_OWE_ME) "Họ nợ bạn" else "Bạn nợ",
                    onEdit = { editIdentity = true },
                )
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { raw ->
                    amountText = raw.filter(Char::isDigit).take(MAX_DIGITS)
                    amountError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(amountFocus),
                label = { Text("Số tiền") },
                placeholder = { Text("0") },
                suffix = { Text("₫") },
                singleLine = true,
                isError = amountError,
                supportingText = if (amountError) {
                    { Text("Nhập số tiền") }
                } else {
                    null
                },
                visualTransformation = VndGroupingTransformation,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { amountFocus.freeFocus() }),
            )
            Spacer(Modifier.height(16.dp))

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = debtType == DebtType.THEY_OWE_ME,
                    onClick = { debtTypeName = DebtType.THEY_OWE_ME.name },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) {
                    Text("Họ nợ tôi")
                }
                SegmentedButton(
                    selected = debtType == DebtType.I_OWE_THEM,
                    onClick = { debtTypeName = DebtType.I_OWE_THEM.name },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) {
                    Text("Tôi nợ họ")
                }
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val parsed = VndFormat.parseDigits(amountText)
                    nameError = !Validation.isNameValid(name)
                    amountError = !Validation.isAmountValid(parsed)
                    if (!nameError && !amountError) {
                        onSave(name.trim(), parsed, debtType, avatarUri)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("Lưu", style = MaterialTheme.typography.bodyLarge)
            }

            if (debt != null) {
                Spacer(Modifier.height(16.dp))
                TransactionSection(
                    debt = debt,
                    transactions = transactions,
                    onRecord = { showTransactionDialog = true },
                    onDeleteTransaction = { tx ->
                        onDeleteTransaction(tx.id)
                        // Đồng bộ số hiển thị trong sheet với DB sau khi xóa.
                        val balance = reverseStoredTransaction(
                            VndFormat.parseDigits(amountText),
                            debtType,
                            tx,
                        )
                        amountText = balance.amount.toString()
                        debtTypeName = balance.debtType.name
                    },
                )

                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Xóa khoản nợ", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showClearConfirm && debt != null) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Xóa khoản nợ?") },
            text = { Text("Số tiền sẽ về 0 ₫ và lịch sử giao dịch sẽ bị xóa. Người này vẫn ở trong sổ nợ.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClearRequest(debt)
                    },
                ) {
                    Text("Xóa khoản nợ", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Hủy")
                }
            },
        )
    }

    if (showDeleteConfirm && debt != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xóa người?") },
            text = { Text("Xóa ${debt.name} và toàn bộ lịch sử giao dịch. Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteRequest(debt)
                    },
                ) {
                    Text("Xóa người", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            },
        )
    }

    if (showTransactionDialog && debt != null) {
        TransactionDialog(
            debt = debt,
            onDismiss = { showTransactionDialog = false },
            onConfirm = { txAmount, kind ->
                onRecordTransaction(debt.id, txAmount, kind)
                val balance = applyTransaction(
                    VndFormat.parseDigits(amountText),
                    debtType,
                    kind,
                    txAmount,
                )
                amountText = balance.amount.toString()
                debtTypeName = balance.debtType.name
                showTransactionDialog = false
            },
        )
    }
}

@Composable
private fun NameField(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    focusRequester: FocusRequester,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        label = { Text("Tên") },
        placeholder = { Text("Ví dụ: Nguyễn Văn A") },
        singleLine = true,
        isError = isError,
        supportingText = if (isError) {
            { Text("Nhập tên người") }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { onDone() }),
    )
}

@Composable
private fun IdentityHeader(
    name: String,
    avatarUri: String?,
    direction: String,
    onEdit: () -> Unit,
) {
    val sono = LocalSonoColors.current
    val color = if (direction == "Họ nợ bạn") sono.oweMe else sono.iOwe

    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(name = name, uri = avatarUri, size = 56.dp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = direction,
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = "Chỉnh tên và ảnh",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun TransactionSection(
    debt: Debt,
    transactions: List<DebtTransaction>,
    onRecord: () -> Unit,
    onDeleteTransaction: (DebtTransaction) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<DebtTransaction?>(null) }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Lịch sử giao dịch",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRecord) {
            Text("+ Ghi nhận")
        }
    }

    if (transactions.isEmpty()) {
        Text(
            text = "Chưa có giao dịch nào.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 168.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            transactions.forEach { tx ->
                TransactionRow(
                    transaction = tx,
                    debtType = debt.debtType,
                    onDelete = { pendingDelete = tx },
                )
            }
        }
    }

    val txToDelete = pendingDelete
    if (txToDelete != null) {
        val labels = kindLabels(debt.debtType)
        val kindText = if (txToDelete.kind == TransactionKind.PAYMENT) labels.payment else labels.add
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Xóa giao dịch?") },
            text = { Text("$kindText ${VndFormat.format(txToDelete.amount)} sẽ bị xóa và số nợ cập nhật lại.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        onDeleteTransaction(txToDelete)
                    },
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Hủy")
                }
            },
        )
    }
}

@Composable
private fun TransactionRow(
    transaction: DebtTransaction,
    debtType: DebtType,
    onDelete: () -> Unit,
) {
    val sono = LocalSonoColors.current
    val isPayment = transaction.kind == TransactionKind.PAYMENT
    val labels = kindLabels(debtType)
    val label = if (isPayment) labels.payment else labels.add
    val sign = if (isPayment) "−" else "+"
    val color = if (isPayment) sono.oweMe else sono.iOwe

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = formatTxTime(transaction.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "$sign${VndFormat.format(transaction.amount)}",
            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Xóa giao dịch $label ${VndFormat.format(transaction.amount)}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TransactionDialog(
    debt: Debt,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, kind: TransactionKind) -> Unit,
) {
    var amountText by rememberSaveable { mutableStateOf("") }
    var kindName by rememberSaveable { mutableStateOf(TransactionKind.ADD.name) }
    var amountError by remember { mutableStateOf(false) }

    val kind = remember(kindName) {
        runCatching { TransactionKind.valueOf(kindName) }.getOrDefault(TransactionKind.ADD)
    }
    val labels = kindLabels(debt.debtType)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ghi nhận giao dịch") },
        text = {
            Column {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = kind == TransactionKind.PAYMENT,
                        onClick = { kindName = TransactionKind.PAYMENT.name },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) {
                        Text(labels.payment)
                    }
                    SegmentedButton(
                        selected = kind == TransactionKind.ADD,
                        onClick = { kindName = TransactionKind.ADD.name },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) {
                        Text(labels.add)
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        amountText = raw.filter(Char::isDigit).take(MAX_DIGITS)
                        amountError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Số tiền") },
                    suffix = { Text("₫") },
                    singleLine = true,
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text("Nhập số tiền") }
                    } else {
                        null
                    },
                    visualTransformation = VndGroupingTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = VndFormat.parseDigits(amountText)
                    amountError = !Validation.isAmountValid(parsed)
                    if (!amountError) {
                        onConfirm(parsed, kind)
                    }
                },
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
    )
}

private val txTimeFormatter = ThreadLocal.withInitial {
    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
}

private fun formatTxTime(timestamp: Long): String =
    txTimeFormatter.get().format(Date(timestamp))

@Composable
private fun AvatarField(uri: String?, name: String, onChange: (String?) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { picked ->
        if (picked != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    picked,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onChange(picked.toString())
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            Avatar(name = name, uri = uri, size = 56.dp)
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomEnd)
                    .padding(3.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = "Ảnh đại diện",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Text(if (uri == null) "Chọn ảnh" else "Đổi ảnh")
            }
        }
    }
}
