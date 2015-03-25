package com.reddcoin.wallet.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.reddcoin.wallet.Constants;
import com.reddcoin.wallet.R;


public class IntroActivity extends android.support.v4.app.FragmentActivity implements WelcomeFragment.Listener, PasswordConfirmationFragment.Listener, SetPasswordFragment.Listener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new WelcomeFragment())
                    .commit();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onCreateNewWallet() {
        replaceFragment(new SeedFragment());
    }

    @Override
    public void onRestoreWallet() {
        replaceFragment(RestoreFragment.newInstance());
    }

    @Override
    public void onRestoreWallet(String seed) {
        replaceFragment(RestoreFragment.newInstance(seed));
    }

    @Override
    public void onSetPassword(String seed) {
        replaceFragment(SetPasswordFragment.newInstance(seed));
    }

    @Override
    public void onConfirmPassword(String seed, boolean isSeedProtected) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_SEED, seed);
        args.putBoolean(Constants.ARG_SEED_PROTECT, isSeedProtected);
        if (isSeedProtected) {
            replaceFragment(PasswordConfirmationFragment.newInstance(
                    getResources().getString(R.string.password_wallet_recovery), args));
        } else {
            replaceFragment(PasswordConfirmationFragment.newInstance(
                    getResources().getString(R.string.set_password_info), args));
        }
    }

    @Override
    public void onPasswordConfirmed(Bundle args) {
        finalizeWalletRestoration(args);
    }

    @Override
    public void onPasswordSet(Bundle args) {
        finalizeWalletRestoration(args);
    }

    private void finalizeWalletRestoration(Bundle args) {
        replaceFragment(FinalizeWalletRestorationFragment.newInstance(args));
    }
}
