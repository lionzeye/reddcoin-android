package com.reddcoin.core.network;

import com.google.common.collect.ImmutableList;
import com.reddcoin.core.coins.ReddcoinMain;
import com.reddcoin.core.wallet.Wallet;
import com.reddcoin.core.coins.CoinType;
import com.reddcoin.core.wallet.WalletPocket;
import com.reddcoin.stratumj.ServerAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author Giannis Dzegoutanis
 */
public class ServerClients {
    private static final Logger log = LoggerFactory.getLogger(ServerClient.class);

    private HashMap<CoinType, ServerClient> connections;

    public ServerClients(ImmutableList<ServerAddress> serverAddresses, Wallet wallet) {
        connections = new HashMap<CoinType, ServerClient>(serverAddresses.size());

        ServerClient client = new ServerClient(serverAddresses);
        connections.put(ReddcoinMain.get(), client);

        setPockets(wallet.getPockets(), false);
    }

    public void setPockets(List<WalletPocket> pockets, boolean reconnect) {
        for (WalletPocket pocket : pockets) {
            if (!connections.containsKey(pocket.getCoinType())) continue;
            connections.get(pocket.getCoinType()).setWalletPocket(pocket, reconnect);
        }
    }

    public void startAllAsync() {
        for (ServerClient client : connections.values()) {
            client.startAsync();
        }
    }

    public void startAsync(WalletPocket pocket) {
        CoinType type = pocket.getCoinType();
        if (connections.containsKey(type)) {
            ServerClient c = connections.get(type);
            c.maybeSetWalletPocket(pocket);
            c.startAsync();
        } else {
            log.warn("No connection found for {}", type.getName());
        }
    }

    public void stopAllAsync() {
        for (ServerClient client : connections.values()) {
            client.stopAsync();
        }
    }

    public void ping() {
        for (final CoinType type : connections.keySet()) {
            ServerClient connection = connections.get(type);
            if (connection.isConnected()) connection.ping();
        }
    }
}
