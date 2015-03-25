package com.reddcoin.core.coins;

import org.bitcoinj.core.Coin;

/**
 * @author Giannis Dzegoutanis
 */
public class MonacoinMain extends CoinType {
    private MonacoinMain() {
        id = "monacoin.main";

        addressHeader = 50;
        p2shHeader = 5;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        spendableCoinbaseDepth = 100;

        name = "Monacoin";
        symbol = "MONA";
        uriScheme = "monacoin";
        bip44Index = 12;
        // TODO set correct values
        feePerKb = Coin.valueOf(1);
        minNonDust = Coin.valueOf(1000);
        unitExponent = 8;
        throw new RuntimeException(name+" bip44Index " + bip44Index + "is not standardized");
    }

    private static MonacoinMain instance = new MonacoinMain();
    public static synchronized MonacoinMain get() {
        return instance;
    }
}
