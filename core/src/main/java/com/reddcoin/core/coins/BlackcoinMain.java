package com.reddcoin.core.coins;

import org.bitcoinj.core.Coin;

/**
 * @author Giannis Dzegoutanis
 */
public class BlackcoinMain extends CoinType {
    private BlackcoinMain() {
        id = "blackcoin.main";

        addressHeader = 25;
        p2shHeader = 85;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        spendableCoinbaseDepth = 500;

        name = "Blackcoin (beta)";
        symbol = "BC";
        uriScheme = "blackcoin";
        bip44Index = 10;
        unitExponent = 8;
        feePerKb = Coin.valueOf(10000); // 0.0001 BC
        minNonDust = Coin.valueOf(1);
        softDustLimit = Coin.valueOf(1000000); // 0.01BC
        softDustPolicy = SoftDustPolicy.AT_LEAST_BASE_FEE_IF_SOFT_DUST_TXO_PRESENT;
    }

    private static BlackcoinMain instance = new BlackcoinMain();
    public static synchronized BlackcoinMain get() {
        return instance;
    }
}
