
package com.lenovo.settings;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class DropdownMenu {
    
    public static final int DROPDOWN_MENU_ITEM_CHICK = 1;
    public static final int DROPDOWN_MENU_GET_ADAPTER = 2;
    public static final int DROPDOWN_MENU_CHECK_CITY = 3;
    public static final int DROPDOWN_MENU_BUTTON_CAN_EDIT = 1;
    public static final int DROPDOWN_MENU_BUTTON_NOT_EDIT = 0;
    public static final String TAG = "DropdownMenu";
    
    private ListView mList;
    private Object mObject;
    private RelativeLayout mListLayout;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mArray = new ArrayList<String>();
    private ArrayList<String> mStrings = new ArrayList<String>();
    private Handler mHandler;
    protected boolean mEnableList = false;
    protected boolean mListEnd = false;
    private int mEditEnable = DROPDOWN_MENU_BUTTON_NOT_EDIT;
    private String mEditDefault = null;
    private Context mContext;
    private View mView;
    
    public DropdownMenu(Context context, View view, Handler handler) {
        mContext = context;
        mView = view;
        mListLayout = (RelativeLayout) view.findViewById(R.id.listLayout);
        mList = (ListView) view.findViewById(R.id.dropdownlist);
        mAdapter = new ArrayAdapter<String>(context, R.layout.dropdown_item, mArray);
        mHandler = handler;
    }
    
    public DropdownMenu(Context context, RelativeLayout list_layout, ListView list,
            Handler handler, int item_layout) {
        mContext = context;
        mListLayout = list_layout;
        mList = list;
        mAdapter = new ArrayAdapter<String>(context, item_layout, mArray);
        mHandler = handler;
    }
    
    public void setButtonListener(final TextView text, String default_str) {
        text.setText(default_str);
        text.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, "  begin  setButtonListener onKey   key code = " + keyCode);
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if ((keyCode == KeyEvent.KEYCODE_ENTER)
                        || (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
                    mEnableList = true;
                    mObject = text;
                    mEditEnable = DROPDOWN_MENU_BUTTON_NOT_EDIT;
                    Message msg = new Message();
                    msg.what = DROPDOWN_MENU_GET_ADAPTER;
                    msg.arg1 = mEditEnable;
                    msg.obj = mObject;
                    mHandler.sendMessage(msg);
                    showDropdownListEnable(true);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.d(TAG, "key code back!");
                    if (mEnableList) {
                        mEnableList = false;
                        mListEnd = false;
                        Log.e(TAG, "mListEnd = false");
                        showDropdownListEnable(false);
                        return true;
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (mEnableList) {
                        return true;
                    }
                } else if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                        || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                    if (mEnableList) {
                        return true;
                    }
                }
                return false;
            }
            
        });
        text.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if (mEnableList && (mObject == text)) {
                    mEnableList = false;
                    mListEnd = false;
                    Log.e(TAG, "mListEnd = false");
                    showDropdownListEnable(false);
                } else {
                    mEnableList = true;
                    mObject = text;
                    mListEnd = false;
                    mEditEnable = DROPDOWN_MENU_BUTTON_NOT_EDIT;
                    Message msg = new Message();
                    msg.what = DROPDOWN_MENU_GET_ADAPTER;
                    msg.arg1 = mEditEnable;
                    msg.obj = mObject;
                    mHandler.sendMessage(msg);
                }
            }
            
        });
    }
    
    public void setButtonListener(final EditText edit, String default_str) {
        mEditDefault = default_str;
        edit.setText(default_str);
        edit.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(TAG, "  begin  setButtonListener onKey   key code = " + keyCode);
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if ((keyCode == KeyEvent.KEYCODE_ENTER)
                        || (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
                    mEnableList = true;
                    mObject = edit;
                    mEditEnable = DROPDOWN_MENU_BUTTON_CAN_EDIT;
                    edit.setCursorVisible(true);
                    int position = edit.getText().length();
                    Selection.setSelection(edit.getText(), position);
                    Message msg = new Message();
                    msg.what = DROPDOWN_MENU_GET_ADAPTER;
                    msg.arg1 = mEditEnable;
                    msg.obj = mObject;
                    mHandler.sendMessage(msg);
                    showDropdownListEnable(true);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mEnableList) {
                        mEnableList = false;
                        showDropdownListEnable(false);
                        String str = (String) edit.getText().toString();
                        if (checkString(str)) {
                            edit.setText(str);
                        } else {
                            edit.setText(mEditDefault);
                        }
                        edit.setCursorVisible(false);
                        mListEnd = false;
                        Log.e(TAG, "mListEnd = " + mListEnd);
                        mListLayout.setVisibility(View.GONE);
                        return true;
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (mEnableList) {
                        return true;
                    }
                }
                return false;
            }
        });
        
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                String prefix = edit.getText().toString();
                getDropdownListArray(prefix, mStrings);
                mListLayout.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
            }
            
        });
        
    }
    
    public void setListViewPosition(int top, int size) {
        // Log.e(TAG,"top = "+top+" size = "+size);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mListLayout
                .getLayoutParams();
        params.height = ((size * 60) + 12);
        params.topMargin = top;
        mListLayout.setLayoutParams(params);
    }
    
    public void setListViewPosition(int top, int item_height, int size) {
        // Log.e(TAG,"top = "+top+" size = "+size);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mListLayout
                .getLayoutParams();
        params.height = ((size * item_height) + 12);
        params.topMargin = top;
        mListLayout.setLayoutParams(params);
    }
    
    public void setListViewPosition(View view, int size) {
        
        // Log.e(TAG,"id = "+view.getId()+" size = "+size);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mListLayout
                .getLayoutParams();
        params.height = ((size * 60) + 12);
        params.addRule(RelativeLayout.BELOW, view.getId());
        mListLayout.setLayoutParams(params);
    }
    
    public void setListViewAdapter(String[] strings, int index) {
        setArrayListStrings(strings);
        // if(mEnableList && (mEditEnable == DROPDOWN_MENU_BUTTON_CAN_EDIT))
        // return;
        mArray.clear();
        for (String str : strings) {
            mArray.add(str);
        }
        mList.setSelection(index);
        mAdapter.notifyDataSetChanged();
        if (index == (strings.length - 1)) {
            mListEnd = true;
        } else {
            mListEnd = false;
        }
    }
    
    public void setListViewAdapter(ArrayList<String> strings, int index) {
        setArrayListStrings(strings);
        // if(mEnableList && (mEditEnable == DROPDOWN_MENU_BUTTON_CAN_EDIT))
        // return;
        mArray.clear();
        for (String str : strings) {
            mArray.add(str);
        }
        mList.setSelection(index);
        mAdapter.notifyDataSetChanged();
        if (index == (strings.size() - 1)) {
            mListEnd = true;
        } else {
            mListEnd = false;
        }
    }
    
    public void setListViewAdapter(String prefix, ArrayList<String> strings, int index) {
        setArrayListStrings(strings);
        // if(mEnableList && (mEditEnable == DROPDOWN_MENU_BUTTON_CAN_EDIT))
        // return;
        mArray.clear();
        for (String str : strings) {
            if (str.startsWith(prefix)) {
                mArray.add(str);
            }
        }
        if (mArray.size() == 0) {
            mArray.add(mContext.getString(R.string.system_search_city_null));
        }
        mList.setSelection(index);
        mAdapter.notifyDataSetChanged();
    }
    
    public void setListViewListener() {
        mList.setDivider(null);
        mList.setDividerHeight(0);
        mList.setAdapter(mAdapter);
        mList.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (mListEnd) {
                        Log.e(TAG, "list view is end");
                        return true;
                    } else if (mList.getCount() == 1) {
                        Log.e(TAG,
                                "list view only one item,SelectedItemPosition = "
                                        + mList.getSelectedItemPosition());
                        return true;
                    }
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mEnableList) {
                        mEnableList = false;
                        mListEnd = false;
                        Log.e(TAG, "mListEnd = false");
                        showDropdownListEnable(false);
                        if (mEditEnable == DROPDOWN_MENU_BUTTON_CAN_EDIT) {
                            ((EditText) mObject).setCursorVisible(false);
                            ((EditText) mObject).requestFocus();
                        } else {
                            ((TextView) mObject).requestFocus();
                        }
                        return true;
                    }
                } else if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                        || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                    if (mEnableList) {
                        return true;
                    }
                }
                return false;
            }
        });
        
        mList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position,
                    long arg3) {
                
                String item = (String) mList.getItemAtPosition(position);
                mEnableList = false;
                
                Message msg = new Message();
                msg.what = DROPDOWN_MENU_ITEM_CHICK;
                msg.arg1 = mEditEnable;
                msg.arg2 = position;
                if (mEditEnable == DROPDOWN_MENU_BUTTON_CAN_EDIT) {
                    msg.obj = mObject;
                    if (checkString(item)) {
                        ((EditText) mObject).setText(item);
                    } else {
                        ((EditText) mObject).setText(mEditDefault);
                    }
                    
                    int size = ((EditText) mObject).getText().length();
                    Selection.setSelection(((EditText) mObject).getText(), size);
                    ((EditText) mObject).requestFocus();
                    ((EditText) mObject).setCursorVisible(false);
                    // saveCity(edit.getText().toString());
                } else {
                    msg.obj = (TextView) mObject;
                    ((TextView) mObject).requestFocus();
                    ((TextView) mObject).setText(item);
                }
                mHandler.sendMessage(msg);
                showDropdownListEnable(false);
                Log.e(TAG, "mListEnd = false");
                mListEnd = false;
            }
            
        });
        mList.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int porsition, long arg3) {
                Log.e(TAG, " onItemSelected ---begin   mListEnd = " + mListEnd + " porsition = " + porsition + " Count = "
                        + mList.getCount());
                if (porsition >= mList.getCount() - 1) {
                    if (!mListEnd) {
                        mListEnd = true;
                    }
                } else {
                    if (mListEnd) {
                        mListEnd = false;
                    }
                }
                Log.e(TAG, " onItemSelected  end-----  mListEnd = " + mListEnd + " porsition = " + porsition + " Count = "
                        + mList.getCount());
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }
    
    public void showDropdownListEnable(boolean enable) {
        if (enable) {
            mListLayout.setFocusable(true);
            mListLayout.setFocusableInTouchMode(true);
            mListLayout.setVisibility(View.VISIBLE);
        } else {
            mListLayout.setFocusable(false);
            mListLayout.setFocusableInTouchMode(false);
            mListLayout.setVisibility(View.GONE);
        }
    }
    
    public boolean checkString(String name) {
        for (int i = 0; i < mStrings.size(); i++) {
            String str = mStrings.get(i);
            if (name.equals(str)) {
                return true;
            }
        }
        return false;
    }
    
    public void setArrayListStrings(String[] strings) {
        mStrings.clear();
        for (String str : strings) {
            mStrings.add(str);
        }
    }
    
    public void setArrayListStrings(ArrayList<String> strings) {
        mStrings.clear();
        for (String str : strings) {
            mStrings.add(str);
        }
    }
    
    void getDropdownListArray(String prefix, ArrayList<String> arrays) {
        mArray.clear();
        for (String str : arrays) {
            if (str.startsWith(prefix)) {
                mArray.add(str);
            }
        }
        if (mArray.size() == 0) {
            mArray.add(mContext.getString(R.string.system_search_city_null));
        }
        mListEnd = false;
    }
}
