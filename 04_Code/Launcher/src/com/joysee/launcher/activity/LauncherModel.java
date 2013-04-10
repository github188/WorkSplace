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

package com.joysee.launcher.activity;

import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.joysee.launcher.cache.IconCache;
import com.joysee.launcher.common.AllAppsList;
import com.joysee.launcher.common.ApplicationInfo;
import com.joysee.launcher.common.DeferredHandler;
import com.joysee.launcher.common.FolderInfo;
import com.joysee.launcher.common.ItemInfo;
import com.joysee.launcher.common.LauncherSettings;
import com.joysee.launcher.common.LauncherSettings.AppMenu;
import com.joysee.launcher.common.ShortcutInfo;
import com.joysee.launcher.utils.FastBitmapDrawable;
import com.joysee.launcher.utils.LauncherLog;
import com.joysee.launcher.utils.Utilities;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver {
    static final boolean DEBUG_LOADERS = true;
    static final String TAG = "com.joysee.launcher.activity.LauncherModel";

    private static final int ITEMS_CHUNK = 6; // batch size for the workspace icons
    private final boolean mAppsCanBeOnExternalStorage;
    private int mBatchSize; // 0 is all apps at once
    private int mAllAppsLoadDelay; // milliseconds between batches

    private final LauncherApplication mApp;
    private final Object mLock = new Object();
    private DeferredHandler mHandler = new DeferredHandler();
    private LoaderTask mLoaderTask;

    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    // We start off with everything not loaded.  After that, we assume that
    // our monitoring of the package manager provides all updates and we never
    // need to do a requery.  These are only ever touched from the loader thread.
    private boolean mMenuLoaded;
    private boolean mDropDownItemLoaded;
    private boolean mAllAppsLoaded;

    private WeakReference<Callbacks> mCallbacks;

    // < only access in worker thread >
    private AllAppsList mAllAppsList;

    // sItemsIdMap maps *all* the ItemInfos (shortcuts, folders, and widgets) created by
    // LauncherModel to their ids
    static final HashMap<Long, ItemInfo> sItemsIdMap = new HashMap<Long, ItemInfo>();

    // sItems is passed to bindItems, which expects a list of all folders and shortcuts created by
    //       LauncherModel that are directly on the home screen (however, no widgets or shortcuts
    //       within folders).
    static final ArrayList<ItemInfo> sWorkspaceItems = new ArrayList<ItemInfo>();

    // sAppWidgets is all LauncherAppWidgetInfo created by LauncherModel. Passed to bindAppWidget()
//    static final ArrayList<LauncherAppWidgetInfo> sAppWidgets =
//        new ArrayList<LauncherAppWidgetInfo>();

    // sFolders is all FolderInfos created by LauncherModel. Passed to bindFolders()
    static final HashMap<Long, FolderInfo> sFolders = new HashMap<Long, FolderInfo>();
    
    static final HashMap<Long, ShortcutInfo> sDropDownItems = new HashMap<Long, ShortcutInfo>();

    // sDbIconCache is the set of ItemInfos that need to have their icons updated in the database
    static final HashMap<Object, byte[]> sDbIconCache = new HashMap<Object, byte[]>();

    // </ only access in worker thread >

    private IconCache mIconCache;
    private Bitmap mDefaultIcon;

    private static int mCellCountX;
    private static int mCellCountY;

    protected int mPreviousConfigMcc;

    public interface Callbacks {
        public boolean setLoadOnResume();
        public int getCurrentWorkspaceScreen();
        public void startBinding();
        public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end);
        public void bindFolders(HashMap<Long,FolderInfo> folders);
        public void finishBindingItems();
//        public void bindAppWidget(LauncherAppWidgetInfo info);
        public void bindAllApplications(ArrayList<ApplicationInfo> apps);
        public void bindAppsAdded(ArrayList<ApplicationInfo> apps);
        public void bindAppsUpdated(ArrayList<ApplicationInfo> apps);
        public void bindAppsRemoved(ArrayList<ApplicationInfo> apps, boolean permanent);
        public void bindPackagesUpdated();
        public boolean isAllAppsVisible();
        public void bindSearchablesChanged();
    }

    LauncherModel(LauncherApplication app, IconCache iconCache) {
        mAppsCanBeOnExternalStorage = !Environment.isExternalStorageEmulated();
        mApp = app;
        mAllAppsList = new AllAppsList(iconCache);
        mIconCache = iconCache;

//        mDefaultIcon = Utilities.createIconBitmap(
//                mIconCache.getFullResDefaultActivityIcon(), app);

//        final Resources res = app.getResources();
//        mAllAppsLoadDelay = res.getInteger(R.integer.config_allAppsBatchLoadDelay);
//        mBatchSize = res.getInteger(R.integer.config_allAppsBatchSize);
//        Configuration config = res.getConfiguration();
//        mPreviousConfigMcc = config.mcc;
    }

    public Bitmap getFallbackIcon() {
        return Bitmap.createBitmap(mDefaultIcon);
    }

    public void unbindWorkspaceItems() {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                unbindWorkspaceItemsOnMainThread();
            }
        });
    }

    /** Unbinds all the sWorkspaceItems on the main thread, and return a copy of sWorkspaceItems
     * that is save to reference from the main thread. */
    private ArrayList<ItemInfo> unbindWorkspaceItemsOnMainThread() {
        // Ensure that we don't use the same workspace items data structure on the main thread
        // by making a copy of workspace items first.
        final ArrayList<ItemInfo> workspaceItems = new ArrayList<ItemInfo>(sWorkspaceItems);
//        final ArrayList<ItemInfo> appWidgets = new ArrayList<ItemInfo>(sAppWidgets);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
               for (ItemInfo item : workspaceItems) {
                   item.unbind();
               }
            }
        });

        return workspaceItems;
    }

    /**
     * Adds an item to the DB if it was not created previously, or move it to a new
     * <container, screen, cellX, cellY>
     */
    static void addOrMoveItemInDatabase(Context context, ItemInfo item, long container,
            int screen, int cellX, int cellY) {
        if (item.getContainer() == ItemInfo.getNoId()) {
            // From all apps
            addItemToDatabase(context, item, container, screen, cellX, cellY, false);
        } else {
            // From somewhere else
            moveItemInDatabase(context, item, container, screen, cellX, cellY);
        }
    }

    static void updateItemInDatabaseHelper(Context context, final ContentValues values,
            final ItemInfo item, final String callingFunction) {
        final long itemId = item.getId();
        final Uri uri = LauncherSettings.AppMenu.getContentUri(itemId, false);
        final ContentResolver cr = context.getContentResolver();

        Runnable r = new Runnable() {
            public void run() {
                cr.update(uri, values, null, null);

                ItemInfo modelItem = sItemsIdMap.get(itemId);
                if (item != modelItem) {
                    // the modelItem needs to match up perfectly with item if our model is to be
                    // consistent with the database-- for now, just require modelItem == item
                    String msg = "item: " + ((item != null) ? item.toString() : "null") +
                        "modelItem: " + ((modelItem != null) ? modelItem.toString() : "null") +
                        "Error: ItemInfo passed to " + callingFunction + " doesn't match original";
                    throw new RuntimeException(msg);
                }

                // Items are added/removed from the corresponding FolderInfo elsewhere, such
                // as in Workspace.onDrop. Here, we just add/remove them from the list of items
                // that are on the desktop, as appropriate
                if (modelItem.getContainer() == LauncherSettings.AppMenu.CONTAINER_DESKTOP ||
                        modelItem.getContainer() == LauncherSettings.AppMenu.CONTAINER_HOTSEAT) {
                    if (!sWorkspaceItems.contains(modelItem)) {
                        sWorkspaceItems.add(modelItem);
                    }
                } else {
                    sWorkspaceItems.remove(modelItem);
                }
            }
        };

        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }
    /**
     * Move an item in the DB to a new <container, screen, cellX, cellY>
     */
    static void moveItemInDatabase(Context context, final ItemInfo item, final long container,
            final int screen, final int cellX, final int cellY) {
//        item.setContainer(container);
//        item.setCellX(cellX);
//        item.setCellY(cellY);
//
//        // We store hotseat items in canonical form which is this orientation invariant position
//        // in the hotseat
//        if (context instanceof Launcher && screen < 0 &&
//                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
//            item.setScreen(((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY));
//        } else {
//            item.setScreen(screen);
//        }
//
//        final ContentValues values = new ContentValues();
//        values.put(LauncherSettings.Favorites.CONTAINER, item.getContainer());
//        values.put(LauncherSettings.Favorites.CELLX, item.getCellX());
//        values.put(LauncherSettings.Favorites.CELLY, item.getCellY());
//        values.put(LauncherSettings.Favorites.SCREEN, item.getScreen());
//
//        updateItemInDatabaseHelper(context, values, item, "moveItemInDatabase");
    }

    /**
     * Resize an item in the DB to a new <spanX, spanY, cellX, cellY>
     */
    static void resizeItemInDatabase(Context context, final ItemInfo item, final int cellX,
            final int cellY, final int spanX, final int spanY) {

        final ContentValues values = new ContentValues();
        values.put(LauncherSettings.AppMenu.CONTAINER, item.getContainer());
        updateItemInDatabaseHelper(context, values, item, "resizeItemInDatabase");
    }


    /**
     * Update an item to the database in a specified container.
     */
    static void updateItemInDatabase(Context context, final ItemInfo item) {
        final ContentValues values = new ContentValues();
        item.onAddToDatabase(values);
        updateItemInDatabaseHelper(context, values, item, "updateItemInDatabase");
    }

    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExists(Context context, String title, Intent intent) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.AppMenu.CONTENT_URI,
            new String[] { "title", "intent" }, "title=? and intent=?",
            new String[] { title, intent.toUri(0) }, null);
        boolean result = false;
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }
        return result;
    }

    /**
     * Returns an ItemInfo array containing all the items in the LauncherModel.
     * The ItemInfo.id is not set through this function.
     */
    static ArrayList<ItemInfo> getItemsInLocalCoordinates(Context context) {
        ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.AppMenu.CONTENT_URI, new String[] {
                LauncherSettings.AppMenu.ITEM_TYPE, LauncherSettings.AppMenu.CONTAINER}, null, null, null);

        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.ITEM_TYPE);
        final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.CONTAINER);

        try {
            while (c.moveToNext()) {
                ItemInfo item = new ItemInfo();
                item.setContainer(c.getInt(containerIndex));
                item.setItemType(c.getInt(itemTypeIndex));

                items.add(item);
            }
        } catch (Exception e) {
            items.clear();
        } finally {
            c.close();
        }

        return items;
    }

    /**
     * Find a folder in the db, creating the FolderInfo if necessary, and adding it to folderList.
     */
    FolderInfo getFolderById(Context context, HashMap<Long,FolderInfo> folderList, long id) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.AppMenu.CONTENT_URI, null,
                "_id=? and (itemType=? or itemType=?)",
                new String[] { String.valueOf(id),
                        String.valueOf(LauncherSettings.AppMenu.ITEM_TYPE_FOLDER)}, null);

        try {
            if (c.moveToFirst()) {
                final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.ITEM_TYPE);
                final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.APPNAME);
                final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.AppMenu.CONTAINER);

                FolderInfo folderInfo = null;
                switch (c.getInt(itemTypeIndex)) {
                    case LauncherSettings.AppMenu.ITEM_TYPE_FOLDER:
                        folderInfo = findOrMakeFolder(folderList, id);
                        break;
                }

                folderInfo.setTitle(c.getString(titleIndex));
                folderInfo.setId(id);
                folderInfo.setContainer(c.getInt(containerIndex));

                return folderInfo;
            }
        } finally {
            c.close();
        }

        return null;
    }

    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    static void addItemToDatabase(Context context, final ItemInfo item, final long container,
            final int screen, final int cellX, final int cellY, final boolean notify) {
//        item.setContainer(container);
//        item.setCellX(cellX);
//        item.setCellY(cellY);
//        // We store hotseat items in canonical form which is this orientation invariant position
//        // in the hotseat
//        if (context instanceof Launcher && screen < 0 &&
//                container == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
//            item.setScreen(((Launcher) context).getHotseat().getOrderInHotseat(cellX, cellY));
//        } else {
//            item.setScreen(screen);
//        }
//
//        final ContentValues values = new ContentValues();
//        final ContentResolver cr = context.getContentResolver();
//        item.onAddToDatabase(values);
//
//        LauncherApplication app = (LauncherApplication) context.getApplicationContext();
//        item.setId(app.getLauncherProvider().generateNewId());
//        values.put(LauncherSettings.Favorites._ID, item.getId());
//        item.updateValuesWithCoordinates(values, item.getCellX(), item.getCellY());
//
//        Runnable r = new Runnable() {
//            public void run() {
//                cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI :
//                        LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);
//
//                if (sItemsIdMap.containsKey(item.getId())) {
//                    // we should not be adding new items in the db with the same id
//                    throw new RuntimeException("Error: ItemInfo id (" + item.getId() + ") passed to " +
//                        "addItemToDatabase already exists." + item.toString());
//                }
//                sItemsIdMap.put(item.getId(), item);
//                switch (item.getItemType()) {
//                    case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
//                        sFolders.put(item.getId(), (FolderInfo) item);
//                        // Fall through
//                    case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
//                    case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
//                        if (item.getContainer() == LauncherSettings.Favorites.CONTAINER_DESKTOP ||
//                                item.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
//                            sWorkspaceItems.add(item);
//                        }
//                        break;
////                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
////                        sAppWidgets.add((LauncherAppWidgetInfo) item);
////                        break;
//                }
//            }
//        };
//
//        if (sWorkerThread.getThreadId() == Process.myTid()) {
//            r.run();
//        } else {
//            sWorker.post(r);
//        }
    }

    /**
     * Creates a new unique child id, for a given cell span across all layouts.
     */
    static int getCellLayoutChildId(
            long container, int screen, int localCellX, int localCellY, int spanX, int spanY) {
        return (((int) container & 0xFF) << 24)
                | (screen & 0xFF) << 16 | (localCellX & 0xFF) << 8 | (localCellY & 0xFF);
    }

    static int getCellCountX() {
        return mCellCountX;
    }

    static int getCellCountY() {
        return mCellCountY;
    }

    /**
     * Updates the model orientation helper to take into account the current layout dimensions
     * when performing local/canonical coordinate transformations.
     */
    static void updateWorkspaceLayoutCells(int shortAxisCellCount, int longAxisCellCount) {
        mCellCountX = shortAxisCellCount;
        mCellCountY = longAxisCellCount;
    }

    /**
     * Removes the specified item from the database
     * @param context
     * @param item
     */
    static void deleteItemFromDatabase(Context context, final ItemInfo item) {
        final ContentResolver cr = context.getContentResolver();
        final Uri uriToDelete = LauncherSettings.AppMenu.getContentUri(item.getId(), false);
        Runnable r = new Runnable() {
            public void run() {
                cr.delete(uriToDelete, null, null);
                switch (item.getItemType()) {
                    case LauncherSettings.AppMenu.ITEM_TYPE_FOLDER:
                        sFolders.remove(item.getId());
                        sWorkspaceItems.remove(item);
                        break;
                    case LauncherSettings.AppMenu.ITEM_TYPE_APPLICATION:
                    case LauncherSettings.AppMenu.ITEM_TYPE_SHORTCUT:
                        sWorkspaceItems.remove(item);
                        break;
//                    case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
//                        sAppWidgets.remove((LauncherAppWidgetInfo) item);
//                        break;
                }
                sItemsIdMap.remove(item.getId());
                sDbIconCache.remove(item);
            }
        };
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }

    /**
     * Remove the contents of the specified folder from the database
     */
    static void deleteFolderContentsFromDatabase(Context context, final FolderInfo info) {
        final ContentResolver cr = context.getContentResolver();

        Runnable r = new Runnable() {
            public void run() {
                cr.delete(LauncherSettings.AppMenu.getContentUri(info.getId(), false), null, null);
                sItemsIdMap.remove(info.getId());
                sFolders.remove(info.getId());
                sDbIconCache.remove(info);
                sWorkspaceItems.remove(info);

                cr.delete(LauncherSettings.AppMenu.CONTENT_URI_NO_NOTIFICATION,
                        LauncherSettings.AppMenu.CONTAINER + "=" + info.getId(), null);
                for (ItemInfo childInfo : info.getContents()) {
                    sItemsIdMap.remove(childInfo.getId());
                    sDbIconCache.remove(childInfo);
                }
            }
        };
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            sWorker.post(r);
        }
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }

    /**
     * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED and
     * ACTION_PACKAGE_CHANGED.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        LauncherLog.log_D(TAG, "onReceive intent=" + intent);

        final String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            int op = PackageUpdatedTask.OP_NONE;

            if (packageName == null || packageName.length() == 0) {
                // they sent us a bad intent
                return;
            }

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = PackageUpdatedTask.OP_UPDATE;
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_REMOVE;
                }
                // else, we are replacing the package, so a PACKAGE_ADDED will be sent
                // later, we will update the package at this time
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_ADD;
                } else {
                    op = PackageUpdatedTask.OP_UPDATE;
                }
            }

            if (op != PackageUpdatedTask.OP_NONE) {
                enqueuePackageUpdated(new PackageUpdatedTask(op, new String[] { packageName }));
            }

        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            // First, schedule to add these apps back in.
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_ADD, packages));
            // Then, rebind everything.
            startLoaderFromBackground();
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(
                        PackageUpdatedTask.OP_UNAVAILABLE, packages));
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            // If we have changed locale we need to clear out the labels in all apps/workspace.
            forceReload();
        } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
             // Check if configuration change was an mcc/mnc change which would affect app resources
             // and we would need to clear out the labels in all apps/workspace. Same handling as
             // above for ACTION_LOCALE_CHANGED
             Configuration currentConfig = context.getResources().getConfiguration();
             if (mPreviousConfigMcc != currentConfig.mcc) {
                   LauncherLog.log_D(TAG, "Reload apps on config change. curr_mcc:"
                       + currentConfig.mcc + " prevmcc:" + mPreviousConfigMcc);
                   forceReload();
             }
             // Update previousConfig
             mPreviousConfigMcc = currentConfig.mcc;
        } /*else if (SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED.equals(action) ||
                   SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED.equals(action)) {
            if (mCallbacks != null) {
                Callbacks callbacks = mCallbacks.get();
                if (callbacks != null) {
                    callbacks.bindSearchablesChanged();
                }
            }
        }*/
    }

    private void forceReload() {
        synchronized (mLock) {
            // Stop any existing loaders first, so they don't set mAllAppsLoaded or
            // mWorkspaceLoaded to true later
            stopLoaderLocked();
            mDropDownItemLoaded = false;
            mAllAppsLoaded = false;
            mMenuLoaded = false;
        }
        // Do this here because if the launcher activity is running it will be restarted.
        // If it's not running startLoaderFromBackground will merely tell it that it needs
        // to reload.
        startLoaderFromBackground();
    }

    /**
     * When the launcher is in the background, it's possible for it to miss paired
     * configuration changes.  So whenever we trigger the loader from the background
     * tell the launcher that it needs to re-run the loader when it comes back instead
     * of doing it now.
     */
    public void startLoaderFromBackground() {
        boolean runLoader = false;
        if (mCallbacks != null) {
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                // Only actually run the loader if they're not paused.
                if (!callbacks.setLoadOnResume()) {
                    runLoader = true;
                }
            }
        }
        if (runLoader) {
            startLoader(mApp, false);
        }
    }

    // If there is already a loader task running, tell it to stop.
    // returns true if isLaunching() was true on the old task
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    public void startLoader(Context context, boolean isLaunching) {
        synchronized (mLock) {
        	LauncherLog.log_D(TAG, "startLoader isLaunching=" + isLaunching);
            

            // Don't bother to start the thread if we know it's not going to do anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop.
                // also, don't downgrade isLaunching if we're already running
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(context, isLaunching);
                sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                sWorker.post(mLoaderTask);
            }
        }
    }

    public void stopLoader() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                mLoaderTask.stopLocked();
            }
        }
    }

    public boolean isAllAppsLoaded() {
        return mAllAppsLoaded;
    }

    /**
     * Runnable for the thread that loads the contents of the launcher:
     *   - workspace icons
     *   - widgets
     *   - all apps icons
     */
    private class LoaderTask implements Runnable {
        private Context mContext;
        private Thread mWaitThread;
        private boolean mIsLaunching;
        private boolean mStopped;
        private boolean mLoadAndBindStepFinished;
        private HashMap<Object, CharSequence> mLabelCache;

        LoaderTask(Context context, boolean isLaunching) {
            mContext = context;
            mIsLaunching = isLaunching;
            mLabelCache = new HashMap<Object, CharSequence>();
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        private void loadAndBindMenu() {
            // Load the workspace
        	LauncherLog.log_D(TAG, "loadAndBindMenu mMenuLoaded=" + mMenuLoaded);
            

            if (!mMenuLoaded) {
                loadMenu();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mMenuLoaded = true;
                }
            }
            
			if (!mDropDownItemLoaded) {
				loadDropdownListItemByBatch();
				synchronized (LoaderTask.this) {
					if (mStopped) {
						return;
					}
					mDropDownItemLoaded = true;
				}
			}

            // Bind the workspace
            bindMenu();
        }

        private void waitForIdle() {
            // Wait until the either we're stopped or the other threads are done.
            // This way we don't start loading all apps until the workspace has settled
            // down.
            synchronized (LoaderTask.this) {
                final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                mHandler.postIdle(new Runnable() {
                        public void run() {
                            synchronized (LoaderTask.this) {
                                mLoadAndBindStepFinished = true;
                                if (DEBUG_LOADERS) {
                                    LauncherLog.log_D(TAG, "done with previous binding step");
                                }
                                LoaderTask.this.notify();
                            }
                        }
                    });

                while (!mStopped && !mLoadAndBindStepFinished) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
                if (DEBUG_LOADERS) {
                    LauncherLog.log_D(TAG, "waited "
                            + (SystemClock.uptimeMillis()-workspaceWaitTime)
                            + "ms for previous step to finish binding");
                }
            }
        }

        public void run() {
            // Optimize for end-user experience: if the Launcher is up and // running with the
            // All Apps interface in the foreground, load All Apps first. Otherwise, load the
            // workspace first (default).
            final Callbacks cbk = mCallbacks.get();
            final boolean loadWorkspaceFirst = cbk != null ? (!cbk.isAllAppsVisible()) : true;

            keep_running: {
                // Elevate priority when Home launches for the first time to avoid
                // starving at boot time. Staring at a blank home is not cool.
                synchronized (mLock) {
                    LauncherLog.log_D(TAG, "Setting thread priority to " + (mIsLaunching ? "DEFAULT" : "BACKGROUND"));
                    android.os.Process.setThreadPriority(mIsLaunching
                            ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);
                }
                if (loadWorkspaceFirst) {
                    LauncherLog.log_D(TAG, "step 1: loading workspace");
                    loadAndBindMenu();
                } else {
                    LauncherLog.log_D(TAG, "step 1: special: loading all apps");
                    loadAndBindAllApps();
                }

                if (mStopped) {
                    break keep_running;
                }

                // Whew! Hard work done.  Slow us down, and wait until the UI thread has
                // settled down.
                synchronized (mLock) {
                    if (mIsLaunching) {
                        LauncherLog.log_D(TAG, "Setting thread priority to BACKGROUND");
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    }
                }
                waitForIdle();
//
                // second step
                if (loadWorkspaceFirst) {
                    LauncherLog.log_D(TAG, "step 2: loading all apps");
                    loadAndBindAllApps();
                } else {
                    LauncherLog.log_D(TAG, "step 2: special: loading workspace");
                    
                }

                // Restore the default thread priority after we are done loading items
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }


            // Update the saved icons if necessary
            LauncherLog.log_D(TAG, "Comparing loaded icons to database icons");
//            for (Object key : sDbIconCache.keySet()) {
//                updateSavedIcon(mContext, (ShortcutInfo) key, sDbIconCache.get(key));
//            }
//            sDbIconCache.clear();

            // Clear out this reference, otherwise we end up holding it until all of the
            // callback runnables are done.
            mContext = null;

            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
            }
        }
        
        private void loadAndBindAllApps() {
            LauncherLog.log_D(TAG, "loadAndBindAllApps mAllAppsLoaded=" + mAllAppsLoaded);
            
            if (!mAllAppsLoaded) {
                loadAllAppsByBatch();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mAllAppsLoaded = true;
                }
            } else {
                onlyBindAllApps();
            }
        }
        
        private void loadAllAppsByBatch() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                LauncherLog.log_D(TAG, "LoaderTask running with no launcher (loadAllAppsByBatch)");
                return;
            }

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            final PackageManager packageManager = mContext.getPackageManager();
            List<ResolveInfo> apps = null;

            int N = Integer.MAX_VALUE;

            int startIndex;
            int i=0;
            int batchSize = -1;
            while (i < N && !mStopped) {
                if (i == 0) {
                    mAllAppsList.clear();
                    final long qiaTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                    apps = packageManager.queryIntentActivities(mainIntent, 0);
                    if (DEBUG_LOADERS) {
                    	LauncherLog.log_D(TAG, "queryIntentActivities took "
                                + (SystemClock.uptimeMillis()-qiaTime) + "ms");
                    }
                    if (apps == null) {
                        return;
                    }
                    N = apps.size();
                    if (DEBUG_LOADERS) {
                    	LauncherLog.log_D(TAG, "queryIntentActivities got " + N + " apps");
                    }
                    if (N == 0) {
                        // There are no apps?!?
                        return;
                    }
                    if (mBatchSize == 0) {
                        batchSize = N;
                    } else {
                        batchSize = mBatchSize;
                    }

                    final long sortTime = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
                    LauncherLog.log_D(TAG, "mLabelCache.size()   " + mLabelCache.size());
                    Collections.sort(apps,
                            new LauncherModel.ShortcutNameComparator(packageManager, mLabelCache));
                    LauncherLog.log_D(TAG, "mLabelCache.size()   " + mLabelCache.size());
                    LauncherLog.log_D(TAG, "sort took "
                                + (SystemClock.uptimeMillis()-sortTime) + "ms");
                    
                }

                final long t2 = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

                startIndex = i;
                for (int j=0; i<N && j<batchSize; j++) {
                    // This builds the icon bitmaps.
                    mAllAppsList.add(new ApplicationInfo(packageManager, apps.get(i),
                            mIconCache, mLabelCache));
                    i++;
                }
                LauncherLog.log_D(TAG, "mLabelCache.size()   " + mLabelCache.size());
                LauncherLog.log_D(TAG, "mIconCache.size()   " + mIconCache.getAllIcons().size());
                for(int m=0;m<N;m++){
                	ApplicationInfo item = mAllAppsList.get(m);
                	LauncherLog.log_D(TAG, "item.getTitle()   " + item.getTitle());
                	LauncherLog.log_D(TAG, "mLabelCache.get(m)   " + mLabelCache.get(item.getComponentName()));
                	LauncherLog.log_D(TAG, "item.getComponentName()   " + item.getComponentName());
                }

                final boolean first = i <= batchSize;
                final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                final ArrayList<ApplicationInfo> added = mAllAppsList.added;
                mAllAppsList.added = new ArrayList<ApplicationInfo>();

                mHandler.post(new Runnable() {
                    public void run() {
                        final long t = SystemClock.uptimeMillis();
                        if (callbacks != null) {
                            if (first) {
                                callbacks.bindAllApplications(added);
                            } else {
                                callbacks.bindAppsAdded(added);
                            }
                            if (DEBUG_LOADERS) {
                                LauncherLog.log_D(TAG, "bound " + added.size() + " apps in "
                                    + (SystemClock.uptimeMillis() - t) + "ms");
                            }
                        } else {
                            LauncherLog.log_D(TAG, "not binding apps: no Launcher activity");
                        }
                    }
                });

                if (DEBUG_LOADERS) {
                    LauncherLog.log_D(TAG, "batch of " + (i-startIndex) + " icons processed in "
                            + (SystemClock.uptimeMillis()-t2) + "ms");
                }

                if (mAllAppsLoadDelay > 0 && i < N) {
                    try {
                        if (DEBUG_LOADERS) {
                            LauncherLog.log_D(TAG, "sleeping for " + mAllAppsLoadDelay + "ms");
                        }
                        Thread.sleep(mAllAppsLoadDelay);
                    } catch (InterruptedException exc) { }
                }
            }

            if (DEBUG_LOADERS) {
                LauncherLog.log_D(TAG, "cached all " + N + " apps in "
                        + (SystemClock.uptimeMillis()-t) + "ms"
                        + (mAllAppsLoadDelay > 0 ? " (including delay)" : ""));
            }
        }


        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        /**
         * Gets the callbacks object.  If we've been stopped, or if the launcher object
         * has somehow been garbage collected, return null instead.  Pass in the Callbacks
         * object that was around when the deferred message was scheduled, and if there's
         * a new Callbacks object around then also return null.  This will save us from
         * calling onto it with data that will be ignored.
         */
        Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }

                if (mCallbacks == null) {
                    return null;
                }

                final Callbacks callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    LauncherLog.log_D(TAG, "no mCallbacks");
                    return null;
                }

                return callbacks;
            }
        }

        // check & update map of what's occupied; used to discard overlapping/invalid items
        private boolean checkItemPlacement(ItemInfo occupied[][][], ItemInfo item) {
//            int containerIndex = item.getScreen();
//            if (item.getContainer() == LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
//                // Return early if we detect that an item is under the hotseat button
//                if (Hotseat.isAllAppsButtonRank(item.getScreen())) {
//                    return false;
//                }
//
//                // We use the last index to refer to the hotseat and the screen as the rank, so
//                // test and update the occupied state accordingly
//                if (occupied[Launcher.SCREEN_COUNT][item.getScreen()][0] != null) {
//                    Log.e(TAG, "Error loading shortcut into hotseat " + item
//                        + " into position (" + item.getScreen() + ":" + item.getCellX() + "," + item.getCellY()
//                        + ") occupied by " + occupied[Launcher.SCREEN_COUNT][item.getScreen()][0]);
//                    return false;
//                } else {
//                    occupied[Launcher.SCREEN_COUNT][item.getScreen()][0] = item;
//                    return true;
//                }
//            } else if (item.getContainer() != LauncherSettings.Favorites.CONTAINER_DESKTOP) {
//                // Skip further checking if it is not the hotseat or workspace container
//                return true;
//            }
//
//            // Check if any workspace icons overlap with each other
//            for (int x = item.getCellX(); x < (item.getCellX()+item.getSpanX()); x++) {
//                for (int y = item.getCellY(); y < (item.getCellY()+item.getSpanY()); y++) {
//                    if (occupied[containerIndex][x][y] != null) {
//                        Log.e(TAG, "Error loading shortcut " + item
//                            + " into cell (" + containerIndex + "-" + item.getScreen() + ":"
//                            + x + "," + y
//                            + ") occupied by "
//                            + occupied[containerIndex][x][y]);
//                        return false;
//                    }
//                }
//            }
//            for (int x = item.getCellX(); x < (item.getCellX()+item.getSpanX()); x++) {
//                for (int y = item.getCellY(); y < (item.getCellY()+item.getSpanY()); y++) {
//                    occupied[containerIndex][x][y] = item;
//                }
//            }

            return true;
        }

        private void loadMenu() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();

            sWorkspaceItems.clear();
            sFolders.clear();

            final Cursor c = contentResolver.query(
                    LauncherSettings.AppMenu.CONTENT_URI, null, AppMenu.ITEM_TYPE + "=?", 
                    						new String[]{String.valueOf(AppMenu.ITEM_TYPE_FOLDER)}, AppMenu.ITEMORDER);
			LauncherLog.log_D(TAG, "loadMenu    cursor.getcount       " + c.getCount());
            try {
                final int idIndex = c.getColumnIndexOrThrow(AppMenu._ID);
                final int intentIndex = c.getColumnIndexOrThrow(AppMenu.INTENT);
                final int titleIndex = c.getColumnIndexOrThrow(AppMenu.APPNAME);
                final int iconTypeIndex = c.getColumnIndexOrThrow(AppMenu.ICON_TYPE);
                final int iconIndex = c.getColumnIndexOrThrow(AppMenu.ICON);
                final int iconFocusIndex = c.getColumnIndexOrThrow(AppMenu.ICONFOCUS);
                final int itemTypeIndex = c.getColumnIndexOrThrow(AppMenu.ITEM_TYPE);
                final int uriIndex = c.getColumnIndexOrThrow(AppMenu.URI);
                

                int container;
                long id;
                Intent intent;
                LauncherLog.log_D(TAG, "loadMenu()  mStopped  is " + mStopped);
                while (!mStopped && c.moveToNext()) {
                    try {
                        int itemType = c.getInt(itemTypeIndex);

                        switch (itemType) {
                        case LauncherSettings.AppMenu.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.AppMenu.ITEM_TYPE_SHORTCUT:
                            break;

                        case LauncherSettings.AppMenu.ITEM_TYPE_FOLDER:
                            id = c.getLong(idIndex);
                            FolderInfo folderInfo = findOrMakeFolder(sFolders, id);

                            folderInfo.setTitle(c.getString(titleIndex));
                            folderInfo.setId(id);
                            folderInfo.setIntent(Intent.parseUri(c.getString(intentIndex), 0));
                            byte[] iconByte = c.getBlob(iconIndex);
                            LauncherLog.log_D(TAG, "loadMenu()  iconByte size() is   " + iconByte.length);
                            Bitmap icon = BitmapFactory.decodeByteArray(iconByte, 0, iconByte.length);
                            folderInfo.setIcon(icon);
                            byte[] iconFocusByte = c.getBlob(iconFocusIndex);
                            LauncherLog.log_D(TAG, "loadMenu()  iconFocusByte size() is   " + iconFocusByte.length);
                            Bitmap iconFocus = BitmapFactory.decodeByteArray(iconFocusByte, 0, iconFocusByte.length);
                            folderInfo.setIconFocus(iconFocus);
                            
                            sItemsIdMap.put(folderInfo.getId(), folderInfo);
                            sFolders.put(folderInfo.getId(), folderInfo);
                            break;
                        }
                    } catch (Exception e) {
                        LauncherLog.d(TAG, "Desktop menu loading interrupted:", e);
                    }
                }
            } finally {
                c.close();
            }
        }

        /**
         * Read everything out of our database.
         */
        private void bindMenu() {
            final long t = SystemClock.uptimeMillis();

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                LauncherLog.log_D(TAG, "LoaderTask running with no launcher");
                return;
            }

            int N;
            // Tell the workspace that we're about to start firing items at it
            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.startBinding();
                    }
                }
            });

            // Ensure that we don't use the same folders data structure on the main thread
            final HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>(sFolders);
            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindFolders(folders);
                        LauncherLog.log_D(TAG,"bindFolders");
                    }
                }
            });
            // Tell the workspace that we're done.
            mHandler.post(new Runnable() {
                public void run() {
                    Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.finishBindingItems();
                    }
                }
            });
            // If we're profiling, this is the last thing in the queue.
            mHandler.post(new Runnable() {
                public void run() {
                    if (DEBUG_LOADERS) {
                        LauncherLog.log_D(TAG, "bound menu in "
                            + (SystemClock.uptimeMillis()-t) + "ms");
                    }
                }
            });
        }

        private void onlyBindAllApps() {
//            final Callbacks oldCallbacks = mCallbacks.get();
//            if (oldCallbacks == null) {
//                // This launcher has exited and nobody bothered to tell us.  Just bail.
//                LauncherLog.log_D(TAG, "LoaderTask running with no launcher (onlyBindAllApps)");
//                return;
//            }
//
//            // shallow copy
//            final ArrayList<ApplicationInfo> list
//                    = (ArrayList<ApplicationInfo>) mAllAppsList.data.clone();
//            mHandler.post(new Runnable() {
//                public void run() {
//                    final long t = SystemClock.uptimeMillis();
//                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
//                    if (callbacks != null) {
//                        callbacks.bindAllApplications(list);
//                    }
//                    if (DEBUG_LOADERS) {
//                        LauncherLog.log_D(TAG, "bound all " + list.size() + " apps from cache in "
//                                + (SystemClock.uptimeMillis()-t) + "ms");
//                    }
//                }
//            });

        }

        private void loadDropdownListItemByBatch() {
            final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

            // Don't use these two variables in any of the callback runnables.
            // Otherwise we hold a reference to them.
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                // This launcher has exited and nobody bothered to tell us.  Just bail.
                LauncherLog.log_D(TAG, "LoaderTask running with no launcher (loadAllAppsByBatch)");
                return;
            }
            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
//            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            sDropDownItems.clear();
            
            final Cursor c = contentResolver.query(
                    LauncherSettings.AppMenu.CONTENT_URI, null, AppMenu.ITEM_TYPE + "=?", 
                    						new String[]{String.valueOf(AppMenu.ITEM_TYPE_SHORTCUT)}, AppMenu.ITEMORDER);
			LauncherLog.log_D(TAG, "loadDropdownListItemByBatch    cursor.getcount       " + c.getCount());
            
			try {
                final int idIndex = c.getColumnIndexOrThrow(AppMenu._ID);
                final int intentIndex = c.getColumnIndexOrThrow(AppMenu.INTENT);
                final int titleIndex = c.getColumnIndexOrThrow(AppMenu.APPNAME);
                final int itemTypeIndex = c.getColumnIndexOrThrow(AppMenu.ITEM_TYPE);
                final int parentIndex = c.getColumnIndexOrThrow(AppMenu.PARENTITEM);
                final int orderIndex = c.getColumnIndexOrThrow(AppMenu.ITEMORDER);
                
                long id;
                LauncherLog.log_D(TAG, "loadMenu()  mStopped  is " + mStopped);
                while (!mStopped && c.moveToNext()) {
                    try {
                        id = c.getLong(idIndex);
                        ShortcutInfo appInfo = findOrMakeListItem(sDropDownItems, id);

                        appInfo.setTitle(c.getString(titleIndex));
                        appInfo.setId(id);
                        appInfo.setIntent(Intent.parseUri(c.getString(intentIndex), 0));
                        appInfo.setParentIndex(c.getInt(parentIndex));
                        appInfo.setOrder(c.getInt(orderIndex));
                        
                        sDropDownItems.put(appInfo.getId(), appInfo);
                        
                        LauncherLog.log_D(TAG, "appInfo   id " + appInfo.getId() + "     appinfo    title       " + appInfo.getTitle());
                    } catch (Exception e) {
                        LauncherLog.d(TAG, "Desktop menu loading interrupted:", e);
                    }
                }
                
                for(FolderInfo folder : sFolders.values()){
                	for(ShortcutInfo item : sDropDownItems.values()){
                		if(folder.getId() == item.getParentIndex()) {
                			folder.addDropDownItem(item);
                			LauncherLog.log_D(TAG, "add listitem to menu " + folder.getId() + "   item.getid " + item.getId());
                		}
                	}
                	LauncherLog.log_D(TAG, "folder size is " + folder.getChilds().size() );
                }
                
            } finally {
                c.close();
            }
        }

        public void dumpState() {
            LauncherLog.log_D(TAG, "mLoaderTask.mContext=" + mContext);
            LauncherLog.log_D(TAG, "mLoaderTask.mWaitThread=" + mWaitThread);
            LauncherLog.log_D(TAG, "mLoaderTask.mIsLaunching=" + mIsLaunching);
            LauncherLog.log_D(TAG, "mLoaderTask.mStopped=" + mStopped);
            LauncherLog.log_D(TAG, "mLoaderTask.mLoadAndBindStepFinished=" + mLoadAndBindStepFinished);
            LauncherLog.log_D(TAG, "mItems size=" + sWorkspaceItems.size());
        }
    }

    void enqueuePackageUpdated(PackageUpdatedTask task) {
        sWorker.post(task);
    }

    private class PackageUpdatedTask implements Runnable {
        int mOp;
        String[] mPackages;

        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted


        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        public void run() {
            final Context context = mApp;

            final String[] packages = mPackages;
            final int N = packages.length;
            switch (mOp) {
                case OP_ADD:
                    for (int i=0; i<N; i++) {
                        LauncherLog.log_D(TAG, "mAllAppsList.addPackage " + packages[i]);
                        mAllAppsList.addPackage(context, packages[i]);
                    }
                    break;
                case OP_UPDATE:
                    for (int i=0; i<N; i++) {
                        LauncherLog.log_D(TAG, "mAllAppsList.updatePackage " + packages[i]);
                        mAllAppsList.updatePackage(context, packages[i]);
                    }
                    break;
                case OP_REMOVE:
                case OP_UNAVAILABLE:
                    for (int i=0; i<N; i++) {
                        LauncherLog.log_D(TAG, "mAllAppsList.removePackage " + packages[i]);
                        mAllAppsList.removePackage(packages[i]);
                    }
                    break;
            }

            ArrayList<ApplicationInfo> added = null;
            ArrayList<ApplicationInfo> removed = null;
            ArrayList<ApplicationInfo> modified = null;

            if (mAllAppsList.added.size() > 0) {
                added = mAllAppsList.added;
                mAllAppsList.added = new ArrayList<ApplicationInfo>();
            }
            if (mAllAppsList.removed.size() > 0) {
                removed = mAllAppsList.removed;
                mAllAppsList.removed = new ArrayList<ApplicationInfo>();
                for (ApplicationInfo info: removed) {
                    mIconCache.remove(info.getIntent().getComponent());
                }
            }
            if (mAllAppsList.modified.size() > 0) {
                modified = mAllAppsList.modified;
                mAllAppsList.modified = new ArrayList<ApplicationInfo>();
            }

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            if (callbacks == null) {
                LauncherLog.log_D(TAG, "Nobody to tell about the new app.  Launcher is probably loading.");
                return;
            }

            if (added != null) {
                final ArrayList<ApplicationInfo> addedFinal = added;
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppsAdded(addedFinal);
                        }
                    }
                });
            }
            if (modified != null) {
                final ArrayList<ApplicationInfo> modifiedFinal = modified;
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppsUpdated(modifiedFinal);
                        }
                    }
                });
            }
            if (removed != null) {
                final boolean permanent = mOp != OP_UNAVAILABLE;
                final ArrayList<ApplicationInfo> removedFinal = removed;
                mHandler.post(new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppsRemoved(removedFinal, permanent);
                        }
                    }
                });
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (callbacks == cb && cb != null) {
                        callbacks.bindPackagesUpdated();
                    }
                }
            });
        }
    }

    /**
     * This is called from the code that adds shortcuts from the intent receiver.  This
     * doesn't have a Cursor, but
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context) {
        return getShortcutInfo(manager, intent, context, null, -1, -1, null);
    }

    /**
     * Make an ShortcutInfo object for a shortcut that is an application.
     *
     * If c is not null, then it will be used to fill in missing data like the title and icon.
     */
    public ShortcutInfo getShortcutInfo(PackageManager manager, Intent intent, Context context,
            Cursor c, int iconIndex, int titleIndex, HashMap<Object, CharSequence> labelCache) {
        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();

        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return null;
        }

        try {
            PackageInfo pi = manager.getPackageInfo(componentName.getPackageName(), 0);
            if (!pi.applicationInfo.enabled) {
                // If we return null here, the corresponding item will be removed from the launcher
                // db and will not appear in the workspace.
                return null;
            }
        } catch (NameNotFoundException e) {
            LauncherLog.log_D(TAG, "getPackInfo failed for package " + componentName.getPackageName());
        }

        // TODO: See if the PackageManager knows about this case.  If it doesn't
        // then return null & delete this.

        // the resource -- This may implicitly give us back the fallback icon,
        // but don't worry about that.  All we're doing with usingFallbackIcon is
        // to avoid saving lots of copies of that in the database, and most apps
        // have icons anyway.
        final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);
//        if (resolveInfo != null) {
//            icon = mIconCache.getIcon(componentName, resolveInfo, labelCache);
//        }
        // the db
        if (icon == null) {
            if (c != null) {
                icon = getIconFromCursor(c, iconIndex, context);
            }
        }
        // the fallback icon
        if (icon == null) {
            icon = getFallbackIcon();
            info.setUsingFallbackIcon(true);
        }
        info.setIcon(icon);

        // from the resource
        if (resolveInfo != null) {
            ComponentName key = LauncherModel.getComponentNameFromResolveInfo(resolveInfo);
            if (labelCache != null && labelCache.containsKey(key)) {
                info.setTitle(labelCache.get(key));
            } else {
                info.setTitle(resolveInfo.activityInfo.loadLabel(manager));
                if (labelCache != null) {
                    labelCache.put(key, info.getTitle());
                }
            }
        }
        // from the db
        if (info.getTitle() == null) {
            if (c != null) {
                info.setTitle(c.getString(titleIndex));
            }
        }
        // fall back to the class name of the activity
        if (info.getTitle() == null) {
            info.setTitle(componentName.getClassName());
        }
        info.setItemType(LauncherSettings.AppMenu.ITEM_TYPE_APPLICATION);
        return info;
    }

    /**
     * Make an ShortcutInfo object for a shortcut that isn't an application.
     */
    private ShortcutInfo getShortcutInfo(Cursor c, Context context,
            int iconTypeIndex, int iconPackageIndex, int iconResourceIndex, int iconIndex,
            int titleIndex) {

        Bitmap icon = null;
        final ShortcutInfo info = new ShortcutInfo();
        info.setItemType(LauncherSettings.AppMenu.ITEM_TYPE_SHORTCUT);

        // TODO: If there's an explicit component and we can't install that, delete it.

        info.setTitle(c.getString(titleIndex));

        int iconType = c.getInt(iconTypeIndex);
        switch (iconType) {
        case LauncherSettings.AppMenu.ICON_TYPE_RESOURCE:
            String packageName = c.getString(iconPackageIndex);
            String resourceName = c.getString(iconResourceIndex);
            PackageManager packageManager = context.getPackageManager();
            info.setCustomIcon(false);
            // the resource
//            try {
//                Resources resources = packageManager.getResourcesForApplication(packageName);
//                if (resources != null) {
//                    final int id = resources.getIdentifier(resourceName, null, null);
//                    icon = Utilities.createIconBitmap(
//                            mIconCache.getFullResIcon(resources, id), context);
//                }
//            } catch (Exception e) {
//                // drop this.  we have other places to look for icons
//            }
            // the db
            if (icon == null) {
                icon = getIconFromCursor(c, iconIndex, context);
            }
            // the fallback icon
            if (icon == null) {
                icon = getFallbackIcon();
                info.setUsingFallbackIcon(true);
            }
            break;
        case LauncherSettings.AppMenu.ICON_TYPE_BITMAP:
            icon = getIconFromCursor(c, iconIndex, context);
            if (icon == null) {
                icon = getFallbackIcon();
                info.setCustomIcon(false);
                info.setUsingFallbackIcon(true);
            } else {
                info.setCustomIcon(true);
            }
            break;
        default:
            icon = getFallbackIcon();
            info.setUsingFallbackIcon(true);
            info.setCustomIcon(false);
            break;
        }
        info.setIcon(icon);
        return info;
    }

    Bitmap getIconFromCursor(Cursor c, int iconIndex, Context context) {
        if (false) {
            LauncherLog.log_D(TAG, "getIconFromCursor app="
                    + c.getString(c.getColumnIndexOrThrow(LauncherSettings.AppMenu.APPNAME)));
        }
        byte[] data = c.getBlob(iconIndex);
        try {
            return Utilities.createIconBitmap(
                    BitmapFactory.decodeByteArray(data, 0, data.length), context);
        } catch (Exception e) {
            return null;
        }
    }

    ShortcutInfo addShortcut(Context context, Intent data, long container, int screen,
            int cellX, int cellY, boolean notify) {
        final ShortcutInfo info = infoFromShortcutIntent(context, data, null);
        if (info == null) {
            return null;
        }
        addItemToDatabase(context, info, container, screen, cellX, cellY, notify);

        return info;
    }

    /**
     * Attempts to find an AppWidgetProviderInfo that matches the given component.
     */
    AppWidgetProviderInfo findAppWidgetProviderInfoWithComponent(Context context,
            ComponentName component) {
        List<AppWidgetProviderInfo> widgets =
            AppWidgetManager.getInstance(context).getInstalledProviders();
        for (AppWidgetProviderInfo info : widgets) {
            if (info.provider.equals(component)) {
                return info;
            }
        }
        return null;
    }

    /**
     * Returns a list of all the widgets that can handle configuration with a particular mimeType.
     */
//    List<WidgetMimeTypeHandlerData> resolveWidgetsForMimeType(Context context, String mimeType) {
//        final PackageManager packageManager = context.getPackageManager();
//        final List<WidgetMimeTypeHandlerData> supportedConfigurationActivities =
//            new ArrayList<WidgetMimeTypeHandlerData>();
//
//        final Intent supportsIntent =
//            new Intent(InstallWidgetReceiver.ACTION_SUPPORTS_CLIPDATA_MIMETYPE);
//        supportsIntent.setType(mimeType);
//
//        // Create a set of widget configuration components that we can test against
//        final List<AppWidgetProviderInfo> widgets =
//            AppWidgetManager.getInstance(context).getInstalledProviders();
//        final HashMap<ComponentName, AppWidgetProviderInfo> configurationComponentToWidget =
//            new HashMap<ComponentName, AppWidgetProviderInfo>();
//        for (AppWidgetProviderInfo info : widgets) {
//            configurationComponentToWidget.put(info.configure, info);
//        }
//
//        // Run through each of the intents that can handle this type of clip data, and cross
//        // reference them with the components that are actual configuration components
//        final List<ResolveInfo> activities = packageManager.queryIntentActivities(supportsIntent,
//                PackageManager.MATCH_DEFAULT_ONLY);
//        for (ResolveInfo info : activities) {
//            final ActivityInfo activityInfo = info.activityInfo;
//            final ComponentName infoComponent = new ComponentName(activityInfo.packageName,
//                    activityInfo.name);
//            if (configurationComponentToWidget.containsKey(infoComponent)) {
//                supportedConfigurationActivities.add(
//                        new InstallWidgetReceiver.WidgetMimeTypeHandlerData(info,
//                                configurationComponentToWidget.get(infoComponent)));
//            }
//        }
//        return supportedConfigurationActivities;
//    }

    ShortcutInfo infoFromShortcutIntent(Context context, Intent data, Bitmap fallbackIcon) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        if (intent == null) {
            // If the intent is null, we can't construct a valid ShortcutInfo, so we return null
            Log.e(TAG, "Can't construct ShorcutInfo with null intent");
            return null;
        }

        Bitmap icon = null;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null && bitmap instanceof Bitmap) {
            icon = Utilities.createIconBitmap(new FastBitmapDrawable((Bitmap)bitmap), context);
            customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
//                    icon = Utilities.createIconBitmap(
//                            mIconCache.getFullResIcon(resources, id), context);
                } catch (Exception e) {
                    LauncherLog.log_D(TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        final ShortcutInfo info = new ShortcutInfo();

        if (icon == null) {
            if (fallbackIcon != null) {
                icon = fallbackIcon;
            } else {
                icon = getFallbackIcon();
                info.setUsingFallbackIcon(true);
            }
        }
        info.setIcon(icon);

        info.setTitle(name);
        info.setIntent(intent);
        info.setCustomIcon(customIcon);
        info.setIconResource(iconResource);

        return info;
    }

    boolean queueIconToBeChecked(HashMap<Object, byte[]> cache, ShortcutInfo info, Cursor c,
            int iconIndex) {
        // If apps can't be on SD, don't even bother.
        if (!mAppsCanBeOnExternalStorage) {
            return false;
        }
        // If this icon doesn't have a custom icon, check to see
        // what's stored in the DB, and if it doesn't match what
        // we're going to show, store what we are going to show back
        // into the DB.  We do this so when we're loading, if the
        // package manager can't find an icon (for example because
        // the app is on SD) then we can use that instead.
        if (!info.isCustomIcon() && !info.isUsingFallbackIcon()) {
            cache.put(info, c.getBlob(iconIndex));
            return true;
        }
        return false;
    }
    void updateSavedIcon(Context context, ShortcutInfo info, byte[] data) {
//        boolean needSave = false;
//        try {
//            if (data != null) {
//                Bitmap saved = BitmapFactory.decodeByteArray(data, 0, data.length);
//                Bitmap loaded = info.getIcon(mIconCache);
//                needSave = !saved.sameAs(loaded);
//            } else {
//                needSave = true;
//            }
//        } catch (Exception e) {
//            needSave = true;
//        }
//        if (needSave) {
//            LauncherLog.log_D(TAG, "going to save icon bitmap for info=" + info);
//            // This is slower than is ideal, but this only happens once
//            // or when the app is updated with a new icon.
//            updateItemInDatabase(context, info);
//        }
    }

    /**
     * Return an existing FolderInfo object if we have encountered this ID previously,
     * or make a new one.
     */
    private static FolderInfo findOrMakeFolder(HashMap<Long, FolderInfo> folders, long id) {
        // See if a placeholder was created for us already
        FolderInfo folderInfo = folders.get(id);
        if (folderInfo == null) {
            // No placeholder -- create a new instance
            folderInfo = new FolderInfo();
            folders.put(id, folderInfo);
        }
        return folderInfo;
    }
    
    private static ShortcutInfo findOrMakeListItem(HashMap<Long, ShortcutInfo> items, long id) {
        // See if a placeholder was created for us already
    	ShortcutInfo item = items.get(id);
        if (item == null) {
            // No placeholder -- create a new instance
        	item = new ShortcutInfo();
            items.put(id, item);
        }
        return item;
    }

    private static final Collator sCollator = Collator.getInstance();
    public static final Comparator<ApplicationInfo> APP_NAME_COMPARATOR
            = new Comparator<ApplicationInfo>() {
        public final int compare(ApplicationInfo a, ApplicationInfo b) {
            int result = sCollator.compare(a.getTitle().toString(), b.getTitle().toString());
            if (result == 0) {
                result = a.getComponentName().compareTo(b.getComponentName());
            }
            return result;
        }
    };
    public static final Comparator<ApplicationInfo> APP_INSTALL_TIME_COMPARATOR
            = new Comparator<ApplicationInfo>() {
        public final int compare(ApplicationInfo a, ApplicationInfo b) {
            if (a.getFirstInstallTime() < b.getFirstInstallTime()) return 1;
            if (a.getFirstInstallTime() > b.getFirstInstallTime()) return -1;
            return 0;
        }
    };
    public static final Comparator<AppWidgetProviderInfo> WIDGET_NAME_COMPARATOR
            = new Comparator<AppWidgetProviderInfo>() {
        public final int compare(AppWidgetProviderInfo a, AppWidgetProviderInfo b) {
            return sCollator.compare(a.label.toString(), b.label.toString());
        }
    };
    public static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
        if (info.activityInfo != null) {
            return new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
        } else {
            return new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
        }
    }
    public static class ShortcutNameComparator implements Comparator<ResolveInfo> {
        private PackageManager mPackageManager;
        private HashMap<Object, CharSequence> mLabelCache;
        ShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, CharSequence>();
        }
        ShortcutNameComparator(PackageManager pm, HashMap<Object, CharSequence> labelCache) {
            mPackageManager = pm;
            mLabelCache = labelCache;
        }
        public final int compare(ResolveInfo a, ResolveInfo b) {
            CharSequence labelA, labelB;
            ComponentName keyA = LauncherModel.getComponentNameFromResolveInfo(a);
            ComponentName keyB = LauncherModel.getComponentNameFromResolveInfo(b);
            if (mLabelCache.containsKey(keyA)) {
                labelA = mLabelCache.get(keyA);
            } else {
                labelA = a.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyA, labelA);
            }
            if (mLabelCache.containsKey(keyB)) {
                labelB = mLabelCache.get(keyB);
            } else {
                labelB = b.loadLabel(mPackageManager).toString();

                mLabelCache.put(keyB, labelB);
            }
            return sCollator.compare(labelA, labelB);
        }
    };
    public static class WidgetAndShortcutNameComparator implements Comparator<Object> {
        private PackageManager mPackageManager;
        private HashMap<Object, String> mLabelCache;
        WidgetAndShortcutNameComparator(PackageManager pm) {
            mPackageManager = pm;
            mLabelCache = new HashMap<Object, String>();
        }
        public final int compare(Object a, Object b) {
            String labelA, labelB;
            if (mLabelCache.containsKey(a)) {
                labelA = mLabelCache.get(a);
            } else {
                labelA = (a instanceof AppWidgetProviderInfo) ?
                    ((AppWidgetProviderInfo) a).label :
                    ((ResolveInfo) a).loadLabel(mPackageManager).toString();
                mLabelCache.put(a, labelA);
            }
            if (mLabelCache.containsKey(b)) {
                labelB = mLabelCache.get(b);
            } else {
                labelB = (b instanceof AppWidgetProviderInfo) ?
                    ((AppWidgetProviderInfo) b).label :
                    ((ResolveInfo) b).loadLabel(mPackageManager).toString();
                mLabelCache.put(b, labelB);
            }
            return sCollator.compare(labelA, labelB);
        }
    };

    public void dumpState() {
        LauncherLog.log_D(TAG, "mCallbacks=" + mCallbacks);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.data", mAllAppsList.data);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.added", mAllAppsList.added);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.removed", mAllAppsList.removed);
        ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.modified", mAllAppsList.modified);
        if (mLoaderTask != null) {
            mLoaderTask.dumpState();
        } else {
            LauncherLog.log_D(TAG, "mLoaderTask=null");
        }
    }
}
