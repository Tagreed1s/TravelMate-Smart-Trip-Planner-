package com.travelmate.ui.screens.cost


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.travelmate.core.Formatters
import com.travelmate.domain.model.Expense
import com.travelmate.domain.model.ExpenseCategory
import com.travelmate.ui.components.GlassCard
import com.travelmate.ui.components.TMChip
import com.travelmate.ui.components.TMPrimaryButton
import com.travelmate.ui.components.TMTextField
import com.travelmate.ui.components.icon
import com.travelmate.ui.components.label
import com.travelmate.ui.theme.PreviewSurface
import com.travelmate.ui.theme.Spacing
import com.travelmate.ui.theme.TMShapes
import com.travelmate.ui.theme.ThemePreviews
import com.travelmate.ui.theme.TravelMateTheme

/**
 * Edit Expenses sheet, opened from Trip Cost Summary (no route of its own).
 * The user's own costs (flights and anything the app cannot calculate) are listed, can be edited
 * by tapping them, deleted, or added with the form below.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpensesSheet(
    expenses: List<Expense>,
    form: ExpenseForm,
    saving: Boolean,
    onEvent: (TripCostSummaryEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TravelMateTheme.colors.surface,
        shape = TMShapes.sheet,
    ) {
        EditExpensesContent(expenses = expenses, form = form, saving = saving, onEvent = onEvent)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditExpensesContent(
    expenses: List<Expense>,
    form: ExpenseForm,
    saving: Boolean,
    onEvent: (TripCostSummaryEvent) -> Unit,
) {
    val colors = TravelMateTheme.colors
    val focusManager = LocalFocusManager.current
    val editing = form.id.isNotEmpty()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = Spacing.gutter)
            .padding(bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text("Edit expenses", style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
        Text(
            "Costs for the whole group that the app cannot calculate, such as flights. They are added to the trip total.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )

        if (expenses.isEmpty()) {
            Text("No expenses added yet.", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        } else {
            GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.xs)) {
                expenses.forEachIndexed { index, expense ->
                    ExpenseRow(
                        expense = expense,
                        selected = expense.id == form.id,
                        onEdit = { onEvent(TripCostSummaryEvent.EditExpense(expense)) },
                        onDelete = { onEvent(TripCostSummaryEvent.DeleteExpense(expense.id)) },
                    )
                    if (index < expenses.lastIndex) HorizontalDivider(color = colors.outlineGlass)
                }
            }
        }

        Text(if (editing) "Edit expense" else "Add an expense", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            ExpenseCategory.entries.forEach { category ->
                TMChip(
                    label = category.label,
                    selected = form.category == category,
                    onClick = { onEvent(TripCostSummaryEvent.FormCategoryChanged(category)) },
                )
            }
        }
        TMTextField(
            value = form.title,
            onValueChange = { onEvent(TripCostSummaryEvent.FormTitleChanged(it)) },
            label = "What is it? (optional)",
            placeholder = "Return flights",
            leadingIcon = Icons.Rounded.Edit,
            error = form.errors[ExpenseField.TITLE],
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
        )
        TMTextField(
            value = form.amountText,
            onValueChange = { onEvent(TripCostSummaryEvent.FormAmountChanged(it)) },
            label = "Amount for the group (SAR)",
            leadingIcon = Icons.Rounded.Payments,
            error = form.errors[ExpenseField.AMOUNT],
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        TMPrimaryButton(
            text = if (editing) "Save changes" else "Add expense",
            onClick = {
                focusManager.clearFocus()
                onEvent(TripCostSummaryEvent.SaveExpense)
            },
            loading = saving,
        )
        if (editing) {
            TextButton(onClick = { onEvent(TripCostSummaryEvent.CancelEdit) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Cancel editing", color = colors.secondary)
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, selected: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = TravelMateTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(expense.category.icon, contentDescription = null, tint = if (selected) colors.primary else colors.textSecondary)
        Column(
            Modifier
                .weight(1f)
                .clickable(onClickLabel = "Edit ${expense.title}", onClick = onEdit)
                .padding(vertical = Spacing.sm),
        ) {
            Text(expense.title, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text(expense.category.label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        }
        Text(Formatters.money(expense.amountSar), style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete ${expense.title}", tint = colors.textSecondary)
        }
    }
}

@ThemePreviews
@Composable
private fun EditExpensesEmptyPreview() = PreviewSurface {
    EditExpensesContent(expenses = emptyList(), form = ExpenseForm(), saving = false, onEvent = {})
}

@ThemePreviews
@Composable
private fun EditExpensesListPreview() = PreviewSurface {
    val sample = listOf(
        Expense("e1", ExpenseCategory.FLIGHTS, "SAMPLE return flights", 1_200, 1),
        Expense("e2", ExpenseCategory.OTHER, "SAMPLE souvenirs", 150, 2),
    )
    EditExpensesContent(
        expenses = sample,
        form = ExpenseForm(id = "e1", category = ExpenseCategory.FLIGHTS, title = "SAMPLE return flights", amountText = "1200"),
        saving = false,
        onEvent = {},
    )
}

