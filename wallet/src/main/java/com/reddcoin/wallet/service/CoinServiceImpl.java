package com.reddcoin.wallet.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.DateUtils;

import com.reddcoin.core.coins.CoinID;
import com.reddcoin.core.coins.CoinType;
import com.reddcoin.core.network.ServerClients;
import com.reddcoin.core.wallet.WalletPocket;
import com.reddcoin.wallet.Configuration;
import com.reddcoin.wallet.Constants;
import com.reddcoin.core.wallet.Wallet;
import com.reddcoin.wallet.WalletApplication;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.CheckForNull;


/**
 * @author Giannis Dzegoutanis
 * @author Andreas Schildbach
 */
public class CoinServiceImpl extends Service implements CoinService {
    private WalletApplication application;
    private Configuration config;

    @CheckForNull
    private ServerClients client;

    private CoinType lastCoin;

    private final Handler handler = new Handler();
    private final Handler delayHandler = new Handler();
    private PowerManager.WakeLock wakeLock;

    private NotificationManager nm;
    private static final int NOTIFICATION_ID_CONNECTED = 0;
    private static final int NOTIFICATION_ID_COINS_RECEIVED = 1;

    private int notificationCount = 0;
    private BigInteger notificationAccumulatedAmount = BigInteger.ZERO;
    private final List<Address> notificationAddresses = new LinkedList<Address>();
    private AtomicInteger transactionsReceived = new AtomicInteger();
    private long serviceCreatedAt;

    private static final int MIN_COLLECT_HISTORY = 2;
    private static final int IDLE_BLOCK_TIMEOUT_MIN = 2;
    private static final int IDLE_TRANSACTION_TIMEOUT_MIN = 9;
    private static final int MAX_HISTORY_SIZE = Math.max(IDLE_TRANSACTION_TIMEOUT_MIN, IDLE_BLOCK_TIMEOUT_MIN);
    private static final long APPWIDGET_THROTTLE_MS = DateUtils.SECOND_IN_MILLIS;

    private static final Logger log = LoggerFactory.getLogger(CoinService.class);

//    private final WalletEventListener walletEventListener = new ThrottlingWalletChangeListener(APPWIDGET_THROTTLE_MS)
//    {
//        @Override
//        public void onThrottledWalletChanged()
//        {
//            notifyWidgets();
//        }
//
//        @Override
//        public void onCoinsReceived(final Wallet wallet, final Transaction tx, final BigInteger prevBalance, final BigInteger newBalance)
//        {
//            transactionsReceived.incrementAndGet();
//
//            final int bestChainHeight = blockChain.getBestChainHeight();
//
//            final Address from = WalletUtils.getFirstFromAddress(tx);
//            final BigInteger amount = tx.getValue(wallet);
//            final TransactionConfidence.ConfidenceType confidenceType = tx.getConfidence().getConfidenceType();
//
//            handler.post(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    final boolean isReceived = amount.signum() > 0;
//                    final boolean replaying = bestChainHeight < bestChainHeightEver;
//                    final boolean isReplayedTx = confidenceType == TransactionConfidence.ConfidenceType.BUILDING && replaying;
//
//                    if (isReceived && !isReplayedTx)
//                        notifyCoinsReceived(from, amount);
//                }
//            });
//        }
//
//        @Override
//        public void onCoinsSent(final Wallet wallet, final Transaction tx, final BigInteger prevBalance, final BigInteger newBalance)
//        {
//            transactionsReceived.incrementAndGet();
//        }
//    };

//    private void notifyCoinsReceived(@Nullable final Address from, @Nonnull final BigInteger amount)
//    {
//        if (notificationCount == 1)
//            nm.cancel(NOTIFICATION_ID_COINS_RECEIVED);
//
//        notificationCount++;
//        notificationAccumulatedAmount = notificationAccumulatedAmount.add(amount);
//        if (from != null && !notificationAddresses.contains(from))
//            notificationAddresses.add(from);
//
//        final int btcPrecision = config.getBtcPrecision();
//        final int btcShift = config.getBtcShift();
//        final String btcPrefix = config.getBtcPrefix();
//
//        final String packageFlavor = application.applicationPackageFlavor();
//        final String msgSuffix = packageFlavor != null ? " [" + packageFlavor + "]" : "";
//
//        final String tickerMsg = getString(R.string.notification_coins_received_msg,
//                btcPrefix + ' ' + GenericUtils.formatValue(amount, btcPrecision, btcShift))
//                + msgSuffix;
//
//        final String msg = getString(R.string.notification_coins_received_msg,
//                btcPrefix + ' ' + GenericUtils.formatValue(notificationAccumulatedAmount, btcPrecision, btcShift))
//                + msgSuffix;
//
//        final StringBuilder text = new StringBuilder();
//        for (final Address address : notificationAddresses)
//        {
//            if (text.length() > 0)
//                text.append(", ");
//
//            final String addressStr = address.toString();
//            final String label = AddressBookProvider.resolveLabel(getApplicationContext(), addressStr);
//            text.append(label != null ? label : addressStr);
//        }
//
//        final NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
//        notification.setSmallIcon(R.drawable.stat_notify_received);
//        notification.setTicker(tickerMsg);
//        notification.setContentTitle(msg);
//        if (text.length() > 0)
//            notification.setContentText(text);
//        notification.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, WalletActivity.class), 0));
//        notification.setNumber(notificationCount == 1 ? 0 : notificationCount);
//        notification.setWhen(System.currentTimeMillis());
//        notification.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.coins_received));
//        nm.notify(NOTIFICATION_ID_COINS_RECEIVED, notification.getNotification());
//    }
//

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        private boolean hasConnectivity;
        private boolean hasStorage = true;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                hasConnectivity = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                log.info("network is " + (hasConnectivity ? "up" : "down"));

                check();
            } else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {
                hasStorage = false;
                log.info("device storage low");

                check();
            } else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
                hasStorage = true;
                log.info("device storage ok");

                check();
            }
        }

        @SuppressLint("Wakelock")
        private void check() {
            Wallet wallet = application.getWallet();
            final boolean hasEverything = hasConnectivity && hasStorage && (wallet != null);

            if (hasEverything && client == null) {
                log.debug("acquiring wakelock");
                wakeLock.acquire();

                log.info("Creating coins clients");
                client = new ServerClients(Constants.DEFAULT_COINS_SERVERS, wallet);
                if (lastCoin != null) client.startAsync(wallet.getPocket(lastCoin));
            }
            else if (!hasEverything && client != null) {
                log.info("stopping stratum client");
                client.stopAllAsync();
                client = null;

                log.debug("releasing wakelock");
                wakeLock.release();
            }
        }
    };

    private final static class ActivityHistoryEntry
    {
        public final int numTransactionsReceived;
        public final int numBlocksDownloaded;

        public ActivityHistoryEntry(final int numTransactionsReceived, final int numBlocksDownloaded)
        {
            this.numTransactionsReceived = numTransactionsReceived;
            this.numBlocksDownloaded = numBlocksDownloaded;
        }

        @Override
        public String toString()
        {
            return numTransactionsReceived + "/" + numBlocksDownloaded;
        }
    }

    private final BroadcastReceiver tickReceiver = new BroadcastReceiver()
    {
        private int lastChainHeight = 0;
        private final List<ActivityHistoryEntry> activityHistory = new LinkedList<ActivityHistoryEntry>();

        @Override
        public void onReceive(final Context context, final Intent intent) {
            log.debug("Received a tick {}", intent);

            if (client != null) {
                client.ping();
            }
//            final int chainHeight = blockChain.getBestChainHeight();
//
//            if (lastChainHeight > 0)
//            {
//                final int numBlocksDownloaded = chainHeight - lastChainHeight;
//                final int numTransactionsReceived = transactionsReceived.getAndSet(0);
//
//                // push history
//                activityHistory.add(0, new ActivityHistoryEntry(numTransactionsReceived, numBlocksDownloaded));
//
//                // trim
//                while (activityHistory.size() > MAX_HISTORY_SIZE)
//                    activityHistory.remove(activityHistory.size() - 1);
//
//                // print
//                final StringBuilder builder = new StringBuilder();
//                for (final ActivityHistoryEntry entry : activityHistory)
//                {
//                    if (builder.length() > 0)
//                        builder.append(", ");
//                    builder.append(entry);
//                }
//                log.info("History of transactions/blocks: " + builder);
//
//                // determine if block and transaction activity is idling
//                boolean isIdle = false;
//                if (activityHistory.size() >= MIN_COLLECT_HISTORY)
//                {
//                    isIdle = true;
//                    for (int i = 0; i < activityHistory.size(); i++)
//                    {
//                        final ActivityHistoryEntry entry = activityHistory.get(i);
//                        final boolean blocksActive = entry.numBlocksDownloaded > 0 && i <= IDLE_BLOCK_TIMEOUT_MIN;
//                        final boolean transactionsActive = entry.numTransactionsReceived > 0 && i <= IDLE_TRANSACTION_TIMEOUT_MIN;
//
//                        if (blocksActive || transactionsActive)
//                        {
//                            isIdle = false;
//                            break;
//                        }
//                    }
//                }
//
//                // if idling, shutdown service
//                if (isIdle)
//                {
//                    log.info("idling detected, stopping service");
//                    stopSelf();
//                }
//            }
//
//            lastChainHeight = chainHeight;
        }
    };

    public class LocalBinder extends Binder
    {
        public CoinService getService()
        {
            return CoinServiceImpl.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(final Intent intent)
    {
        log.debug(".onBind()");

        return mBinder;
    }

    @Override
    public boolean onUnbind(final Intent intent)
    {
        log.debug(".onUnbind()");

        return super.onUnbind(intent);
    }

    @Override
    public void onCreate()
    {
        serviceCreatedAt = System.currentTimeMillis();
        log.debug(".onCreate()");

        super.onCreate();

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final String lockName = getPackageName() + " blockchain sync";

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, lockName);

        application = (WalletApplication) getApplication();
        config = application.getConfiguration();
//        final Wallet wallet = application.getWallet();

//        bestChainHeightEver = config.getBestChainHeightEver();

//        peerConnectivityListener = new PeerConnectivityListener();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        registerReceiver(connectivityReceiver, intentFilter);

//        blockChainFile = new File(getDir("blockstore", Context.MODE_PRIVATE), Constants.BLOCKCHAIN_FILENAME);
//        final boolean blockChainFileExists = blockChainFile.exists();
//
//        if (!blockChainFileExists)
//        {
//            log.info("blockchain does not exist, resetting wallet");
//
//            wallet.clearTransactions(0);
//            wallet.setLastBlockSeenHeight(-1); // magic value
//            wallet.setLastBlockSeenHash(null);
//        }
//
//        try
//        {
//            blockStore = new SPVBlockStore(Constants.NETWORK_PARAMETERS, blockChainFile);
//            blockStore.getChainHead(); // detect corruptions as early as possible
//
//            final long earliestKeyCreationTime = wallet.getEarliestKeyCreationTime();
//
//            if (!blockChainFileExists && earliestKeyCreationTime > 0)
//            {
//                try
//                {
//                    final InputStream checkpointsInputStream = getAssets().open(Constants.CHECKPOINTS_FILENAME);
//                    CheckpointManager.checkpoint(Constants.NETWORK_PARAMETERS, checkpointsInputStream, blockStore, earliestKeyCreationTime);
//                }
//                catch (final IOException x)
//                {
//                    log.error("problem reading checkpoints, continuing without", x);
//                }
//            }
//        }
//        catch (final BlockStoreException x)
//        {
//            blockChainFile.delete();
//
//            final String msg = "blockstore cannot be created";
//            log.error(msg, x);
//            throw new Error(msg, x);
//        }
//
//        log.info("using " + blockStore.getClass().getName());
//
//        try
//        {
//            blockChain = new BlockChain(Constants.NETWORK_PARAMETERS, wallet, blockStore);
//        }
//        catch (final BlockStoreException x)
//        {
//            throw new Error("blockchain cannot be created", x);
//        }

//        application.getWallet().addEventListener(walletEventListener, Threading.SAME_THREAD);
//
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId)
    {
        log.info("service start command: " + intent
                + (intent.hasExtra(Intent.EXTRA_ALARM_COUNT) ? " (alarm count: " + intent.getIntExtra(Intent.EXTRA_ALARM_COUNT, 0) + ")" : ""));

        final String action = intent.getAction();

        if (CoinService.ACTION_CANCEL_COINS_RECEIVED.equals(action)) {
            notificationCount = 0;
            notificationAccumulatedAmount = BigInteger.ZERO;
            notificationAddresses.clear();

            nm.cancel(NOTIFICATION_ID_COINS_RECEIVED);

        } else if (CoinService.ACTION_RESET_WALLET.equals(action)) {
            if (application.getWallet() != null) {
                Wallet wallet = application.getWallet();
                List<CoinType> coinTypesToReset;
                boolean resetWallet = false;
                if (intent.hasExtra(Constants.ARG_COIN_ID)) {
                    CoinType typeToReset = CoinID.typeFromId(intent.getStringExtra(Constants.ARG_COIN_ID));
                    coinTypesToReset = ImmutableList.of(typeToReset);
                } else {
                    coinTypesToReset = wallet.getCoinTypes();
                    resetWallet = true;
                }

                List<WalletPocket> pockets = wallet.refresh(coinTypesToReset);
                if (client != null) {
                    if (resetWallet) {
                        client.stopAllAsync();
                        lastCoin = null;
                        client = new ServerClients(Constants.DEFAULT_COINS_SERVERS, wallet);
                    } else {
                        client.setPockets(pockets, true);
                    }
                }
            } else {
                log.error("Got wallet reset intent, but no wallet is available");
            }
        } else if (CoinService.ACTION_CONNECT_COIN.equals(action)) {
            if (intent.hasExtra(Constants.ARG_COIN_ID)) {
                lastCoin = CoinID.typeFromId(intent.getStringExtra(Constants.ARG_COIN_ID));
                if (client != null && application.getWalletPocket(lastCoin) != null) {
                    client.startAsync(application.getWalletPocket(lastCoin));
                }
            } else {
                log.warn("Missing coin id argument, not doing anything");
            }
        } else if (CoinService.ACTION_BROADCAST_TRANSACTION.equals(action)) {
            final Sha256Hash hash = new Sha256Hash(intent.getByteArrayExtra(CoinService.ACTION_BROADCAST_TRANSACTION_HASH));
            final Transaction tx = null; // FIXME

            if (client != null)
            {
                log.info("broadcasting transaction " + tx.getHashAsString());
                broadcastTransaction(tx);
            }
            else
            {
                log.info("client not available, not broadcasting transaction " + tx.getHashAsString());
            }
        }

        return START_NOT_STICKY;
    }

    private void broadcastTransaction(Transaction tx) {
        // TODO send broadcast message
    }

    @Override
    public void onDestroy()
    {
        log.debug(".onDestroy()");

        unregisterReceiver(tickReceiver);

//        application.getWallet().removeEventListener(walletEventListener);

        if (client != null) {
            client.stopAllAsync();
            client = null;
        }

//        if (peerGroup != null)
//        {
//            peerGroup.removeEventListener(peerConnectivityListener);
//            peerGroup.removeWallet(application.getWallet());
//            peerGroup.stopAndWait();
//
//            log.info("peergroup stopped");
//        }
//
//        peerConnectivityListener.stop();

        unregisterReceiver(connectivityReceiver);

        delayHandler.removeCallbacksAndMessages(null);

        application.saveWalletNow();

        if (wakeLock.isHeld())
        {
            log.debug("wakelock still held, releasing");
            wakeLock.release();
        }

        super.onDestroy();

        log.info("service was up for " + ((System.currentTimeMillis() - serviceCreatedAt) / 1000 / 60) + " minutes");
    }

    @Override
    public void onLowMemory() {
        log.warn("low memory detected, stopping service");
        stopSelf();
    }

    public void notifyWidgets()
    {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

//        final ComponentName providerName = new ComponentName(this, WalletBalanceWidgetProvider.class);
//
//        try
//        {
//            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(providerName);
//
//            if (appWidgetIds.length > 0)
//            {
//                final Wallet wallet = application.getWallet();
//                final BigInteger balance = wallet.getBalance(Wallet.BalanceType.ESTIMATED);
//
//                WalletBalanceWidgetProvider.updateWidgets(this, appWidgetManager, appWidgetIds, balance);
//            }
//        }
//        catch (final RuntimeException x) // system server dead?
//        {
//            log.warn("cannot update app widgets", x);
//        }
    }


}
