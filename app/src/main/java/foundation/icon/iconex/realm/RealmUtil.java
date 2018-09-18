package foundation.icon.iconex.realm;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.control.Contacts;
import foundation.icon.iconex.control.RecentSendInfo;
import foundation.icon.iconex.realm.data.CoinNToken;
import foundation.icon.iconex.realm.data.ETHContacts;
import foundation.icon.iconex.realm.data.ICXContacts;
import foundation.icon.iconex.realm.data.RecentETHSend;
import foundation.icon.iconex.realm.data.RecentICXSend;
import foundation.icon.iconex.token.Token;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import io.realm.Realm;
import io.realm.RealmList;
import loopchain.icon.wallet.core.Constants;

import static foundation.icon.iconex.MyConstants.CoinType.ICX;

/**
 * Created by js on 2018. 3. 5..
 */

public class RealmUtil {
    private static final String TAG = RealmUtil.class.getSimpleName();

    public static void loadWallet() throws Exception {
        Realm realm = Realm.getDefaultInstance();
        ICONexApp.mWallets = new ArrayList<>();
        if (realm.where(foundation.icon.iconex.realm.data.Wallet.class).count() > 0) {
            for (foundation.icon.iconex.realm.data.Wallet wallet : realm.where(foundation.icon.iconex.realm.data.Wallet.class).findAll()) {
                Wallet walletInfo = new Wallet();
                walletInfo.setCoinType(wallet.getCoinType());
                walletInfo.setAlias(wallet.getAlias());
                walletInfo.setAddress(wallet.getAddress());
                walletInfo.setKeyStore(wallet.getKeyStore());

                List<CoinNToken> list = wallet.getCoinNToken();
                List<WalletEntry> entries = new ArrayList<>();
                SecureRandom random = new SecureRandom();
                for (CoinNToken coinNToken : list) {
                    WalletEntry entry = new WalletEntry();
                    entry.setType(coinNToken.getType());
                    entry.setName(coinNToken.getName());
                    entry.setAddress(coinNToken.getAddress());
                    entry.setSymbol(coinNToken.getSymbol());
                    entry.setBalance("");
                    entry.setId(new Random().nextInt(999999) + 1000000);

                    if (coinNToken.getType().equals(MyConstants.TYPE_TOKEN)) {
                        entry.setContractAddress(coinNToken.getContractAddress());
                        entry.setUserName(coinNToken.getUserName());
                        entry.setUserSymbol(coinNToken.getUserSymbol());
                        entry.setDefaultDec(coinNToken.getDecimal());
                        entry.setUserDec(coinNToken.getUserDecimal());
                        entry.setCreatedAt(coinNToken.getCreateAt());
                    } else {
                        entry.setDefaultDec(18);
                        entry.setUserDec(18);
                        entry.setUserName(coinNToken.getName());
                        entry.setUserSymbol(coinNToken.getSymbol());
                    }
                    entries.add(entry);
                }
                walletInfo.setWalletEntries(entries);
                walletInfo.setCreatedAt(wallet.getCreateAt());
                ICONexApp.mWallets.add(walletInfo);
            }

            Collections.reverse(ICONexApp.mWallets);

            makeExchanges();
        }

        realm.close();
    }

    public static void addWallet(Wallet wallet) throws Exception {
        Realm realm = Realm.getDefaultInstance();
        Number currentMaxNId = realm.where(foundation.icon.iconex.realm.data.Wallet.class).max("id");
        int nextId;
        if (currentMaxNId == null)
            nextId = 1;
        else
            nextId = currentMaxNId.intValue() + 1;

        realm.beginTransaction();
        foundation.icon.iconex.realm.data.Wallet mWallet = realm.createObject(foundation.icon.iconex.realm.data.Wallet.class, nextId);
        mWallet.setCoinType(wallet.getCoinType());
        mWallet.setAlias(wallet.getAlias());
        mWallet.setAddress(wallet.getAddress());
        mWallet.setKeyStore(wallet.getKeyStore());
        mWallet.setCreateAt(wallet.getCreatedAt());

        RealmList<CoinNToken> list = mWallet.getCoinNToken();
        for (WalletEntry entry : wallet.getWalletEntries()) {
            if (entry.getType().equals(MyConstants.TYPE_TOKEN)) {
                CoinNToken token = realm.createObject(CoinNToken.class);
                token.setType(MyConstants.TYPE_TOKEN);
                token.setAddress(entry.getAddress());
                token.setContractAddress(entry.getContractAddress());
                token.setDecimal(entry.getDefaultDec());
                token.setUserDecimal(entry.getUserDec());
                token.setName(entry.getName());
                token.setUserName(entry.getUserName());
                token.setSymbol(entry.getSymbol());
                token.setUserSymbol(entry.getUserSymbol());
                token.setCreateAt(entry.getCreatedAt());

                list.add(token);
            } else if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                CoinNToken coin = realm.createObject(CoinNToken.class);
                if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                    coin.setType(MyConstants.TYPE_COIN);
                    coin.setName(MyConstants.NAME_ICX);
                    coin.setUserName(MyConstants.NAME_ICX);
                    coin.setAddress(wallet.getAddress());
                    coin.setSymbol(Constants.KS_COINTYPE_ICX);
                    coin.setUserSymbol(Constants.KS_COINTYPE_ICX);
                } else {
                    coin.setType(MyConstants.TYPE_COIN);
                    coin.setName(MyConstants.NAME_ETH);
                    coin.setUserSymbol(MyConstants.NAME_ETH);
                    coin.setAddress(wallet.getAddress());
                    coin.setSymbol(Constants.KS_COINTYPE_ETH);
                    coin.setUserSymbol(Constants.KS_COINTYPE_ETH);
                }

                coin.setDecimal(18);
                coin.setUserDecimal(18);

                list.add(coin);
            }
        }

        addICXToken(wallet.getCoinType(), realm, list, mWallet.getAddress(), mWallet.getCreateAt());

        realm.commitTransaction();

        realm.close();
    }

    private static void addICXToken(String coinType, Realm realm, RealmList<CoinNToken> list, String ownAddr, String createdAt) {
        boolean isExist = false;
        String contractAddr;
        if (ICONexApp.network == MyConstants.NETWORK_MAIN)
            contractAddr = MyConstants.M_ERC_ICX_ADDR;
        else
            contractAddr = MyConstants.T_ERC_ICX_ADDR;

        if (coinType.equals(Constants.KS_COINTYPE_ICX))
            return;

        for (CoinNToken entry : list) {
            if (entry.getContractAddress().equals(contractAddr))
                isExist = true;
        }

        if (!isExist) {
            CoinNToken token = realm.createObject(CoinNToken.class);
            token.setType(MyConstants.TYPE_TOKEN);
            token.setAddress(ownAddr);
            token.setContractAddress(contractAddr);
            token.setName(MyConstants.NAME_ICX);
            token.setUserName(MyConstants.NAME_ICX);
            token.setSymbol(MyConstants.ICX_SYM);
            token.setUserSymbol(MyConstants.ICX_SYM);
            token.setDecimal(MyConstants.ICX_DEC);
            token.setUserDecimal(MyConstants.ICX_DEC);
            token.setCreateAt(createdAt);

            list.add(token);
        }
    }

    public static void overwriteWallet(String address, Wallet wallet) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        foundation.icon.iconex.realm.data.Wallet ownWallet = realm.where(foundation.icon.iconex.realm.data.Wallet.class).equalTo("address", address).findFirst();
        ownWallet.setAlias(wallet.getAlias());
        ownWallet.setKeyStore(wallet.getKeyStore());
        ownWallet.setCreateAt(wallet.getCreatedAt());
        realm.commitTransaction();

        realm.close();
    }

    public static void modWalletAlias(String address, String newAlias) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        foundation.icon.iconex.realm.data.Wallet wallet = realm.where(foundation.icon.iconex.realm.data.Wallet.class).equalTo("address", address).findFirst();
        wallet.setAlias(newAlias);
        realm.commitTransaction();

        realm.close();
    }

    public static void modWalletPassword(String address, String keyStore) throws Exception {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        foundation.icon.iconex.realm.data.Wallet wallet = realm.where(foundation.icon.iconex.realm.data.Wallet.class).equalTo("address", address).findFirst();
        wallet.setKeyStore(keyStore);
        realm.commitTransaction();

        realm.close();
    }

    public static void removeWallet(String address) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        foundation.icon.iconex.realm.data.Wallet wallet = realm.where(foundation.icon.iconex.realm.data.Wallet.class).equalTo("address", address).findFirst();
        wallet.deleteFromRealm();
        realm.commitTransaction();

        realm.close();
    }

    public static void makeExchanges() {
        for (Wallet info : ICONexApp.mWallets) {
            for (WalletEntry entry : info.getWalletEntries()) {
                String symbol = entry.getSymbol().toLowerCase();
                if (!ICONexApp.EXCHANGES.contains(symbol))
                    ICONexApp.EXCHANGES.add(symbol);
            }
        }
    }

    public static void addToken(String walletAddress, Token token) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        foundation.icon.iconex.realm.data.Wallet wallet = realm.where(foundation.icon.iconex.realm.data.Wallet.class).equalTo("address", walletAddress).findFirst();
        RealmList<CoinNToken> entries = wallet.getCoinNToken();
        CoinNToken newToken = realm.createObject(CoinNToken.class);
        newToken.setType(MyConstants.TYPE_TOKEN);
        newToken.setAddress(walletAddress);
        newToken.setContractAddress(token.getContractAddress());
        newToken.setName(token.getDefaultName());
        newToken.setUserName(token.getUserName());
        newToken.setSymbol(token.getDefaultSymbol());
        newToken.setUserSymbol(token.getUserSymbol());
        newToken.setDecimal(token.getDefaultDec());
        newToken.setUserDecimal(token.getUserDec());
        newToken.setCreateAt(getTimeStamp());

        entries.add(newToken);

        realm.commitTransaction();

        realm.close();
    }

    public static void modToken(String own, String contract, String name, String symbol, int dec) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        foundation.icon.iconex.realm.data.Wallet wallet = realm.where(foundation.icon.iconex.realm.data.Wallet.class).equalTo("address", own).findFirst();
        RealmList<CoinNToken> list = wallet.getCoinNToken();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getContractAddress() != null
                    && list.get(i).getContractAddress().equals(contract)) {
                list.get(i).setUserName(name);
                list.get(i).setUserSymbol(symbol);
                list.get(i).setUserDecimal(dec);

                break;
            }
        }
        realm.commitTransaction();

        realm.close();
    }

    public static void deleteToken(String own, String contract) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        foundation.icon.iconex.realm.data.Wallet wallet = realm.where(foundation.icon.iconex.realm.data.Wallet.class).equalTo("address", own).findFirst();
        RealmList<CoinNToken> list = wallet.getCoinNToken();
        for (CoinNToken token : list) {
            if (token.getContractAddress() != null
                    && contract.equals(token.getContractAddress())) {
                list.remove(token);
                break;
            }
        }

        realm.commitTransaction();

        realm.close();
    }

    public static void addContacts(MyConstants.CoinType type, String name, String address) {
        Realm realm = Realm.getDefaultInstance();

        Number currentMaxId;
        int nextId;
        if (type == ICX) {
            currentMaxId = realm.where(ICXContacts.class).max("id");
            if (currentMaxId == null)
                nextId = 0;
            else
                nextId = currentMaxId.intValue() + 1;

            realm.beginTransaction();
            ICXContacts contact = realm.createObject(ICXContacts.class, nextId);
            contact.setName(name);
            contact.setAddress(address);
            realm.commitTransaction();

            realm.close();

        } else {
            currentMaxId = realm.where(ETHContacts.class).max("id");
            if (currentMaxId == null)
                nextId = 0;
            else
                nextId = currentMaxId.intValue() + 1;

            realm.beginTransaction();
            ETHContacts contact = realm.createObject(ETHContacts.class, nextId);
            contact.setName(name);
            contact.setAddress(address);
            realm.commitTransaction();

            realm.close();
        }
    }

    public static void loadContacts() {
        Realm realm = Realm.getDefaultInstance();
        if (realm.where(ICXContacts.class).count() > 0) {
            ICONexApp.ICXContacts = new ArrayList<>();
            for (ICXContacts contacts : realm.where(ICXContacts.class).findAll()) {
                Contacts contact = new Contacts();
                contact.setName(contacts.getName());
                contact.setAddress(contacts.getAddress());
                ICONexApp.ICXContacts.add(contact);
            }
        } else {
            ICONexApp.ICXContacts = new ArrayList<>();
        }

        if (realm.where(ETHContacts.class).count() > 0) {
            ICONexApp.ETHContacts = new ArrayList<>();
            for (ETHContacts contacts : realm.where(ETHContacts.class).findAll()) {
                Contacts contact = new Contacts();
                contact.setName(contacts.getName());
                contact.setAddress(contacts.getAddress());
                ICONexApp.ETHContacts.add(contact);
            }
        } else {
            ICONexApp.ETHContacts = new ArrayList<>();
        }

        realm.close();
    }

    public static void addRecentSend(MyConstants.CoinType type, String txHash, String name, String address, String date, String amount, String symbol) {
        Realm realm = Realm.getDefaultInstance();

        Number currentMaxId;
        int nextId;
        if (type == ICX) {
            currentMaxId = realm.where(RecentICXSend.class).max("id");
            if (currentMaxId == null)
                nextId = 0;
            else
                nextId = currentMaxId.intValue() + 1;

            realm.beginTransaction();
            RecentICXSend send = realm.createObject(RecentICXSend.class, nextId);
            send.setName(name);
            send.setAddress(address);
            send.setAmount(amount);
            send.setDate(date);
            send.setSymbol(symbol);
            send.setTxHash(txHash);
            send.setIsDone(MyConstants.TX_PENDING);
            realm.commitTransaction();

            realm.close();

        } else {
            currentMaxId = realm.where(RecentETHSend.class).max("id");
            if (currentMaxId == null)
                nextId = 0;
            else
                nextId = currentMaxId.intValue() + 1;

            realm.beginTransaction();
            RecentETHSend send = realm.createObject(RecentETHSend.class, nextId);
            send.setName(name);
            send.setAddress(address);
            send.setAmount(amount);
            send.setDate(date);
            send.setSymbol(symbol);
            send.setIsDone(MyConstants.TX_PENDING);
            realm.commitTransaction();

            realm.close();
        }
    }

    public static void deleteContact(String coinType, String name) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        if (coinType.equals(Constants.KS_COINTYPE_ICX)) {
            ICXContacts contact = realm.where(ICXContacts.class).equalTo("name", name).findFirst();
            contact.deleteFromRealm();
        } else {
            ETHContacts contact = realm.where(ETHContacts.class).equalTo("name", name).findFirst();
            contact.deleteFromRealm();
        }
        realm.commitTransaction();

        realm.close();
    }

    public static void modifyContact(String coinType, String address, String newName) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        if (coinType.equals(Constants.KS_COINTYPE_ICX)) {
            ICXContacts contact = realm.where(ICXContacts.class).equalTo("address", address).findFirst();
            contact.setName(newName);
        } else {
            ETHContacts contact = realm.where(ETHContacts.class).equalTo("address", address).findFirst();
            contact.setName(newName);

        }
        realm.commitTransaction();

        realm.close();
    }

    public static void loadRecents() {
        Realm realm = Realm.getDefaultInstance();
        if (realm.where(RecentICXSend.class).count() > 0) {
            ICONexApp.ICXSendInfo = new ArrayList<>();
            for (RecentICXSend send : realm.where(RecentICXSend.class).findAll()) {
                RecentSendInfo sendInfo = new RecentSendInfo();
                sendInfo.setTxHash(send.getTxHash());
                sendInfo.setName(send.getName());
                sendInfo.setAddress(send.getAddress());
                sendInfo.setDate(send.getDate());
                sendInfo.setAmount(send.getAmount());
                sendInfo.setSymbol(send.getSymbol());
                sendInfo.setIsDone(send.getIsDone());

                ICONexApp.ICXSendInfo.add(sendInfo);
            }

            Collections.reverse(ICONexApp.ICXSendInfo);
        }

        if (realm.where(RecentETHSend.class).count() > 0) {
            ICONexApp.ETHSendInfo = new ArrayList<>();
            for (RecentETHSend send : realm.where(RecentETHSend.class).findAll()) {
                RecentSendInfo sendInfo = new RecentSendInfo();
                sendInfo.setTxHash(send.getTxHash());
                sendInfo.setName(send.getName());
                sendInfo.setAddress(send.getAddress());
                sendInfo.setDate(send.getDate());
                sendInfo.setAmount(send.getAmount());
                sendInfo.setSymbol(send.getSymbol());
                sendInfo.setIsDone(send.getIsDone());

                ICONexApp.ETHSendInfo.add(sendInfo);
            }

            Collections.reverse(ICONexApp.ETHSendInfo);
        }

        realm.close();
    }

    private static String getTimeStamp() {
        long time = System.currentTimeMillis();
        return Long.toString(time);
    }
}
