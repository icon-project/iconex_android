package foundation.icon.iconex.dev_detail

data class TransactionItem(
        var txHash: String?,
        var date: String?,
        var from: String?,
        var to: String?,
        var amount: String?,
        var fee: String?,
        var state: Int?
)