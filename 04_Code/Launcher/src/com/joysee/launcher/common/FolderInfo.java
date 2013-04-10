/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joysee.launcher.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;

/**
 * Represents a folder containing shortcuts or apps.
 */
public class FolderInfo extends ItemInfo {

    /**
     * Whether this folder has been opened
     */
    boolean opened;

    /**
     * The folder name.
     */
    CharSequence title;
    
    Bitmap icon;
    
    Bitmap iconFocus;
    
    Intent intent;
    
    ArrayList<ShortcutInfo> mChilds = new ArrayList<ShortcutInfo>();

    /**
     * The apps and shortcuts
     */
    ArrayList<ShortcutInfo> contents = new ArrayList<ShortcutInfo>();

    ArrayList<FolderListener> listeners = new ArrayList<FolderListener>();

    public FolderInfo() {
        itemType = LauncherSettings.AppMenu.ITEM_TYPE_FOLDER;
    }

    /**
     * Add an app or shortcut
     *
     * @param item
     */
    public void add(ShortcutInfo item) {
        contents.add(item);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onAdd(item);
        }
        itemsChanged();
    }

    /**
     * Remove an app or shortcut. Does not change the DB.
     *
     * @param item
     */
    public void remove(ShortcutInfo item) {
        contents.remove(item);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onRemove(item);
        }
        itemsChanged();
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onTitleChanged(title);
        }
    }

    @Override
    public void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);
        values.put(LauncherSettings.AppMenu.APPNAME, title.toString());
    }

    void addListener(FolderListener listener) {
        listeners.add(listener);
    }

    void removeListener(FolderListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    void itemsChanged() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onItemsChanged();
        }
    }

    @Override
    public void unbind() {
        super.unbind();
        listeners.clear();
    }

    interface FolderListener {
        public void onAdd(ShortcutInfo item);
        public void onRemove(ShortcutInfo item);
        public void onTitleChanged(CharSequence title);
        public void onItemsChanged();
    }

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	public ArrayList<ShortcutInfo> getContents() {
		return contents;
	}

	public void setContents(ArrayList<ShortcutInfo> contents) {
		this.contents = contents;
	}

	public ArrayList<FolderListener> getListeners() {
		return listeners;
	}

	public void setListeners(ArrayList<FolderListener> listeners) {
		this.listeners = listeners;
	}

	public CharSequence getTitle() {
		return title;
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

	public Bitmap getIconFocus() {
		return iconFocus;
	}

	public void setIconFocus(Bitmap iconFocus) {
		this.iconFocus = iconFocus;
	}

	public Intent getIntent() {
		return intent;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
	}

	public ArrayList<ShortcutInfo> getChilds() {
		return mChilds;
	}

	public void setChilds(ArrayList<ShortcutInfo> mChilds) {
		this.mChilds = mChilds;
	}
    
    public void addDropDownItem(ShortcutInfo item){
    	if(!mChilds.contains(item)){
    		this.mChilds.add(item);
    		Collections.sort(mChilds, appComparator);
    	}
    }
    
    private ApplicationInfoComparator appComparator = new ApplicationInfoComparator();
    
    class ApplicationInfoComparator implements Comparator<ShortcutInfo> {

		@Override
		public int compare(ShortcutInfo lhs, ShortcutInfo rhs) {
			int lOrder = lhs.getOrder();
			int rOrder = rhs.getOrder();
			
			if(lOrder < rOrder){
				return -1;
			}else if (lOrder == rOrder){
				return 0;
			}else{
				return 1;
			}
		}

    	
    }
}
