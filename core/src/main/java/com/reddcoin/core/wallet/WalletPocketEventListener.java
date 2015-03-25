package com.reddcoin.core.wallet;

import org.bitcoinj.core.*;

/**
 * @author Giannis Dzegoutanis
 */
public interface WalletPocketEventListener {

    void onNewBalance(Coin newBalance, Coin pendingAmount);

    void onNewBlock(WalletPocket pocket);

    void onTransactionConfidenceChanged(WalletPocket pocket, Transaction tx);

    void onPocketChanged(final WalletPocket pocket);

    void onConnectivityStatus(final WalletPocketConnectivity pocketConnectivity);
}
