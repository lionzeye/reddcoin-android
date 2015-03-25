package com.reddcoin.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.reddcoin.wallet.Constants;
import com.reddcoin.wallet.R;
import com.reddcoin.wallet.WalletApplication;

import javax.annotation.Nullable;

public class SignTransactionActivity extends android.support.v4.app.FragmentActivity
        implements SignTransactionFragment.Listener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_transaction);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, SignTransactionFragment.newInstance(getIntent().getExtras()))
                .commit();
    }

    protected WalletApplication getWalletApplication() {
        return (WalletApplication) getApplication();
    }

    @Override
    public void onSignResult(@Nullable Exception error) {
        final Intent result = new Intent();
        result.putExtra(Constants.ARG_ERROR, error);
        setResult(RESULT_OK, result);

        // delayed finish
        new Handler().post(new Runnable()
        {
            @Override
            public void run()
            {
                finish();
            }
        });
    }
}
