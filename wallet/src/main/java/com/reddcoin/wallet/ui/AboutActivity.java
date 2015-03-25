package com.reddcoin.wallet.ui;

import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.reddcoin.wallet.R;

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        TextView version = (TextView) findViewById(R.id.about_version);
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            version.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            version.setVisibility(View.INVISIBLE);
        }
    }
}