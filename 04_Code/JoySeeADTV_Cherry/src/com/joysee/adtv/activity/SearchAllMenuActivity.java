package com.joysee.adtv.activity;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.TransponderUtil;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.ui.SearchEditText;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SearchAllMenuActivity extends Activity implements OnClickListener {
	private SearchEditText mSearchEditText;
	private Transponder mTransponder;
	private Button mStartSerchButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_all_menu_layout);
		init();
	}
	public void init(){
		mSearchEditText = (SearchEditText) findViewById(R.id.search_advanced_option_symbolrate);
		mStartSerchButton = (Button) findViewById(R.id.search_advanced_option_save_button);
		mStartSerchButton.setOnClickListener(this);
		mTransponder = TransponderUtil.getTransponderFromXml(
                this,
                DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL);
		mSearchEditText.setText(mTransponder.getSymbolRate()+"");
	}
	@Override
	public void onClick(View v) {
		String symbolrate = mSearchEditText.getText().toString();
		Log.d("songwenxuan","symbolrate" + symbolrate);
		mTransponder.setFrequency(DefaultParameter.DefaultTpValue.FREQUENCY);
		mTransponder.setModulation(DefaultParameter.DefaultTpValue.MODULATION);
		mTransponder.setSymbolRate(Integer.parseInt(symbolrate));
		TransponderUtil.saveTransponerToXml(
                this,
                DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL,
                mTransponder);
		Intent intent = new Intent();
		intent.setClass(this, SearchMainActivity.class);
		intent.putExtra(SearchMainActivity.SEARCH_TYPE,SearchMainActivity.ALL_SEARCH);
		startActivity(intent);
	}
}
