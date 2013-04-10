package com.joysee.adtv.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.doc.ADTVEpgDoc;
import com.joysee.adtv.doc.ADTVResource;

public class TestActivity extends BasicActivity{
    
    public static final int RES_TIME=1;
    public static final int RES_BITMEP=2;
    
    private ImageView image;
    private TextView text;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        image=(ImageView)findViewById(R.id.img);
        text=(TextView)findViewById(R.id.tex);
        doc = new ADTVEpgDoc();
        setDoc(doc);
    }
    
    protected void onDocGotResource(ADTVResource res){
        switch(res.getType()){
            case RES_TIME:
                text.setText(res.getString());
                break;
            case RES_BITMEP:
                image.setImageBitmap(res.getBitmap());
                break;
        }
    }

}
