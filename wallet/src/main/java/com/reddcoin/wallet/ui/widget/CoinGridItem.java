package com.reddcoin.wallet.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.reddcoin.core.coins.CoinType;
import com.reddcoin.wallet.Constants;
import com.reddcoin.wallet.R;

/**
 * @author Giannis Dzegoutanis
 */
public class CoinGridItem extends LinearLayout {
    private final TextView title;
    private final ImageView icon;
    private final View view;

    public CoinGridItem(Context context) {
        super(context);

        view = LayoutInflater.from(context).inflate(R.layout.coin_grid_item, this, true);
        title = (TextView) findViewById(R.id.item_text);
        icon = (ImageView) findViewById(R.id.item_icon);
    }

    public void setCoin(CoinType coin) {
        title.setText(coin.getName());
        icon.setImageResource(Constants.COINS_ICON);
    }
}
