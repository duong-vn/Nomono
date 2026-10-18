package com.nomono.sono.ui.edit

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nomono.sono.data.Debt
import com.nomono.sono.data.DebtTransaction
import com.nomono.sono.data.DebtType
import com.nomono.sono.data.TransactionKind
import com.nomono.sono.ui.components.Avatar
import com.nomono.sono.ui.components.TerminalKeyButton
import com.nomono.sono.ui.components.TerminalSheetHeader
import com.nomono.sono.ui.components.drawTerminalPhosphorGrid
import com.nomono.sono.ui.theme.LocalSonoColors
import com.nomono.sono.ui.theme.TerminalInk
import com.nomono.sono.ui.theme.TerminalNeg
import com.nomono.sono.ui.theme.TerminalOrange
import com.nomono.sono.ui.theme.TerminalOrangeDim
import com.nomono.sono.ui.theme.TerminalPage
import com.nomono.sono.ui.theme.TerminalScreen
import com.nomono.sono.ui.theme.TerminalSonoColors
import com.nomono.sono.ui.theme.VT323FontFamily
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
    isTerminal: Boolean = LocalSonoColors.current == TerminalSonoColors,
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
        containerColor = if (isTerminal) TerminalScreen else BottomSheetDefaults.ContainerColor,
        scrimColor = if (isTerminal) Color.Black.copy(alpha = 0.75f) else BottomSheetDefaults.ScrimColor,
        shape = if (isTerminal) RoundedCornerShape(0.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = if (isTerminal) null else { { BottomSheetDefaults.DragHandle() } },
    ) {
        if (isTerminal) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .background(TerminalScreen)
                    .drawTerminalPhosphorGrid()
                    .border(2.dp, TerminalOrange)
                    .padding(3.dp)
                    .border(1.dp, TerminalOrangeDim)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                TerminalSheetHeader(
                    title = if (debt != null) "CHI TIẾT NỢ" else "THÊM KHOẢN NỢ",
                    onDismiss = onDismiss,
                    extraAction = if (debt != null) {
                        {
                            TerminalKeyButton(
                                text = "XÓA NGƯỜI",
                                color = TerminalNeg,
                                borderColor = TerminalNeg,
                                onClick = { showDeleteConfirm = true },
                            )
                        }
                    } else null,
                )

                Spacer(Modifier.height(12.dp))

                if (editIdentity) {
                    TerminalAvatarField(
                        uri = avatarUri,
                        name = name,
                        onChange = { avatarUri = it },
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocus),
                        label = { Text("TÊN NGƯỜI", fontFamily = VT323FontFamily) },
                        placeholder = {
                            Text("VÍ DỤ: NGUYỄN VĂN A", fontFamily = VT323FontFamily, color = TerminalOrangeDim)
                        },
                        singleLine = true,
                        isError = nameError,
                        textStyle = TextStyle(
                            fontFamily = VT323FontFamily,
                            fontSize = 18.sp,
                            color = TerminalInk,
                        ),
                        supportingText = if (nameError) {
                            { Text("VUI LÒNG NHẬP TÊN NGƯỜI", fontFamily = VT323FontFamily, color = TerminalNeg) }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { amountFocus.requestFocus() }),
                        shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerminalOrange,
                            unfocusedBorderColor = TerminalOrangeDim,
                            focusedLabelColor = TerminalOrange,
                            unfocusedLabelColor = TerminalOrangeDim,
                            cursorColor = TerminalOrange,
                            focusedTextColor = TerminalInk,
                            unfocusedTextColor = TerminalInk,
                            errorBorderColor = TerminalNeg,
                            errorLabelColor = TerminalNeg,
                        ),
                    )
                } else {
                    val isZero = VndFormat.parseDigits(amountText) == 0L
                    val directionText = if (isZero) {
                        "KHÔNG CÒN NỢ"
                    } else if (debtType == DebtType.THEY_OWE_ME) {
                        "HỌ NỢ BẠN"
                    } else {
                        "BẠN NỢ HỌ"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalPage)
                            .border(1.dp, TerminalOrangeDim, RoundedCornerShape(0.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .border(1.dp, TerminalOrange, RoundedCornerShape(0.dp))
                                .background(TerminalScreen),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (avatarUri != null) {
                                Avatar(
                                    name = name,
                                    uri = avatarUri,
                                    size = 40.dp,
                                    shape = RoundedCornerShape(0.dp),
                                )
                            } else {
                                Text(
                                    text = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontFamily = VT323FontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = TerminalOrange,
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = name.uppercase(),
                                fontFamily = VT323FontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerminalOrange,
                            )
                            Text(
                                text = directionText,
                                fontFamily = VT323FontFamily,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp,
                                color = if (debtType == DebtType.THEY_OWE_ME) TerminalOrange else TerminalNeg,
                            )
                        }
                        TerminalKeyButton(
                            text = "[SỬA]",
                            active = false,
                            onClick = { editIdentity = true },
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        amountText = raw.filter(Char::isDigit).take(MAX_DIGITS)
                        amountError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(amountFocus),
                    label = { Text("SỐ TIỀN", fontFamily = VT323FontFamily) },
                    placeholder = { Text("0", fontFamily = VT323FontFamily, color = TerminalOrangeDim) },
                    suffix = { Text("₫", fontFamily = VT323FontFamily, fontWeight = FontWeight.Bold, color = TerminalOrange) },
                    singleLine = true,
                    isError = amountError,
                    textStyle = TextStyle(
                        fontFamily = VT323FontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalOrange,
                    ),
                    supportingText = if (amountError) {
                        { Text("VUI LÒNG NHẬP SỐ TIỀN", fontFamily = VT323FontFamily, color = TerminalNeg) }
                    } else null,
                    visualTransformation = VndGroupingTransformation,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { amountFocus.freeFocus() }),
                    shape = RoundedCornerShape(0.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TerminalOrange,
                        unfocusedBorderColor = TerminalOrangeDim,
                        focusedLabelColor = TerminalOrange,
                        unfocusedLabelColor = TerminalOrangeDim,
                        cursorColor = TerminalOrange,
                        focusedTextColor = TerminalOrange,
                        unfocusedTextColor = TerminalInk,
                        errorBorderColor = TerminalNeg,
                        errorLabelColor = TerminalNeg,
                    ),
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        onClick = { debtTypeName = DebtType.THEY_OWE_ME.name },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        color = if (debtType == DebtType.THEY_OWE_ME) TerminalOrange else TerminalPage,
                        border = BorderStroke(
                            1.dp,
                            if (debtType == DebtType.THEY_OWE_ME) TerminalOrange else TerminalOrangeDim,
                        ),
                        shape = RoundedCornerShape(0.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "▲ HỌ NỢ TÔI",
                                fontFamily = VT323FontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                letterSpacing = 1.sp,
                                color = if (debtType == DebtType.THEY_OWE_ME) TerminalScreen else TerminalOrange,
                            )
                        }
                    }
                    Surface(
                        onClick = { debtTypeName = DebtType.I_OWE_THEM.name },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        color = if (debtType == DebtType.I_OWE_THEM) TerminalNeg else TerminalPage,
                        border = BorderStroke(
                            1.dp,
                            if (debtType == DebtType.I_OWE_THEM) TerminalNeg else TerminalOrangeDim,
                        ),
                        shape = RoundedCornerShape(0.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "▼ TÔI NỢ HỌ",
                                fontFamily = VT323FontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                letterSpacing = 1.sp,
                                color = if (debtType == DebtType.I_OWE_THEM) TerminalScreen else TerminalNeg,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Surface(
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
                        .height(46.dp),
                    color = TerminalOrange,
                    border = BorderStroke(2.dp, TerminalOrange),
                    shape = RoundedCornerShape(0.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✓ LƯU THÔNG TIN",
                            fontFamily = VT323FontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 2.sp,
                            color = TerminalScreen,
                        )
                    }
                }

                if (debt != null) {
                    Spacer(Modifier.height(14.dp))
                    TransactionSection(
                        debt = debt,
                        transactions = transactions,
                        isTerminal = true,
                        onRecord = { showTransactionDialog = true },
                        onDeleteTransaction = { tx ->
                            onDeleteTransaction(tx.id)
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
                    Surface(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        color = TerminalPage,
                        border = BorderStroke(1.dp, TerminalNeg),
                        shape = RoundedCornerShape(0.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "[XÓA KHOẢN NỢ VỀ 0 ₫]",
                                fontFamily = VT323FontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp,
                                color = TerminalNeg,
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (debt != null) "Chi tiết nợ" else "Thêm khoản nợ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
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
                    val isZero = VndFormat.parseDigits(amountText) == 0L
                    val directionText = if (isZero) {
                        "Không nợ"
                    } else if (debtType == DebtType.THEY_OWE_ME) {
                        "Họ nợ bạn"
                    } else {
                        "Bạn nợ"
                    }
                    IdentityHeader(
                        name = name,
                        avatarUri = avatarUri,
                        direction = directionText,
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
                    suffix = { Text("₫", fontWeight = FontWeight.Bold) },
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
                    shape = RoundedCornerShape(14.dp),
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
                Spacer(Modifier.height(20.dp))

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
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Lưu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (debt != null) {
                    Spacer(Modifier.height(16.dp))
                    TransactionSection(
                        debt = debt,
                        transactions = transactions,
                        isTerminal = false,
                        onRecord = { showTransactionDialog = true },
                        onDeleteTransaction = { tx ->
                            onDeleteTransaction(tx.id)
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
    }

    if (showClearConfirm && debt != null) {
        if (isTerminal) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                containerColor = TerminalScreen,
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.border(2.dp, TerminalOrange, RoundedCornerShape(0.dp)),
                title = {
                    Text(
                        "XÓA KHOẢN NỢ?",
                        fontFamily = VT323FontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TerminalOrange,
                    )
                },
                text = {
                    Text(
                        "Số tiền sẽ về 0 ₫ và lịch sử giao dịch sẽ bị xóa. Người này vẫn ở trong sổ nợ.",
                        fontFamily = VT323FontFamily,
                        fontSize = 15.sp,
                        color = TerminalInk,
                    )
                },
                confirmButton = {
                    TerminalKeyButton(
                        text = "[XÓA]",
                        color = TerminalNeg,
                        borderColor = TerminalNeg,
                        onClick = {
                            showClearConfirm = false
                            onClearRequest(debt)
                        },
                    )
                },
                dismissButton = {
                    TerminalKeyButton(
                        text = "[HỦY]",
                        onClick = { showClearConfirm = false },
                    )
                },
            )
        } else {
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
    }

    if (showDeleteConfirm && debt != null) {
        if (isTerminal) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                containerColor = TerminalScreen,
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.border(2.dp, TerminalOrange, RoundedCornerShape(0.dp)),
                title = {
                    Text(
                        "XÓA NGƯỜI?",
                        fontFamily = VT323FontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TerminalOrange,
                    )
                },
                text = {
                    Text(
                        "Xóa ${debt.name} và toàn bộ lịch sử giao dịch. Hành động này không thể hoàn tác.",
                        fontFamily = VT323FontFamily,
                        fontSize = 15.sp,
                        color = TerminalInk,
                    )
                },
                confirmButton = {
                    TerminalKeyButton(
                        text = "[XÓA]",
                        color = TerminalNeg,
                        borderColor = TerminalNeg,
                        onClick = {
                            showDeleteConfirm = false
                            onDeleteRequest(debt)
                        },
                    )
                },
                dismissButton = {
                    TerminalKeyButton(
                        text = "[HỦY]",
                        onClick = { showDeleteConfirm = false },
                    )
                },
            )
        } else {
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
    }

    if (showTransactionDialog && debt != null) {
        TransactionDialog(
            debt = debt,
            isTerminal = isTerminal,
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
private fun TerminalAvatarField(uri: String?, name: String, onChange: (String?) -> Unit) {
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
        Box(
            modifier = Modifier
                .size(52.dp)
                .border(1.dp, TerminalOrange, RoundedCornerShape(0.dp))
                .background(TerminalPage)
                .clickable {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            if (uri != null) {
                Avatar(name = name, uri = uri, size = 48.dp, shape = RoundedCornerShape(0.dp))
            } else {
                Text(
                    text = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontFamily = VT323FontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TerminalOrange,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        TerminalKeyButton(
            text = if (uri != null) "[ĐỔI ẢNH]" else "[CHỌN ẢNH]",
            active = false,
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
        )
        if (uri != null) {
            Spacer(Modifier.width(6.dp))
            TerminalKeyButton(
                text = "[XÓA ẢNH]",
                active = false,
                color = TerminalNeg,
                borderColor = TerminalNeg,
                onClick = { onChange(null) },
            )
        }
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
    val color = when (direction) {
        "Không nợ" -> MaterialTheme.colorScheme.onSurfaceVariant
        "Họ nợ bạn" -> sono.oweMe
        else -> sono.iOwe
    }

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
    isTerminal: Boolean,
    onRecord: () -> Unit,
    onDeleteTransaction: (DebtTransaction) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<DebtTransaction?>(null) }

    if (isTerminal) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TerminalOrangeDim),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "LỊCH SỬ GIAO DỊCH",
                fontFamily = VT323FontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 1.sp,
                color = TerminalOrange,
                modifier = Modifier.weight(1f),
            )
            TerminalKeyButton(
                text = "+ GHI NHẬN",
                active = false,
                onClick = onRecord,
            )
        }
        Spacer(Modifier.height(6.dp))

        if (transactions.isEmpty()) {
            Text(
                text = "CHƯA CÓ GIAO DỊCH NÀO.",
                fontFamily = VT323FontFamily,
                fontSize = 13.sp,
                color = TerminalOrangeDim,
            )
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 168.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                transactions.forEach { tx ->
                    TerminalTransactionRow(
                        transaction = tx,
                        debtType = debt.debtType,
                        onDelete = { pendingDelete = tx },
                    )
                }
            }
        }
    } else {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Lịch sử giao dịch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            FilledTonalButton(
                onClick = onRecord,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("+ Ghi nhận", fontWeight = FontWeight.SemiBold)
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
    }

    val txToDelete = pendingDelete
    if (txToDelete != null) {
        val labels = kindLabels(debt.debtType)
        val kindText = if (txToDelete.kind == TransactionKind.PAYMENT) labels.payment else labels.add
        if (isTerminal) {
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                containerColor = TerminalScreen,
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.border(2.dp, TerminalOrange, RoundedCornerShape(0.dp)),
                title = {
                    Text(
                        "XÓA GIAO DỊCH?",
                        fontFamily = VT323FontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TerminalOrange,
                    )
                },
                text = {
                    Text(
                        "$kindText ${VndFormat.format(txToDelete.amount)} sẽ bị xóa và số nợ cập nhật lại.",
                        fontFamily = VT323FontFamily,
                        fontSize = 15.sp,
                        color = TerminalInk,
                    )
                },
                confirmButton = {
                    TerminalKeyButton(
                        text = "[XÓA]",
                        color = TerminalNeg,
                        borderColor = TerminalNeg,
                        onClick = {
                            pendingDelete = null
                            onDeleteTransaction(txToDelete)
                        },
                    )
                },
                dismissButton = {
                    TerminalKeyButton(
                        text = "[HỦY]",
                        onClick = { pendingDelete = null },
                    )
                },
            )
        } else {
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
}

@Composable
private fun TerminalTransactionRow(
    transaction: DebtTransaction,
    debtType: DebtType,
    onDelete: () -> Unit,
) {
    val isPayment = transaction.kind == TransactionKind.PAYMENT
    val labels = kindLabels(debtType)
    val label = if (isPayment) labels.payment else labels.add
    val sign = if (isPayment) "−" else "+"
    val color = if (isPayment) TerminalOrange else TerminalNeg

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(0.dp),
        color = TerminalPage,
        border = BorderStroke(1.dp, if (isPayment) TerminalOrangeDim else TerminalNeg.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    fontFamily = VT323FontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
                Text(
                    text = formatTxTime(transaction.createdAt),
                    fontFamily = VT323FontFamily,
                    fontSize = 11.sp,
                    color = TerminalOrangeDim,
                )
            }
            Text(
                text = "$sign${VndFormat.format(transaction.amount)}",
                fontFamily = VT323FontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Spacer(Modifier.width(8.dp))
            TerminalKeyButton(
                text = "[X]",
                color = TerminalNeg,
                borderColor = TerminalNeg.copy(alpha = 0.5f),
                onClick = onDelete,
            )
        }
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = formatTxTime(transaction.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "$sign${VndFormat.format(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
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
}

@Composable
private fun TransactionDialog(
    debt: Debt,
    isTerminal: Boolean,
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

    if (isTerminal) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = TerminalScreen,
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.border(2.dp, TerminalOrange, RoundedCornerShape(0.dp)),
            title = {
                Text(
                    "GHI NHẬN GIAO DỊCH",
                    fontFamily = VT323FontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TerminalOrange,
                )
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            onClick = { kindName = TransactionKind.PAYMENT.name },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            color = if (kind == TransactionKind.PAYMENT) TerminalOrange else TerminalPage,
                            border = BorderStroke(
                                1.dp,
                                if (kind == TransactionKind.PAYMENT) TerminalOrange else TerminalOrangeDim,
                            ),
                            shape = RoundedCornerShape(0.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = labels.payment.uppercase(),
                                    fontFamily = VT323FontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (kind == TransactionKind.PAYMENT) TerminalScreen else TerminalOrange,
                                )
                            }
                        }
                        Surface(
                            onClick = { kindName = TransactionKind.ADD.name },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            color = if (kind == TransactionKind.ADD) TerminalNeg else TerminalPage,
                            border = BorderStroke(
                                1.dp,
                                if (kind == TransactionKind.ADD) TerminalNeg else TerminalOrangeDim,
                            ),
                            shape = RoundedCornerShape(0.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = labels.add.uppercase(),
                                    fontFamily = VT323FontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (kind == TransactionKind.ADD) TerminalScreen else TerminalNeg,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { raw ->
                            amountText = raw.filter(Char::isDigit).take(MAX_DIGITS)
                            amountError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("SỐ TIỀN", fontFamily = VT323FontFamily) },
                        placeholder = { Text("0", fontFamily = VT323FontFamily, color = TerminalOrangeDim) },
                        suffix = { Text("₫", fontFamily = VT323FontFamily, fontWeight = FontWeight.Bold, color = TerminalOrange) },
                        singleLine = true,
                        isError = amountError,
                        textStyle = TextStyle(
                            fontFamily = VT323FontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerminalOrange,
                        ),
                        supportingText = if (amountError) {
                            { Text("NHẬP SỐ TIỀN", fontFamily = VT323FontFamily, color = TerminalNeg) }
                        } else null,
                        visualTransformation = VndGroupingTransformation,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(0.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerminalOrange,
                            unfocusedBorderColor = TerminalOrangeDim,
                            focusedLabelColor = TerminalOrange,
                            unfocusedLabelColor = TerminalOrangeDim,
                            cursorColor = TerminalOrange,
                            focusedTextColor = TerminalOrange,
                            unfocusedTextColor = TerminalInk,
                            errorBorderColor = TerminalNeg,
                            errorLabelColor = TerminalNeg,
                        ),
                    )
                }
            },
            confirmButton = {
                TerminalKeyButton(
                    text = "[LƯU GIAO DỊCH]",
                    active = true,
                    onClick = {
                        val parsed = VndFormat.parseDigits(amountText)
                        amountError = !Validation.isAmountValid(parsed)
                        if (!amountError) {
                            onConfirm(parsed, kind)
                        }
                    },
                )
            },
            dismissButton = {
                TerminalKeyButton(
                    text = "[HỦY]",
                    onClick = onDismiss,
                )
            },
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Ghi nhận giao dịch",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
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
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { raw ->
                            amountText = raw.filter(Char::isDigit).take(MAX_DIGITS)
                            amountError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Số tiền") },
                        placeholder = { Text("0") },
                        suffix = { Text("₫", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        isError = amountError,
                        supportingText = if (amountError) {
                            { Text("Nhập số tiền") }
                        } else {
                            null
                        },
                        visualTransformation = VndGroupingTransformation,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
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
                    Text("Lưu", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Hủy")
                }
            },
        )
    }
}

private val txTimeFormatter = ThreadLocal.withInitial {
    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
}

private fun formatTxTime(timestamp: Long): String =
    txTimeFormatter.get()?.format(Date(timestamp)) ?: ""

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
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(if (uri != null) "Đổi ảnh" else "Chọn ảnh")
                }
                if (uri != null) {
                    TextButton(
                        onClick = { onChange(null) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    ) {
                        Text("Xóa", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
