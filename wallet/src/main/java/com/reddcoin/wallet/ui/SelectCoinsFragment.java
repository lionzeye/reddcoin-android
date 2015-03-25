package com.reddcoin.wallet.ui;



import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.reddcoin.core.coins.CoinType;
import com.reddcoin.wallet.R;
import com.reddcoin.wallet.WalletApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fragment that restores a wallet
 */
public class SelectCoinsFragment extends Fragment {
    private static final Logger log = LoggerFactory.getLogger(SelectCoinsFragment.class);
    private GridView coinsGridView;
    private Listener mListener;

    public SelectCoinsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_coins, container, false);

        coinsGridView = (GridView) view.findViewById(R.id.coins_grid);
        coinsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = parent.getItemAtPosition(position);

                if (obj != null && obj instanceof CoinType) {
                    selectItem((CoinType)obj);
                }
            }
        });
        coinsGridView.setAdapter(new CoinsListAdapter(getActivity(), getWalletApplication()));

        return view;
    }

    private void selectItem(CoinType type) {
        if (mListener != null) {
            mListener.onCoinSelected(type);
        }
    }

    WalletApplication getWalletApplication() {
        return (WalletApplication) getActivity().getApplication();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface Listener {
        public void onCoinSelected(CoinType type);
    }
}
