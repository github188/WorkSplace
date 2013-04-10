package com.joysee.adtv.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.EmailViewHolder;
import com.joysee.adtv.logic.CaManager;
import com.joysee.adtv.logic.bean.EmailContent;
import com.joysee.adtv.logic.bean.EmailHead;
import com.joysee.adtv.ui.adapter.EmailAdapter;

public class EmailActivity extends Activity {
    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.activity.EmailActivity",DvbLog.DebugType.D);
    private ListView mEmailListView;
    /**删除所有邮件按钮*/
    private Button mDeleteAllButton;
    /** 下翻页按钮 */
    private Button mPageDownButton;
    /** 下翻页按钮 */
    private Button mPageUpButton;
    /** 邮件弹出对话框，有两种风格 */
    private PopupWindow mEmailDialog;
    /** 邮件空间提示 */
    private TextView mEmailSpaceTv;
    private TextView mEmailNoSpaceTv;
    /**  查看邮件内容弹出框 */
    public static final int DIALOG_TYPE_READ = 0;
    /** 删除邮件确认弹出框 */
    public static final int DIALOG_TYPE_ASK = 1;
    /** 加载数据进度条 */
    private ProgressBar mEmailPgb;
    //对话框控件
    private TextView tpDialogTitleTv;
    private TextView tPDialogTimeTv;
    private TextView tpDialogContentTv;
    private Button tpDialogLeftBt;
    private Button tpDialogRightBt;
    private EmailAdapter mEmailAdapter;
    private LayoutInflater mLayoutInflater;
    private View dialog = null;
    /**邮件数量*/
    private int mEmailCount = 0;
    /**邮件剩余空间*/
    private int mEmailSpace = 0;

    private CaManager mCaManager;
    /**邮件头列表*/
    private ArrayList<EmailHead> mEmailHeadList;
    /**邮件ItemView对象List*/
    private List<EmailViewHolder> mEmailViewHolderList;
    private static final class HandlerMsg{
        /** 获取邮件空间和初始化ListView数据 */
        public static final int MSG_INITDATA = 0;
        /** 获取邮件内容 */
        public static final int MSG_GET_EMAILCONTENT = 1;
        /** 邮件内容 */
        public static final int MSG_DELETE_ONE_EMAIL = 2;
        /** 获取邮件内容 */
        public static final int MSG_DELETE_ALL_EMAIL = 3;
        /** DismissDialog */
        public static final int MSG_DISMISS_DIALOG = 4;
        /**Set as read*/
        public static final int MSG_SET_AS_READ = 5;
    }
    private Dialog mDialog ;
    private int mDialogType = 0;
    /** UI线程 handler */
    private Handler mMainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HandlerMsg.MSG_INITDATA:
                mEmailCount = mCaManager.nativeGetEmailUsedSpace();
                mEmailSpace = mCaManager.nativeGetEmailIdleSpace();
                mEmailHeadList = new ArrayList<EmailHead>();
                int getHeadBack = mCaManager
                        .nativeGetEmailHeads(mEmailHeadList);
                if (getHeadBack >= 0) {
                    log.D("----get Email space  used = " + mEmailCount
                            + " remain = " + mEmailSpace
                            + " get Email header back = " + getHeadBack );
                } else {
                    log.E("----get Email header Error "
                            + " get Email header back = " + getHeadBack);
                }
                log.D(" mEmailHeadList size = " + mEmailHeadList.size());
                initListViewData();
                showEmailCount(mEmailCount,mEmailSpace);
                break;
            case HandlerMsg.MSG_GET_EMAILCONTENT:
                EmailContent emailContent = new EmailContent();
//                int id = mEmailIdArray[msg.arg1];// 得到当前Item邮件ID
                int id = mEmailHeadList.get(msg.arg1).getEmailID();
                int success = mCaManager.nativeGetEmailContent(id,emailContent);
                if (success >= 0) {
                    if (null != emailContent) {
                        // 显示读取邮件对话框
                        showEmailDialog(
                        		DIALOG_TYPE_READ,
                                mEmailHeadList.get(msg.arg1).getEmailTitle().trim(),
                                DateFormatUtil.getDateFromMillis(new Date( mEmailHeadList.get(msg.arg1).getEmailSendTime())),
                                emailContent.getEmailContent().trim(),
                                msg.arg1);
                    } /*else {
                        // 测试程序
                        showEmailDialog(
                                DIALOG_TYPE_READ,
                                getResources().getString(R.string.email_dialog_title_str),
                                getResources().getString(R.string.email_dialog_time_str),
                                getResources().getString(R.string.email_dialog_content_str,
                                "afewfha;owefjawiefjawe;fj;j;jifasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf"),
                                msg.arg1);
                    }*/
                } else {
                    log.E("--- get email content error ,email id = " + id
                            + " get success = " + success);
                }
                break;
            case HandlerMsg.MSG_DELETE_ONE_EMAIL:
                deleteOneEmail(msg.arg1);
                mMainHandler.sendEmptyMessage(HandlerMsg.MSG_DISMISS_DIALOG);
                mEmailCount--;
                mEmailSpace++;
                showEmailCount(mEmailCount, mEmailSpace);
                break;
            case HandlerMsg.MSG_DELETE_ALL_EMAIL:
                deleteAllEmail();
                mEmailCount = 0;
                mEmailSpace = 100;
                showEmailCount(mEmailCount, mEmailSpace);
                break;
            case HandlerMsg.MSG_DISMISS_DIALOG:
                if (mEmailDialog != null) {
                    mEmailDialog.dismiss();
                }
                break;
            case HandlerMsg.MSG_SET_AS_READ:
                try {
                    log.D(" mEmailAdapter.getCount() = "
                            + mEmailAdapter.getCount());
                    if (mEmailAdapter.getCount() > 0
                            && mEmailAdapter.getCount() > msg.arg1) {
                        mEmailViewHolderList
                                .get(msg.arg1)
                                .getmIcon()
                                .setImageResource(
                                        R.drawable.email_icon_readed);
                        mEmailAdapter.notifyDataSetChanged();
                        log.D(" mEmailAdapter.getCount() = "
                                + mEmailAdapter.getCount() + " id = " + msg.arg1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mMainHandler.sendEmptyMessage(HandlerMsg.MSG_DISMISS_DIALOG);
                break;
            default:
                break;
            }
            super.handleMessage(msg);
        }
    };

    /**ListView Item onClickListener*/
    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            log.D(" arg2 = " + arg2 + " arg3 = " + arg3);
            Message msg = new Message();
            msg.what = HandlerMsg.MSG_GET_EMAILCONTENT;
            msg.arg1 = arg2;
            mMainHandler.sendMessage(msg);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_main);
        
        RelativeLayout email_main_bg = (RelativeLayout) findViewById(R.id.email_main_bg);
        
        if(getThemePaper() != null){
            email_main_bg.setBackgroundDrawable(getThemePaper());
        }
        
        initView();
        mCaManager = CaManager.getCaManager();
    }

    /**
     * 初始化页面控件
     */
    public void initView(){
        log.D("----------initview  begin ");
        mEmailListView = (ListView) findViewById(R.id.email_listview);
        mEmailSpaceTv = (TextView) findViewById(R.id.email_space_info_tv);
        mEmailNoSpaceTv = (TextView) findViewById(R.id.email_nospace_info_tv);
        mPageDownButton =  (Button) findViewById(R.id.email_page_down_button);
        mPageUpButton =  (Button) findViewById(R.id.email_page_up_button);
        mDeleteAllButton = (Button) findViewById(R.id.email_deleteall_button);
        mEmailPgb = (ProgressBar) findViewById(R.id.email_progressbar);
        //显示进度条
        mEmailPgb.setVisibility(View.VISIBLE);
        mDeleteAllButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    showDeleteDialog();
            }
        });
        mPageDownButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mPageUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        log.D("----------initview  end ");
    }
    /**
     * 初始化ListView数据
     */
    public void initListViewData(){
        log.D("----------initListViewData  begin ");
        mEmailAdapter = new EmailAdapter(this, getEmailListData());
        mEmailListView.setAdapter(mEmailAdapter);
        mEmailListView.setFocusable(true);
        mEmailListView.requestFocus();
        mEmailListView.setOnItemClickListener(mOnItemClickListener);
        //隐藏进度条
        mEmailPgb.setVisibility(View.INVISIBLE);
        log.D("----------initListViewData  end ");
    }
    /**
     * 得到邮件ListView数据
     * @return List(EmailViewHolder)
     */
    public List<EmailViewHolder> getEmailListData() {
        mEmailViewHolderList = new ArrayList<EmailViewHolder>();
        log.D(" ----mEmailHeadList size = "+mEmailHeadList.size());
        if (null != mEmailHeadList) {
            for (EmailHead emailHead : mEmailHeadList) {
                if (null != emailHead) {
                    EmailViewHolder viewHolder = new EmailViewHolder(this);
                    log.D("------getEmailListData isNewEmail = "
                            + emailHead.isNewEmail() + " title = "
                            + emailHead.getEmailTitle().trim());
                    log.D(" send time = " + emailHead.getEmailSendTime()
                            + " email level = " + emailHead.getEmailLevel()
                            + " email id = " + emailHead.getEmailID());
                    // 邮件读取标志
                    if (emailHead.isNewEmail()) {
                        viewHolder.getmIcon().setImageResource(
                                R.drawable.email_icon_no_read);
                    } else {
                        viewHolder.getmIcon().setImageResource(
                                R.drawable.email_icon_readed);
                    }
                    // 邮件标题
                    viewHolder.getmTitle().setText(""+emailHead.getEmailTitle().trim());
                    // 邮件发送时间
                    viewHolder.getmTime().setText(""+
                    		DateFormatUtil.getDateFromMillis(new Date(emailHead.getEmailSendTime())));
                    // 邮件类型
                    if (emailHead.getEmailLevel() == 1) {
                        // 重要邮件
                        viewHolder.getmType().setText(
                                getResources().getString(R.string.email_type_important));
                    } else {
                        // 普通邮件
                        viewHolder.getmType().setText(
                                getResources().getString(R.string.email_type_common));
                    }
//                    mEmailIdArray[count++] = emailHead.getmEmailID();
                    mEmailViewHolderList.add(viewHolder);
                }
            }
        }else{
            //测试数据
            for (int i = 0; i < 10; i++) {
                EmailViewHolder viewHolder = new EmailViewHolder(this);
                if (i % 2 == 0) {
                    viewHolder.getmIcon().setImageResource(R.drawable.email_icon_readed);
                } else {
                    viewHolder.getmIcon().setImageResource( R.drawable.email_icon_no_read);
                }
                viewHolder.getmTime().setText(new Date().toLocaleString());
                viewHolder.getmTitle().setText("Title" + i);
                viewHolder.getmType().setText("Type" + i);
                mEmailViewHolderList.add(viewHolder);
            }
        }
        log.D(" mEmailViewHolderList size = " + mEmailViewHolderList.size());
        return mEmailViewHolderList;
    }

    /**
     * 邮件弹出框，有两种类型
     * @param type 弹出框类型
     * @param title 邮件标题
     * @param time 邮件时间
     * @param content 邮件内容
     * @param id 选中item ID
     */
    public void showEmailDialog(final int type, String title, String time,
            String content, final int id) {
        log.D("showEmailDialog type = " + type + " title = " + title
                + " time = " + time + " content = " + content + " id = " + id);
        if (mDialogType != type) {
            mEmailDialog = null;
            dialog = null;
        }
        mDialogType = type;
        if (dialog == null) {
            mLayoutInflater = this.getLayoutInflater();
            dialog = mLayoutInflater.inflate(R.layout.email_dialog, null);
            // 得到控件
            tpDialogTitleTv = (TextView) dialog.findViewById(R.id.email_dialog_title_tv);
            tPDialogTimeTv = (TextView) dialog.findViewById(R.id.email_dialog_time_tv);
            tpDialogContentTv = (TextView) dialog.findViewById(R.id.email_dialog_content_tv);
            tpDialogLeftBt = (Button) dialog.findViewById(R.id.email_dialog_back_bt);
            tpDialogRightBt = (Button) dialog.findViewById(R.id.email_dialog_delete_bt);
        }
        if (mEmailDialog == null) {
            mEmailDialog = new PopupWindow(dialog);
        }
        switch (type) {
        case DIALOG_TYPE_READ:
            // 注册按钮监听
            tpDialogLeftBt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Message msgSetAsRead = new Message();
                    msgSetAsRead.what = HandlerMsg.MSG_SET_AS_READ;
                    msgSetAsRead.arg1 = id;
                    mMainHandler.removeMessages(HandlerMsg.MSG_SET_AS_READ);
                    mMainHandler.sendMessageDelayed(msgSetAsRead,500);
                }
            });
            tpDialogRightBt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Message msgDeleteOne = new Message();
                    msgDeleteOne.what = HandlerMsg.MSG_DELETE_ONE_EMAIL;
                    msgDeleteOne.arg1 = id;
                    mMainHandler.removeMessages(HandlerMsg.MSG_DELETE_ONE_EMAIL);
                    mMainHandler.sendMessageDelayed(msgDeleteOne, 500);
                }
            });
            // 设置页面显示内容
            tpDialogTitleTv.setText(title);
            tPDialogTimeTv.setText(time);
            tpDialogContentTv.setText(content);

            mEmailDialog.setWidth((int)getResources().getDimension(R.dimen.email_dialog_read_width));
            mEmailDialog.setHeight((int)getResources().getDimension(R.dimen.email_dialog_read_height));
            break;
        case DIALOG_TYPE_ASK:
            tpDialogLeftBt.setText(getResources().getText(
                    R.string.email_dialog_positive_str));
            tpDialogRightBt.setText(getResources().getText(
                    R.string.email_dialog_negative_str));
            // 注册按钮监听
            tpDialogLeftBt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msgDeleteOne = new Message();
                    msgDeleteOne.what = HandlerMsg.MSG_DELETE_ONE_EMAIL;
                    msgDeleteOne.arg1 = id;
                    mMainHandler.sendMessage(msgDeleteOne);
                }
            });
            tpDialogRightBt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainHandler
                            .removeMessages(HandlerMsg.MSG_DISMISS_DIALOG);
                    mMainHandler.sendEmptyMessageDelayed(
                            HandlerMsg.MSG_DISMISS_DIALOG, 500);
                }
            });

            tpDialogTitleTv.setText(title);
            tPDialogTimeTv.setVisibility(View.GONE);
            tpDialogContentTv.setVisibility(View.GONE);

            mEmailDialog.setWidth((int)getResources().getDimension(R.dimen.email_dialog_ask_width));
            mEmailDialog.setHeight((int)getResources().getDimension(R.dimen.email_dialog_ask_height));
            break;
        }
        mEmailDialog.setFocusable(true);
        mEmailDialog.showAtLocation(this.getWindow().getDecorView(),
                Gravity.CENTER, 0, 0);
    }
    /**
     * 删除一封邮件
     * @param id
     */
    public void deleteOneEmail(int id) {
        log.D(" deleteOneEmail id = " + id);
        log.D(" deleteOneEmail mEmailAdapter size = " + mEmailAdapter.getCount());
        log.D(" deleteOneEmail mEmailViewHolderList size = " + mEmailViewHolderList.size());
        log.D(" deleteOneEmail mEmailHeadList size = " + mEmailHeadList.size());
        int success = mCaManager.nativeDelEmail(mEmailHeadList.get(id).getEmailID());
      //删除一封邮件
        if (success >= 0) {
            mEmailViewHolderList.remove(id);
            mEmailHeadList.remove(id);
//            mEmailAdapter.deleteOne(id);
            mEmailAdapter.notifyDataSetChanged();
        } else {
            log.E(" ------ delete email false !!!!!!!!");
        }
        log.D(" ------ delete email success = " + success);
    }
    /**
     * 删除所有邮件
     * @param id
     */
    public void deleteAllEmail(){
        log.D(" ------ delete all email begin");
        for (EmailHead emailHead : mEmailHeadList) {
            int success = mCaManager.nativeDelEmail(emailHead.getEmailID());
            log.D(" ------ delete email success = " + success + " email id = "
                    + emailHead.getEmailID());
        }
        //删除所有邮件
        mEmailViewHolderList.removeAll(mEmailViewHolderList);
        mEmailHeadList.removeAll(mEmailHeadList);
        mEmailAdapter.notifyDataSetChanged();
        log.D(" ------ delete all email end");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        mMainHandler.sendEmptyMessage(HandlerMsg.MSG_INITDATA);
        super.onResume();
    }
    /**
     * 显示邮件空间
     * @param used 邮件数量
     * @param remain 剩余邮件空间
     */
    public void showEmailCount(int used , int remain){
        mEmailSpaceTv.setText(getResources().getString(
                R.string.email_space_info_str, "" + used,"" + remain));
        if (used == 100) {
            mEmailNoSpaceTv.setVisibility(View.VISIBLE);
        }else{
            mEmailNoSpaceTv.setVisibility(View.INVISIBLE);
        }
        if (mEmailViewHolderList.size() > 0) {
            mDeleteAllButton.setEnabled(true);
            mDeleteAllButton.setFocusable(true);
            mDeleteAllButton.setFocusableInTouchMode(true);
            mDeleteAllButton.clearFocus();
        }else{
            mDeleteAllButton.setEnabled(false);
            mDeleteAllButton.setFocusable(false);
            mDeleteAllButton.setFocusableInTouchMode(false);
            mDeleteAllButton.clearFocus();
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        log.D(" dispatchKeyEvent event = " + event.getKeyCode());
        return super.dispatchKeyEvent(event);
    }

    /**
     * 取setting背景图，用于设置成这个activity的背景
     * @return
     */
    public Drawable getThemePaper(){
        String url = Settings.System.getString(this.getContentResolver(), "settings.theme.url");
        if(url!=null && url.length()>0){
                Bitmap bitmap = BitmapFactory.decodeFile(url);
                Drawable drawable = new BitmapDrawable(bitmap);
                return drawable;
        }
        return null;
    }
    public void showDeleteDialog(){
        if (mDialog == null) {
            mDialog = new Dialog(this, R.style.notify_dialog_bg);
            View vi = LayoutInflater.from(EmailActivity.this).inflate(
                    R.layout.email_delete_ask_dialog, null);
            Button ok = (Button) vi.findViewById(R.id.config_dialog_btn_confirm);
            Button cancel = (Button) vi.findViewById(R.id.config_dialog_btn_cancel);
            mDialog.setContentView(vi);
            mDialog.show();
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainHandler
                            .sendEmptyMessage(HandlerMsg.MSG_DELETE_ALL_EMAIL);
                    mDialog.dismiss();
                }
            });
        }else{
            mDialog.show();
        }
    }
}