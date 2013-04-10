
package novel.supertv.dvb.activity;

import novel.supertv.dvb.R;
import novel.supertv.dvb.utils.TransponderUtil;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 搜索菜单主界面
 */
public class SearchMenuActivity extends Activity implements OnClickListener {
    private TextView mSearchAutoTextView;
    private TextView mSearchFullTextView;
    private TextView mSearchManualTextView;
    private TextView mSearchDownloadTextView;
    private TextView mSearchRessetTextView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_search_layout);
        findView();
        setListener();
    }

    private void setListener() {
        mSearchAutoTextView.setOnClickListener(this);
        mSearchFullTextView.setOnClickListener(this);
        mSearchManualTextView.setOnClickListener(this);
        mSearchDownloadTextView.setOnClickListener(this);
        mSearchRessetTextView.setOnClickListener(this);
    }

    private void findView() {
        mSearchAutoTextView = (TextView) findViewById(R.id.auto_search_textview);
        mSearchFullTextView = (TextView) findViewById(R.id.full_search_textview);
        mSearchManualTextView = (TextView) findViewById(R.id.manual_search_textview);
        mSearchDownloadTextView = (TextView) findViewById(R.id.download_search_textview);
        mSearchRessetTextView = (TextView) findViewById(R.id.search_resset);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auto_search_textview:
                Intent autoSearchIntent = new Intent(this,
                        SearchMainActivity.class);
                startActivity(autoSearchIntent);
                break;
            case R.id.full_search_textview:
                Intent fullSearchIntent = new Intent(this,
                        SearchMainActivity.class);
                fullSearchIntent.putExtra(SearchMainActivity.SEARCHTYPE,
                        SearchMainActivity.FULLSEARCH);
                startActivity(fullSearchIntent);
                break;
            case R.id.manual_search_textview:
                Intent manualSearchIntent = new Intent(this,
                        SearchHandActivity.class);
                startActivity(manualSearchIntent);
                break;
            case R.id.download_search_textview:
                Intent downloadSearchIntent = new Intent(this,
                        SearchNetActivity.class);
                startActivity(downloadSearchIntent);
                break;
            case R.id.search_resset://恢复默认值
                TransponderUtil.saveDefaultTransponer(this);
                break;
        }
    }
}
