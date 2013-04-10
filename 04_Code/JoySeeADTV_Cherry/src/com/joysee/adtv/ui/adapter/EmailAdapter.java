package com.joysee.adtv.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.EmailViewHolder;

public class EmailAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    /**
     * ListView 中所有Item，通过操作它可以动态的改变Listview数据
     */
    private List<EmailViewHolder> mEmailViewList;
    private Context mContext;

    public EmailAdapter(Context context, List<EmailViewHolder> view) {
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mEmailViewList = view;
        mContext = context;
    }
    /**
     * 得到ListView Item数量
     */
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mEmailViewList.size();
    }
    /**
     * 得到某一个Item对象(EmailViewHolder)
     */
    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mEmailViewList.get(position);
    }
    /**
     * 得到Item的位置
     */
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EmailViewHolder ViewHolder;
        // TODO Auto-generated method stub
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.email_listview_item,
                    null);
            ViewHolder = new EmailViewHolder(mContext);
            ViewHolder.setmIcon((ImageView) convertView
                    .findViewById(R.id.email_listview_item_emailicon));
            ViewHolder.setmTime((TextView) convertView
                    .findViewById(R.id.email_listview_item_emailtime));
            ViewHolder.setmTitle((TextView) convertView
                    .findViewById(R.id.email_listview_item_emailtitle));
            ViewHolder.setmType((TextView) convertView
                    .findViewById(R.id.email_listview_item_emailtype));
            convertView.setTag(ViewHolder);
        }else{
            ViewHolder = (EmailViewHolder) convertView.getTag();
        }
        EmailViewHolder emailView = mEmailViewList.get(position);
        if (null != emailView) {
            ViewHolder.getmIcon().setImageDrawable(
                    emailView.getmIcon().getDrawable());
            ViewHolder.getmTitle().setText(emailView.getmTitle().getText());
            ViewHolder.getmTime().setText(emailView.getmTime().getText());
            ViewHolder.getmType().setText(emailView.getmType().getText());
        }
        if(position%2 == 0)
        	convertView.setBackgroundDrawable(null);
        else
        	convertView.setBackgroundResource(R.color.search_channel_list_color);
        return convertView;
    }
    /**
     * 删除一封邮件
     * @param id 邮件ID，用Item ID标示。
     * @return 操作成功返回 true，否则返回失败。
     */
    public boolean deleteOne(int id){
        if (id < 0 || id >= mEmailViewList.size()) {
            return false;
        }
        mEmailViewList.remove(id);
        this.notifyDataSetChanged();
        return true;
    }
    /**
     * 删除所有邮件
     * @return 操作成功返回 true，否则返回失败。
     */
    public boolean deleteAll(){
        boolean flag = mEmailViewList.removeAll(mEmailViewList);
        this.notifyDataSetChanged();
        return flag;
    }
    /**
     * 设置一封邮件状态为已读
     * @param id 邮件ID，用Item ID标示。
     * @return 操作成功返回 true，否则返回失败。
     */
    public boolean setEmailAsReaded(int id){
        if (id < 0 || id >= mEmailViewList.size()) {
            return false;
        }
        mEmailViewList.get(id).getmIcon().setImageResource(R.drawable.email_icon_readed);
        this.notifyDataSetChanged();
        return true;
    }
}
