package com.lenovo.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lenovo.settings.Util.ThemeImage;

public class ThemeSettings extends Fragment implements OnItemClickListener {

	public static final String TAG = "ThemeSettings";
	
	public static String FILENAME = "theme.jpg";
    
    
	private View mView;
	private GridView mThemeGrid;
	private List<Map<String, Object>> mData;
	public LayoutInflater mInflater;
	private ListAdapter mGridViewAdapter;
	
	public ArrayList<ThemeImage> mArrayBitmap;
	private String filepath = "/mnt/sdcard/image";
	
	//动态加载图片
	private Thread mThread = null;
	private RelativeLayout mLoadingLayout;
	private AnimationDrawable mAnimationLoading;
	public static final int STATUS_OK = 0;
    public static final int STATUS_NOT_FOUND = 1;
    public static final int STATUS_READ_ERROR = 2;
    public static final int STATUS_EMPTY_FILE = 3;
    public static final int STATUS_SHOW_LOADING = 4;
    public static final int STATUS_SHOW_TEXT = 5;
	protected static final int MSG_HANDLER_EXIT = 1000;
	private boolean isExit = false;
	private boolean isShowText = false;
	
	
	private int mIndexTheme;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.theme_settings, container, false);
		mThemeGrid = (GridView) mView.findViewById(R.id.gridViewTheme);
		mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);
		//mThemeGrid.setVisibility(View.INVISIBLE);
        isExit = false;
		isShowText = false;
		
		
		
		mArrayBitmap = new ArrayList<ThemeImage>();
		readBitmap();
		for(int i = 0;i<mbitmaps.size();i++){
			ThemeImage themeImage = new ThemeImage();
			themeImage.setBitmap(mbitmaps.get(i));
			themeImage.setTitle("图片"+i);
			if(i == 0)
				themeImage.setIscheck(0);
			else
				themeImage.setIscheck(1);
			mArrayBitmap.add(themeImage);
		}
		
		
		mGridViewAdapter = new ListViewAdapter(getActivity(), mArrayBitmap);
		mThemeGrid.setAdapter(mGridViewAdapter);
		
		mThemeGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int postion,
					long arg3) {
				showThemeSetDialog(mbitmaps.get(postion));
			}
		});
		LenovoSettingsActivity.setTitleFocus(false);
		mThemeGrid.requestFocus();
		
//		mHandler.sendEmptyMessage(STATUS_SHOW_LOADING);
//		LoadingThread();
		
		return mView;
	}




	@Override
	public void onStart() {

		super.onStart();
	}




	@Override
	public void onStop() {

		super.onStop();
	}




	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		
	}


	
	ImageView mImageView;
	TextView mTextTitle;
	TextView mTextMsg;
	LinearLayout mLinearLayout;
	public class ListViewAdapter extends BaseAdapter {
	   	 
    	//private Context mContext;
		public ArrayList<ThemeImage> bitmap;
		private LayoutInflater mLayoutInflater;
		private int selectedPosition = -1;  
		
		public void setSelectedPosition(int position) {   
			selectedPosition = position;   
		}

		public ListViewAdapter(Context context, ArrayList<ThemeImage> bitmap1) { 
			bitmap = bitmap1; 
            mLayoutInflater = LayoutInflater.from(context); 
        } 

		@Override
		public int getCount() {
	
			return bitmap.size();
		}

		@Override
		public Object getItem(int position) {
	
			return bitmap.get(position);
		}

		@Override
		public long getItemId(int position) {
	
			return position;
		}
 
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	
			//ViewHolder holder = null;
			if (convertView == null) { 
				//holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.theme_list, null); 
				mImageView = (ImageView) convertView.findViewById(R.id.theme_imag);
				mTextTitle = (TextView) convertView.findViewById(R.id.theme_title);
				mTextMsg = (TextView) convertView.findViewById(R.id.theme_msg);
				mLinearLayout = (LinearLayout) convertView.findViewById(R.id.themeLayout);
				mTextMsg.setAlpha(0.3f);
				//convertView.setTag(holder);
			}
//			else{
//				holder = (ViewHolder) convertView.getTag();
//			}
			mImageView.setImageBitmap(bitmap.get(position).getBitmap());
			mTextTitle.setText(bitmap.get(position).getTitle());
			if(bitmap.get(position).getIscheck() == 0){
				mTextMsg.setVisibility(View.VISIBLE);
				mTextMsg.setText(R.string.default_theme);
			}else{
				mTextMsg.setVisibility(View.INVISIBLE);
			}
			
			return convertView; 
		}

		public class ViewHolder { 
		    public TextView textView;
		    public TextView textViewMsg;
		    public ImageView imageView;
		    public LinearLayout layout;
		} 
    }
	
	File targetFile;
	ArrayList<Bitmap> mbitmaps;
	
	
	public  void readBitmap(){
		mbitmaps = new ArrayList<Bitmap>();
		targetFile = new File(filepath);
		File[] photos = targetFile.listFiles();
		if(photos!=null && photos.length>0){
			for(int i = 0;i<photos.length;i++){
				String photoPath = photos[i].getPath();
				//Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
				
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
				opt.inPurgeable = true;
				opt.inInputShareable = true;
				
				Bitmap bitmap = BitmapFactory.decodeFile(photoPath, opt);
				
				if(bitmap.isRecycled()){
					bitmap.recycle();
					System.gc();
				}
				
				mbitmaps.add(bitmap);
			}
		}
    }
	
	
	
	
	private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

			if(isExit){
				Log.e(TAG,"mHandler exit");
				return;
			}
            if (msg.what == STATUS_OK) {
                String text = (String) msg.obj;
                hideLoadingView();
                mThemeGrid.setVisibility(View.VISIBLE);
                mThemeGrid.requestFocus();
            }else if (msg.what == STATUS_SHOW_LOADING){
        		showLoadingView(getActivity().getString(
        				R.string.loading_theme),true);
            }else if (msg.what == STATUS_SHOW_TEXT){

            	for(int i = 0;i<10;i++){
        			ThemeImage themeImage = new ThemeImage();
        			themeImage.setBitmap(bitmap);
        			themeImage.setTitle("图片"+1);
        			if(i == 0)
        				themeImage.setIscheck(0);
        			else
        				themeImage.setIscheck(1);
        			mArrayBitmap.add(themeImage);
        		}
        		
        		
        		mGridViewAdapter = new ListViewAdapter(getActivity(), mArrayBitmap);
        		mThemeGrid.setAdapter(mGridViewAdapter);
            	
            	isShowText = false;
            }
        }
    };
	
    Bitmap bitmap;
	void LoadingThread(){
		mThread = new Thread(){

			@Override
			public void run() {
				int status = STATUS_OK;
				
				try {
					 readBitmap();
	                if((bitmap !=null)){
	                	isShowText = true;
	                	Message msg = mHandler.obtainMessage(STATUS_SHOW_TEXT, null);
	                	mHandler.sendMessage(msg);
	                }
				} catch (Exception e) {
					
					status = STATUS_EMPTY_FILE;
				} 
	            if ((status == STATUS_OK)) {
	                Log.e(TAG, "License is empty ");
	                status = STATUS_EMPTY_FILE;
	            }

	            // Tell the UI thread that we are finished.
	            Message msg = mHandler.obtainMessage(status, null);
	            if (status == STATUS_OK) {
	                //msg.obj = data.toString();
	            	//msg.obj = data_str;
	            }
	            mHandler.sendMessage(msg);
			}
			
		};
		mThread.start();
	}
	void hideLoadingView(){
		if(mAnimationLoading.isRunning()){
			mAnimationLoading.stop();
		}
		mLoadingLayout.setVisibility(View.INVISIBLE);
	}
	void showLoadingView(String msg, boolean hasLoading){	
		TextView textView = (TextView)mLoadingLayout.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mLoadingLayout.findViewById(R.id.toastImage);
		textView.setText(msg);
		if(hasLoading){
			img.setVisibility(View.VISIBLE);
			mAnimationLoading = (AnimationDrawable)img.getBackground();
			if(!mAnimationLoading.isRunning()){
				mAnimationLoading.start();
			}
		}else{
			img.setVisibility(View.INVISIBLE);
		}
		mLoadingLayout.setVisibility(View.VISIBLE);
	}

	
	//显示dialog
	private void showThemeSetDialog(final Bitmap bitmap){
		final Dialog dialog;
		TextView msg;
		final Button btnConfirm;
		Button btnCancel;
		dialog = new Dialog(mView.getContext(),R.style.DialogStyle);
		dialog.setContentView(R.layout.resolution_dialog);
		msg = (TextView) dialog.findViewById(R.id.textview_dialog);
        btnConfirm = (Button) dialog.findViewById(R.id.btn_dlg_confirm);        
        btnCancel = (Button) dialog.findViewById(R.id.btn_dlg_cancel);
        msg.setText(R.string.set_theme_msg);
    	btnConfirm.setText(R.string.yes);
        btnCancel.setText(R.string.no);
        btnConfirm.requestFocus();
        btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//write(bitmap);
				try {
					saveMyBitmap(bitmap);
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				dialog.dismiss();
				read();
			}
        	
        });
        btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				dialog.dismiss();
				//getActivity().getFragmentManager().popBackStack();
			}
        	
        });
        dialog.show();
	}
	
	
	public void saveMyBitmap(Bitmap mbitmap) throws IOException {
        File f = new File("/sdcard/image/"  + "theme.jpg");
        f.createNewFile();
        FileOutputStream fOut = null;
        try {
                fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        }
        mbitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
                fOut.flush();
        } catch (IOException e) {
                e.printStackTrace();
        }
        try {
                fOut.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
	}
	// 写文件
	private void write(Bitmap content){
		try {
			File file = new File(FILENAME);
			if(!file.exists())
				file.mkdir();
			FileOutputStream fos =new FileOutputStream(file, false);
			fos.write(content.mBuffer);
			fos.close();
		} catch (Exception e) {
	}
	}
	
	private void read() {
		try {
			FileInputStream inputStream = getActivity().openFileInput(FILENAME);
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);
			System.out.println(inputStream.equals(null));
		} catch (Exception e) {
		}
	}
}
