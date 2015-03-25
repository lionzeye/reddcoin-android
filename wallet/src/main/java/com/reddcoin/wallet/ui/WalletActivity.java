package com.reddcoin.wallet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.reddcoin.core.coins.CoinID;
import com.reddcoin.core.coins.CoinType;
import com.reddcoin.core.uri.CoinURI;
import com.reddcoin.core.uri.CoinURIParseException;
import com.reddcoin.wallet.Constants;
import com.reddcoin.wallet.R;
import com.reddcoin.wallet.WalletApplication;
import com.reddcoin.wallet.service.CoinService;
import com.reddcoin.wallet.service.CoinServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Giannis Dzegoutanis
 * @author Andreas Schildbach
 */
final public class WalletActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final Logger log = LoggerFactory.getLogger(WalletActivity.class);

    private static final int RECEIVE = 0;
    private static final int INFO = 1;
    private static final int SEND = 2;

    private static final int REQUEST_CODE_SCAN = 0;
    private static final int ADD_COIN = 1;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private int coinIconRes = R.drawable.ic_launcher;

    /**
     * For SharedPreferences, used to check if first launch ever.
     */
    private ViewPager mViewPager;
    private AsyncTask<Void, Void, Void> refreshTask;
    private CoinType currentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        if (getWalletApplication().getWallet() == null) {
            startIntro();
            finish();
            return;
        }
        mTitle = getTitle();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        // Set OffscreenPageLimit to 2 because receive fragment draws a QR code and we don't
        // want to re-render that if we go to the SendFragment and back
        mViewPager.setOffscreenPageLimit(2);

        // Get the last used wallet pocket and select it
        CoinType lastPocket = getWalletApplication().getConfiguration().getLastPocket();
        mNavigationDrawerFragment.selectCoinInit(lastPocket);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWalletApplication().startBlockchainService(CoinService.ServiceMode.CANCEL_COINS_RECEIVED);

        //TODO
//        checkLowStorageAlert();
    }

    protected WalletApplication getWalletApplication() {
        return (WalletApplication) getApplication();
    }

    @Override
    public void onNavigationDrawerCoinSelected(CoinType coinType) {
        log.info("Coin selected {}", coinType);

        openPocket(coinType);
    }

    @Override
    public void onNavigationDrawerAddCoinsSelected() {
        startActivityForResult(new Intent(WalletActivity.this, AddCoinsActivity.class), ADD_COIN);
    }

    private void openPocket(CoinType coinType) {
        if (mViewPager != null && !coinType.equals(currentType)) {
            currentType = coinType;
            mTitle = coinType.getName();
            coinIconRes = Constants.COINS_ICON;
            AppSectionsPagerAdapter adapter = new AppSectionsPagerAdapter(this, coinType);
            mViewPager.setAdapter(adapter);
            mViewPager.setCurrentItem(INFO);
            mViewPager.getAdapter().notifyDataSetChanged();
            getWalletApplication().getConfiguration().touchLastPocket(coinType);

            // Open connection if needed or possible
            Intent intent = new Intent(CoinService.ACTION_CONNECT_COIN, null,
                    getWalletApplication(), CoinServiceImpl.class);
            intent.putExtra(Constants.ARG_COIN_ID, currentType.getId());
            getWalletApplication().startService(intent);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setIcon(coinIconRes);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                final String input = intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);

                try {
                    final CoinURI coinUri = new CoinURI(input);
                    CoinType scannedType = coinUri.getType();



                    setSendFromCoin(coinUri);
                } catch (final CoinURIParseException e) {
                    String error = getResources().getString(R.string.uri_error, e.getMessage());
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == ADD_COIN) {
            if (resultCode == Activity.RESULT_OK) {
                mNavigationDrawerFragment.notifyDataSetChanged();
                CoinType type = CoinID.typeFromId(intent.getStringExtra(Constants.ARG_COIN_ID));
                mNavigationDrawerFragment.selectItem(type);
            }
        }
    }

    private void setSendFromCoin(CoinURI coinUri) throws CoinURIParseException {
        mNavigationDrawerFragment.selectItem(coinUri.getType());
        if (mViewPager != null) {
            mViewPager.setCurrentItem(SEND);
            AppSectionsPagerAdapter adapter = (AppSectionsPagerAdapter) mViewPager.getAdapter();
            SendFragment send = (SendFragment) adapter.getItem(SEND);
            send.updateStateFrom(coinUri.getAddress(), coinUri.getAmount(), coinUri.getLabel());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mNavigationDrawerFragment != null && !mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // TODO launch settings here
            return true;
        } else if (id == R.id.action_restore_wallet) {
            startRestore();
            finish();
            return true;
        } else if (id == R.id.action_scan_qr_code) {
            startActivityForResult(new Intent(this, ScanActivity.class), REQUEST_CODE_SCAN);
            return true;
        } else if (id == R.id.action_refresh_wallet) {
            refreshWallet();
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(WalletActivity.this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshWallet() {
        if (getWalletApplication().getWallet() != null) {
            Intent intent = new Intent(CoinService.ACTION_RESET_WALLET, null,
                    getWalletApplication(), CoinServiceImpl.class);
            intent.putExtra(Constants.ARG_COIN_ID, currentType.getId());
            getWalletApplication().startService(intent);
            // FIXME, we get a crash if the activity is not restarted
            Intent introIntent = new Intent(WalletActivity.this, WalletActivity.class);
            startActivity(introIntent);
            finish();
        }
    }

    private void startIntro() {
        Intent introIntent = new Intent(this, IntroActivity.class);
        startActivity(introIntent);
    }

    private void startRestore() {
        startActivity(new Intent(this, IntroActivity.class));
    }

    private static class AppSectionsPagerAdapter extends FragmentStatePagerAdapter {

        private final CoinType type;
        private final WalletActivity walletActivity;
        private RequestFragment request;
        private SendFragment send;
        private BalanceFragment balance;

        public AppSectionsPagerAdapter(WalletActivity walletActivity, CoinType type) {
            super(walletActivity.getSupportFragmentManager());
            this.walletActivity = walletActivity;
            this.type = type;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case RECEIVE:
                    if (request == null) request = RequestFragment.newInstance(type);
                    return request;
                case SEND:
                    if (send == null) send = SendFragment.newInstance(type);
                    return send;
                case INFO:
                default:
                    if (balance == null) balance = BalanceFragment.newInstance(type);
                    return balance;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case RECEIVE:
                    return walletActivity.getString(R.string.wallet_title_request);
                case SEND:
                    return walletActivity.getString(R.string.wallet_title_send);
                case INFO:
                default:
                    return walletActivity.getString(R.string.wallet_title_balance);
            }
        }
    }
}
