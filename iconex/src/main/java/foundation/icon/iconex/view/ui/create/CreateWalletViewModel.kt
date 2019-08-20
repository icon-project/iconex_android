package foundation.icon.iconex.view.ui.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateWalletViewModel : ViewModel() {

    private var coinType: MutableLiveData<CoinType>? = null

    fun getCoinType(): MutableLiveData<CoinType> {
        if (coinType == null)
            coinType = MutableLiveData()

        return coinType!!
    }

    fun setCoinType(type: CoinType) {
        getCoinType().value = type
    }

    enum class CoinType constructor(val type: String) {
        ICX("icx"),
        ETH("eth");


        companion object {

            fun fromType(type: String): CoinType? {
                for (t in values()) {
                    if (t.type == type)
                        return t
                }

                return null
            }
        }
    }
}
