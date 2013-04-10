
package com.lenovo.settings;

import com.lenovo.settings.Util.TransponderUtil;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class SearchMenuFragment extends Fragment implements OnClickListener {
    private View mMainView;
    private TextView mSearchAutoTextView;
    private TextView mSearchFullTextView;
    private TextView mSearchManualTextView;
    private TextView mSearchRessetTextView;
    private SettingFragment settingFragment;
    public static final int MANUALSEARCH = 0;// 手动
    public static final int FULLSEARCH = 1; //全频
    public static final int AUTOSEARCH = 2;//自动

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.search_menu_layout, container ,false);
        setupViews();
        settingFragment = new SettingFragment(getActivity());
        setListener();
        mSearchAutoTextView.requestFocus();
        LenovoSettingsActivity.setTitleFocus(false);
        return mMainView;
    }

    private void setupViews() {
        mSearchAutoTextView = (TextView) mMainView.findViewById(R.id.auto_search_textview);
        mSearchFullTextView = (TextView) mMainView.findViewById(R.id.full_search_textview);
        mSearchManualTextView = (TextView) mMainView.findViewById(R.id.manual_search_textview);
        mSearchRessetTextView = (TextView) mMainView.findViewById(R.id.search_resset);
    }

    private void setListener() {
        mSearchAutoTextView.setOnClickListener(this);
        mSearchFullTextView.setOnClickListener(this);
        mSearchManualTextView.setOnClickListener(this);
        // mSearchDownloadTextView.setOnClickListener(this);
        mSearchRessetTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auto_search_textview:
                SearchMainFragment searchMainFragment = new SearchMainFragment();
                searchMainFragment.setSearchType(AUTOSEARCH);
                settingFragment.setFragment(searchMainFragment, true);
                break;
            case R.id.full_search_textview:
                searchMainFragment = new SearchMainFragment();
                searchMainFragment.setSearchType(FULLSEARCH);
                settingFragment.setFragment(searchMainFragment, true);
                break;
            case R.id.manual_search_textview:
                settingFragment.setFragment(new SearchManualFragment(), true);
                break;
            case R.id.search_resset:
                TransponderUtil.saveDefaultTransponer(getActivity());
                break;
        }
    }
}
