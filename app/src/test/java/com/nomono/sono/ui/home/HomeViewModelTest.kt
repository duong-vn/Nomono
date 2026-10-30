package com.nomono.sono.ui.home

import androidx.lifecycle.ViewModelStore
import com.nomono.sono.data.Debt
import com.nomono.sono.data.DebtDao
import com.nomono.sono.data.DebtRepository
import com.nomono.sono.data.DebtTransaction
import com.nomono.sono.data.DebtType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @Test
    fun search_input_is_immediate_while_results_are_debounced() = withViewModel { vm ->
        withTimeout(5_000) { vm.uiState.first { it.debts.size == 2 } }

        vm.setSearchQuery("Bình")
        assertEquals("Bình", vm.searchInput.value)
        assertEquals("", vm.uiState.value.searchQuery)

        val searched = withTimeout(5_000) { vm.uiState.first { it.searchQuery == "Bình" } }
        assertEquals(listOf("Bình"), searched.debts.map { it.name })
        assertEquals(100L, searched.totals.oweMe)
        assertEquals(50L, searched.totals.iOwe)

        vm.setSearchQuery("")
        assertEquals("", vm.searchInput.value)
        val cleared = withTimeout(5_000) { vm.uiState.first { it.searchQuery.isEmpty() } }
        assertEquals(2, cleared.debts.size)
    }

    @Test
    fun filtering_and_sorting_preserve_global_totals() = withViewModel { vm ->
        val initial = withTimeout(5_000) { vm.uiState.first { it.debts.size == 2 } }
        vm.setSortMode(SortMode.NAME_ASC)
        val sorted = withTimeout(5_000) { vm.uiState.first { it.sortMode == SortMode.NAME_ASC } }
        assertEquals(listOf("An", "Bình"), sorted.debts.map { it.name })

        vm.setDebtFilter(DebtFilter.I_OWE_THEM)
        val filtered = withTimeout(5_000) { vm.uiState.first { it.debtFilter == DebtFilter.I_OWE_THEM } }
        assertEquals(listOf("Bình"), filtered.debts.map { it.name })
        assertEquals(initial.totals, filtered.totals)
    }

    private fun withViewModel(block: suspend (HomeViewModel) -> Unit) = runBlocking {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val store = ViewModelStore()
        try {
            val vm = HomeViewModel(DebtRepository(ReadOnlyDebtDao()))
            store.put("home", vm)
            block(vm)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private class ReadOnlyDebtDao : DebtDao {
        private val debts = MutableStateFlow(listOf(
            Debt(2, "Bình", 50, DebtType.I_OWE_THEM, null, 2, 2),
            Debt(1, "An", 100, DebtType.THEY_OWE_ME, null, 1, 1),
        ))

        override fun observeAll() = debts
        override fun observeTransactions(debtId: Long) = flowOf(emptyList<DebtTransaction>())
        override suspend fun getById(id: Long): Debt? = debts.value.find { it.id == id }
        override suspend fun getTransactionById(id: Long): DebtTransaction? = null
        override suspend fun insert(debt: Debt): Long = error("read-only")
        override suspend fun update(debt: Debt): Unit = error("read-only")
        override suspend fun delete(debt: Debt): Unit = error("read-only")
        override suspend fun deleteTransaction(transaction: DebtTransaction): Unit = error("read-only")
        override suspend fun insertTransaction(transaction: DebtTransaction): Long = error("read-only")
        override suspend fun deleteTransactions(debtId: Long): Unit = error("read-only")
    }
}
