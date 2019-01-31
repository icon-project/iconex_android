package ethereum;

import android.support.annotation.NonNull;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigInteger;

import foundation.icon.MyConstants;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;

public class Ethereum {

    private final Web3j web3j;

    public Ethereum(@NonNull int network) {
        String url;
        if (network == MyConstants.NETWORK_MAIN)
            url = ServiceConstants.ETH_HOST;
        else
            url = ServiceConstants.ETH_ROP_HOST;

        web3j = Web3jFactory.build(new HttpService(url));
    }

    public BigInteger estimateGasLimit(String address, String price, String limit, String amount, int decimals, String data) throws Exception {
        EthGetTransactionCount nonce = web3j.ethGetTransactionCount(checkPrefix(address), DefaultBlockParameterName.LATEST).send();
        EthEstimateGas estimateGas = web3j.ethEstimateGas(Transaction.createFunctionCallTransaction(checkPrefix(address),
                nonce.getTransactionCount(),
                Convert.toWei(price, Convert.Unit.GWEI).toBigInteger(),
                new BigInteger(limit), checkPrefix(address),
                ConvertUtil.valueToBigInteger(amount, decimals),
                data)).send();

        return estimateGas.getAmountUsed();
    }

    private String checkPrefix(String addr) {
        if (addr.startsWith("0x"))
            return addr;
        else
            return "0x" + addr;
    }
}
