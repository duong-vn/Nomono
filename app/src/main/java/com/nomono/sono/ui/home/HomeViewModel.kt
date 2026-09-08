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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode(val label: String) {
    RECENT("Gần đây"),
    AMOUNT_DESC("Số tiền lớn nhất"),
    NAME_ASC("Tên A-Z"),
}

data class HomeUiState(
    val debts: List<Debt> = emptyList(),
    val sortMode: SortMode = SortMode.RECENT,
    val searchQuery: String = "",
    val totals: DebtTotals = DebtTotals(0L, 0L),
)

class HomeViewModel(private val repository: DebtRepository) : ViewModel() {

    private val sortMode = MutableStateFlow(SortMode.RECENT)
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> =
        combine(repository.observeAll(), sortMode, searchQuery) { list, sort, query ->
            val visible = if (query.isBlank()) {
                list
            } else {
                list.filter { it.name.contains(query.trim(), ignoreCase = true) }
            }
            val sorted = when (sort) {
                SortMode.RECENT -> visible
                SortMode.AMOUNT_DESC -> visible.sortedByDescending { it.amount }
                SortMode.NAME_ASC -> visible.sortedBy { it.name.lowercase() }
            }
            HomeUiState(
                debts = sorted,
                sortMode = sort,
                searchQuery = query,
                totals = computeTotals(list),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setSortMode(mode: SortMode) {
        sortMode.value = mode
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

    companion object {
        fun factory(repository: DebtRepository) = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
