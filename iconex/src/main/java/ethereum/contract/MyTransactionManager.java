package ethereum.contract;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by js on 2018. 4. 8..
 */

public class MyTransactionManager extends TransactionManager {

    private static final int SLEEP_DURATION = 1000;
    private static final int ATTEMPTS = 20;


    public MyTransactionManager(
            Web3j web3j, String fromAddress, List<String> privateFor) {
        super(web3j, ATTEMPTS, SLEEP_DURATION, fromAddress);
    }

    @Override
    public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException {
        return null;
    }
}
