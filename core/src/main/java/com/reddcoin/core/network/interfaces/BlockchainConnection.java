package com.reddcoin.core.network.interfaces;

import com.reddcoin.core.network.AddressStatus;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

import java.util.List;

/**
 * @author Giannis Dzegoutanis
 */
public interface BlockchainConnection {
    void subscribeToBlockchain(final TransactionEventListener listener);

    void subscribeToAddresses(List<Address> addresses,
                              TransactionEventListener listener);

    void getUnspentTx(AddressStatus status, TransactionEventListener listener);

    void getHistoryTx(AddressStatus status, TransactionEventListener listener);

    void getTransaction(Sha256Hash txHash, TransactionEventListener listener);

    void broadcastTx(final Transaction tx, final TransactionEventListener listener);

    void ping();

}
