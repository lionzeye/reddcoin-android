package com.reddcoin.wallet.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.reddcoin.core.coins.CoinType;
import com.reddcoin.wallet.WalletApplication;
import com.reddcoin.wallet.ui.widget.NavDrawerItem;

import javax.annotation.Nonnull;

/**
 * @author Giannis Dzegoutanis
 */
public class NavDrawerListAdapter extends BaseAdapter {
    private final Context context;
    private final WalletApplication application;

    public NavDrawerListAdapter(final Context context, @Nonnull final WalletApplication application) {
        this.context = context;
        this.application = application;
    }

    @Override
    public int getCount() {
        if (application.getWallet() != null) {
            return application.getWallet().getCoinTypes().size();
        }
        return 0;
    }

    @Override
    public CoinType getItem(int position) {
        if (application.getWallet() != null) {
            return application.getWallet().getCoinTypes().get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
        if (row == null) {
            row = new NavDrawerItem(context);
        }

        CoinType coinType = getItem(position);
        if (coinType != null) ((NavDrawerItem) row).setCoin(coinType);

        return row;
    }


}