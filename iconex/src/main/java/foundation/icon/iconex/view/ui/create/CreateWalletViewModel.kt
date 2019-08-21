package foundation.icon.iconex.view.ui.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import foundation.icon.iconex.wallet.Wallet

class CreateWalletViewModel : ViewModel() {

    private var coinType: MutableLiveData<CoinType>? = null
    private var wallet: MutableLiveData<Wallet>? = null
    private var privateKey: MutableLiveData<String>? = null

    fun getCoinType(): MutableLiveData<CoinType> {
        if (coinType == null)
            coinType = MutableLiveData()

        return coinType!!
    }

    fun setCoinType(type: CoinType) {
        getCoinType().value = type
    }

    fun getWallet(): MutableLiveData<Wallet> {
        if (wallet == null)
            wallet = MutableLiveData()

        return wallet!!
    }

    fun setWallet(wallet: Wallet) {
        getWallet().value = wallet
    }

    fun getPrivateKey(): MutableLiveData<String> {
        if (privateKey == null)
            privateKey = MutableLiveData()

        return privateKey!!
    }

    fun setPrivateKey(privateKey: String) {
        getPrivateKey().value = privateKey
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
