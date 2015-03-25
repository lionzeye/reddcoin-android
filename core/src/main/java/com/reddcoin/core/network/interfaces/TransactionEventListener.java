package com.reddcoin.core.network.interfaces;

import com.reddcoin.core.network.AddressStatus;
import com.reddcoin.core.network.BlockHeader;
import com.reddcoin.core.network.ServerClient;
import org.bitcoinj.core.Transaction;

import java.util.List;

/**
 * @author Giannis Dzegoutanis
 */
public interface TransactionEventListener {
    void onNewBlock(BlockHeader header);

    void onAddressStatusUpdate(AddressStatus status);

    void onUnspentTransactionUpdate(AddressStatus status, List<ServerClient.UnspentTx> unspentTxes);

    void onTransactionHistory(AddressStatus status, List<ServerClient.HistoryTx> historyTxes);

    void onTransactionUpdate(Transaction tx);

    void onTransactionBroadcast(Transaction transaction);

    void onTransactionBroadcastError(Transaction tx, Throwable throwable);
}
