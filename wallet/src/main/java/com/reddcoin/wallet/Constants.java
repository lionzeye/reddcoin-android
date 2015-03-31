package com.reddcoin.wallet;

import com.reddcoin.core.coins.CoinType;
import com.reddcoin.core.coins.ReddcoinMain;
import com.reddcoin.stratumj.ServerAddress;
import com.google.common.collect.ImmutableList;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author Giannis Dzegoutanis
 * @author Andreas Schildbach
 */
public class Constants {

    public static final String ARG_SEED = "seed";
    public static final String ARG_SEED_PROTECT = "seed_protect";
    public static final String ARG_PASSWORD = "password";
    public static final String ARG_SEND_TO_ADDRESS = "send_to_address";
    public static final String ARG_SEND_AMOUNT = "send_amount";
    public static final String ARG_COIN_ID = "coin_id";
    public static final String ARG_TRANSACTION_ID = "transaction_id";
    public static final String ARG_ERROR = "error";
    public static final String ARG_MESSAGE = "message";

    public static final String WALLET_FILENAME_PROTOBUF = "wallet";
    public static final long WALLET_WRITE_DELAY = 3;
    public static final TimeUnit WALLET_WRITE_DELAY_UNIT = TimeUnit.SECONDS;

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

    public static final char CHAR_HAIR_SPACE = '\u200a';
    public static final char CHAR_THIN_SPACE = '\u2009';
    public static final char CHAR_ALMOST_EQUAL_TO = '\u2248';
    public static final char CHAR_CHECKMARK = '\u2713';
    public static final String CURRENCY_PLUS_SIGN = "+" + CHAR_THIN_SPACE;
    public static final String CURRENCY_MINUS_SIGN = "-" + CHAR_THIN_SPACE;

    // TODO move to resource files
    public static final ImmutableList<ServerAddress> COIN_SERVERS = ImmutableList.of(new ServerAddress("rdd-cce-1.coinomi.net", 5014), new ServerAddress("reddwallet.org", 50001));

    public static final Integer COINS_ICON = R.drawable.reddcoin;
    public static final String COIN_BLOCK_EXPLORER = "http://live.reddcoin.com/tx/%s";

    public static final CoinType DEFAULT_COIN = ReddcoinMain.get();
}
