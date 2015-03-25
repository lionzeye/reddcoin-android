package com.reddcoin.core.network;

import com.reddcoin.core.coins.CoinType;

/**
 * @author Giannis Dzegoutanis
 */
public class BlockHeader {
    final CoinType type;
    final long timestamp;
    final int blockHeight;

    /**
     * timestamp in seconds (unix epoch)
     */
    public BlockHeader(CoinType type, long timestamp, int blockHeight) {
        this.type = type;
        this.timestamp = timestamp;
        this.blockHeight = blockHeight;
    }

    public CoinType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getBlockHeight() {
        return blockHeight;
    }
}
