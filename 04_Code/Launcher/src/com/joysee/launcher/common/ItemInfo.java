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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo {
    
    static final int NO_ID = -1;
    
    /**
     * The id in the settings database for this item
     */
    long id = NO_ID;
    
    /**
     * One of {@link LauncherSettings.AppMenu#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.AppMenu#ITEM_TYPE_SHORTCUT},
     * {@link LauncherSettings.AppMenu#ITEM_TYPE_FOLDER}, or
     * {@link LauncherSettings.AppMenu#ITEM_TYPE_APPWIDGET}.
     */
    int itemType;
    
    /**
     * The id of the container that holds this item. For the desktop, this will be 
     * {@link LauncherSettings.AppMenu#CONTAINER_DESKTOP}. For the all applications folder it
     * will be {@link #NO_ID} (since it is not stored in the settings DB). For user folders
     * it will be the id of the folder.
     */
    long container = NO_ID;
    
    public ItemInfo() {
    }

    ItemInfo(ItemInfo info) {
        id = info.id;
        itemType = info.itemType;
        container = info.container;
    }

    /**
     * Write the fields of this item to the DB
     * 
     * @param values
     */
    public void onAddToDatabase(ContentValues values) { 
        values.put(LauncherSettings.BaseLauncherColumns.ITEM_TYPE, itemType);
    }

    static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flattenBitmap(bitmap);
            values.put(LauncherSettings.AppMenu.ICON, data);
        }
    }

    /**
     * It is very important that sub-classes implement this if they contain any references
     * to the activity (anything in the view hierarchy etc.). If not, leaks can result since
     * ItemInfo objects persist across rotation and can hence leak by holding stale references
     * to the old view hierarchy / activity.
     */
    public void unbind() {
    }

    @Override
    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + " container=" + this.container
            + ")";
    }
    
    public long getContainer() {
		return container;
	}
    
    public static int getNoId() {
		return NO_ID;
	}
    
    public long getId() {
		return id;
	}

	public int getItemType() {
		return itemType;
	}

	public void setItemType(int itemType) {
		this.itemType = itemType;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setContainer(long container) {
		this.container = container;
	}
    
    
}
