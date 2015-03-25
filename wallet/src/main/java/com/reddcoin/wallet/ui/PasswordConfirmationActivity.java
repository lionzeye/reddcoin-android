package com.reddcoin.wallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.reddcoin.wallet.R;

/**
 * @author Giannis Dzegoutanis
 */
public class PasswordConfirmationActivity extends android.support.v4.app.FragmentActivity implements PasswordConfirmationFragment.Listener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PasswordConfirmationFragment())
                    .commit();
        }
    }

    @Override
    public void onPasswordConfirmed(Bundle args) {
        final Intent result = new Intent();
        result.putExtras(args);
        setResult(RESULT_OK, result);

        // delayed finish
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }
}
