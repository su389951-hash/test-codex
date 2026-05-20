package com.example.stocktrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                StockTradingApp()
            }
        }
    }
}

data class Stock(val symbol: String, var price: Double)

@Composable
fun StockTradingApp() {
    val market = remember {
        mutableStateListOf(
            Stock("AAPL", 188.20),
            Stock("TSLA", 177.43),
            Stock("NVDA", 1031.57),
            Stock("MSFT", 417.88)
        )
    }
    var cash by remember { mutableDoubleStateOf(10000.0) }
    var holdings by remember { mutableStateOf(mapOf<String, Int>()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("模拟炒股助手") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("可用资金: $${"%.2f".format(cash)}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("持仓: ${holdings.entries.joinToString { "${it.key} x${it.value}" }.ifBlank { "暂无" }}")
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    market.forEachIndexed { index, stock ->
                        val delta = Random.nextDouble(-0.03, 0.03)
                        market[index] = stock.copy(price = (stock.price * (1 + delta)).coerceAtLeast(1.0))
                    }
                }) {
                    Text("刷新行情")
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(market) { stock ->
                    StockCard(
                        stock = stock,
                        onBuy = {
                            if (cash >= stock.price) {
                                cash -= stock.price
                                holdings = holdings.toMutableMap().apply {
                                    this[stock.symbol] = (this[stock.symbol] ?: 0) + 1
                                }
                            }
                        },
                        onSell = {
                            val count = holdings[stock.symbol] ?: 0
                            if (count > 0) {
                                cash += stock.price
                                holdings = holdings.toMutableMap().apply {
                                    if (count == 1) remove(stock.symbol) else this[stock.symbol] = count - 1
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StockCard(stock: Stock, onBuy: () -> Unit, onSell: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(stock.symbol, style = MaterialTheme.typography.titleMedium)
            Text("价格: $${"%.2f".format(stock.price)}")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBuy) { Text("买入 1 股") }
                Button(onClick = onSell) { Text("卖出 1 股") }
            }
        }
    }
}
