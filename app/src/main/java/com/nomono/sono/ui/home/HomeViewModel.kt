package com.nomono.sono.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nomono.sono.data.Debt
import com.nomono.sono.data.DebtRepository
import com.nomono.sono.data.DebtType
import com.nomono.sono.data.TransactionKind
import com.nomono.sono.util.DebtTotals
import com.nomono.sono.util.computeTotals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode(val label: String) {
    RECENT("Gần đây"),
    AMOUNT_DESC("Số tiền lớn nhất"),
    NAME_ASC("Tên A-Z"),
}

enum class DebtFilter(val label: String) {
    ALL("Tất cả"),
    THEY_OWE_ME("Họ nợ tôi"),
    I_OWE_THEM("Tôi nợ họ"),
    SETTLED("Đã tất toán"),
}

fun List<Debt>.filterFor(filter: DebtFilter): List<Debt> = filter {
    when (filter) {
        DebtFilter.ALL -> true
        DebtFilter.THEY_OWE_ME -> it.debtType == DebtType.THEY_OWE_ME && it.amount > 0
        DebtFilter.I_OWE_THEM -> it.debtType == DebtType.I_OWE_THEM && it.amount > 0
        DebtFilter.SETTLED -> it.amount == 0L
    }
}

data class HomeUiState(
    val debts: List<Debt> = emptyList(),
    val sortMode: SortMode = SortMode.RECENT,
    val debtFilter: DebtFilter = DebtFilter.THEY_OWE_ME,
    val searchQuery: String = "",
    val totals: DebtTotals = DebtTotals(0L, 0L),
)

@OptIn(FlowPreview::class)
class HomeViewModel(private val repository: DebtRepository) : ViewModel() {

    private val sortMode = MutableStateFlow(SortMode.RECENT)
    private val debtFilter = MutableStateFlow(DebtFilter.THEY_OWE_ME)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> =
        combine(
            repository.observeAll(),
            sortMode,
            debtFilter,
            // Don't re-filter/sort on every keystroke while typing fast.
            searchQuery.debounce(150),
        ) { list, sort, filter, query ->
            val filtered = list.filterFor(filter)
            val q = query.trim()
            val visible = if (q.isEmpty()) {
                filtered
            } else {
                filtered.filter { it.name.contains(q, ignoreCase = true) }
            }
            val sorted = when (sort) {
                SortMode.RECENT -> visible
                SortMode.AMOUNT_DESC -> visible.sortedByDescending { it.amount }
                // compareBy avoids a lowercase() allocation per element.
                SortMode.NAME_ASC -> visible.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            }
            HomeUiState(
                debts = sorted,
                sortMode = sort,
                debtFilter = filter,
                searchQuery = query,
                totals = computeTotals(list),
            )
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setSortMode(mode: SortMode) {
        sortMode.value = mode
    }

    fun setDebtFilter(filter: DebtFilter) {
        debtFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun add(name: String, amount: Long, debtType: DebtType, avatarUri: String?) {
        viewModelScope.launch { repository.add(name, amount, debtType, avatarUri) }
    }

    fun update(id: Long, name: String, amount: Long, debtType: DebtType, avatarUri: String?) {
        viewModelScope.launch { repository.update(id, name, amount, debtType, avatarUri) }
    }

    fun clear(id: Long) {
        viewModelScope.launch { repository.clear(id) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun recordTransaction(debtId: Long, amount: Long, kind: TransactionKind) {
        viewModelScope.launch { repository.recordTransaction(debtId, amount, kind) }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch { repository.deleteTransaction(transactionId) }
    }

    companion object {
        fun factory(repository: DebtRepository) = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
