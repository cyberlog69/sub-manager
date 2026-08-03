package com.submanager.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.submanager.app.data.local.SubscriptionEntity
import com.submanager.app.data.model.BillingCycle
import com.submanager.app.data.model.SubscriptionCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionSheet(
    sheetState: SheetState,
    existingSubscription: SubscriptionEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, currency: String, cycle: BillingCycle, category: SubscriptionCategory, paymentMethod: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(existingSubscription?.name ?: "") }
    var amountStr by remember { mutableStateOf(existingSubscription?.amount?.toString() ?: "") }
    var currency by remember { mutableStateOf(existingSubscription?.currency ?: "INR") }
    var selectedCycle by remember {
        mutableStateOf(
            existingSubscription?.let { try { BillingCycle.valueOf(it.billingCycle) } catch (e: Exception) { BillingCycle.MONTHLY } } ?: BillingCycle.MONTHLY
        )
    }
    var selectedCategory by remember {
        mutableStateOf(
            existingSubscription?.let { try { SubscriptionCategory.valueOf(it.category) } catch (e: Exception) { SubscriptionCategory.ENTERTAINMENT } } ?: SubscriptionCategory.ENTERTAINMENT
        )
    }
    var paymentMethod by remember { mutableStateOf(existingSubscription?.paymentMethod ?: "Credit Card / UPI") }
    var notes by remember { mutableStateOf(existingSubscription?.notes ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (existingSubscription == null) "Add Subscription" else "Edit Subscription",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Service Name (e.g. Netflix, Spotify)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Amount & Currency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Billing Cycle Selection
            Text(
                text = "Billing Cycle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BillingCycle.values()) { cycle ->
                    FilterChip(
                        selected = selectedCycle == cycle,
                        onClick = { selectedCycle = cycle },
                        label = { Text(cycle.displayName) }
                    )
                }
            }

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SubscriptionCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.displayName) }
                    )
                }
            }

            // Payment Method
            OutlinedTextField(
                value = paymentMethod,
                onValueChange = { paymentMethod = it },
                label = { Text("Payment Method (e.g. HDFC Credit Card, UPI Mandate)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0.0) {
                        onSave(name, amount, currency, selectedCycle, selectedCategory, paymentMethod, notes)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (existingSubscription == null) "Save Subscription" else "Update Subscription")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
