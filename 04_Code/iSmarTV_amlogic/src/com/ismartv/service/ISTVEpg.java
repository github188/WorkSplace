package com.ismartv.service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Date;
import java.util.Comparator;
import java.util.Map.Entry;
import java.lang.Integer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import android.util.Log;

import com.ismartv.util.DataSerial;

public class ISTVEpg{
	private static final String TAG="ISTVEpg";
	private static final int HISTORY_COUNT=50;
	private static final String HISTORY_FILE="/data/data/com.ismartv.ui/history";
	private static final String ACCREDIT_FILE="/data/data/com.ismartv.ui/accredit";

	private HashMap<String, ISTVContentModel> cmMap = new HashMap<String, ISTVContentModel>();
	private HashMap<String, ISTVChannel> channelMap = new HashMap<String, ISTVChannel>();
	private Collection<ISTVChannel> channelList = null;
	private HashMap<String, ISTVSection> sectionMap = new HashMap<String, ISTVSection>();
	private HashMap<Integer, ISTVItem>   itemMap = new HashMap<Integer, ISTVItem>();
	private HashMap<Integer, ISTVClip>   clipMap = new HashMap<Integer, ISTVClip>();
	private HashSet<Integer>  historySet = new HashSet<Integer>();
	private ArrayList<Integer>  historyArray = new ArrayList<Integer>();
	private boolean historyChanged=false;
	private HashSet<Integer>  bookmarkSet = new HashSet<Integer>();
	private ArrayList<Integer>  homeSet = new ArrayList<Integer>();
	private ArrayList<String> hotWords = new ArrayList<String>();
	private ISTVTopVideo topVideo;
	private boolean gotContentModel=false;
	private boolean gotChannelList=false;
	private boolean gotHistory=false;
	private boolean importHistory=false;
	private boolean gotBookmark=false;
	private boolean gotHome=false;
	private boolean gotTopVideo=false;
	private boolean gotHotWords=false;
	private boolean active=false;
	private boolean login=false;
	private static String accredit=null;

	private String accessToken=null;

	public synchronized boolean needLogin(){
		if(accessToken==null)
			return true;
		return false;
	}

	synchronized String getAccessToken(){
		return accessToken;
	}

	synchronized void setAccessToken(String token){
		accessToken = token;
	}
	
	public void setAccredit(String acc){
	    accredit=acc;
	    StoreAccreditThread accrdeitTh=new StoreAccreditThread();
	    accrdeitTh.start();
	}
	
	public String getAccredit(){
	    return accredit;
	}

	private boolean storeDataFlag=false;
	private StoreDataThread storeDataThread=null;

    private class StoreDataThread extends Thread {
        public void run() {

            while (true) {
                if (storeDataFlag) {
                    storeDataFlag = false;
                    ObjectOutputStream os = null;
                    int count = 0;

                    try {
                        File path = new File(HISTORY_FILE);
                        path.getParentFile().mkdirs();

                        os = new ObjectOutputStream(new FileOutputStream(
                                HISTORY_FILE));
                        Collection<ISTVItem> items = getHistoryList();

                        for (ISTVItem item : items) {
                            if (item != null && item.offset > 0) {
                                Log.d(TAG, "store item \"" + item.title
                                    + " \" " + item.offset + "\""
                                    + item.updateDate + "\""+" pk:"+item.pk+";itemPK="+item.itemPK);
                                ISTVItem witem = item.clone();
                                witem.attrs = null;
                                witem.description = null;
                                witem.subItems = null;
                                witem.gotDetail = false;
                                os.writeObject(witem);
                            }
                            count++;
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "store history file failed "
                            + e.getMessage());
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (Exception e) {
                            }
                        }
                    }

                    Log.d(TAG, "store history entry " + count);
                    try {
                        sleep(60000);
                    } catch (Exception e) {
                    }
                }
                else {
                    try {
                        sleep(10);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
    
    private class StoreAccreditThread extends Thread {
        public void run() {
                    ObjectOutputStream os = null;

                    try {
                        File path = new File(ACCREDIT_FILE);
                        path.getParentFile().mkdirs();

                        os = new ObjectOutputStream(new FileOutputStream(
                                ACCREDIT_FILE));
                        Log.d(TAG, "----StoreAccreditThread---accredit="+accredit);
                        DataSerial data=new DataSerial(accredit);
                        os.writeObject(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "store accredit file failed "+ e.getMessage());
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (Exception e) {
                            }
                        }
                    }
        }
    }

	private synchronized void startStoreDataThread(){
		if(storeDataThread==null){		
			storeDataFlag=true;
			storeDataThread=new StoreDataThread();
			storeDataThread.start();
		}else{
			storeDataFlag=true;
		}
	}

	public synchronized void storeHistory(){

		if(!historyChanged){
			Log.d(TAG, "!historyChanged   return");
			return;
		}
		startStoreDataThread();
		historyChanged = false;
	}

	void loadHistory(){
		ObjectInputStream is=null;
		ArrayList<ISTVItem> items=new ArrayList<ISTVItem>();

		try{
			ISTVItem item;

			is = new ObjectInputStream(new FileInputStream(HISTORY_FILE));
			int count = 0;
			do{
				count++;
				item = (ISTVItem)is.readObject();
				if(item!=null){
					item.bookmarked=false;
					item.gotDetail=false;
					item.attrs=null;
					items.add(item);
					Log.d(TAG, "---------load item \""+item.title+"\" "+item.offset+" \""+((item.updateDate!=null)?item.updateDate:"")+"\""+";pk="+item.pk+";itemPK="+item.itemPK);
				}
				Thread.sleep(10);
			}while(item!=null && count<100);//最多读出一百条
		}catch(Exception e){
		}finally{
			if(is!=null){
				try{
					is.close();
				}catch(Exception e){
				}
			}

			Log.d(TAG, "load history entry "+items.size());
			setHistoryList(items);
		}
	}
	
	void loadAccredit(){
	    Log.d(TAG, "   loadAccredit ");
	    ObjectInputStream is=null;
        try{
            DataSerial data=null;

            is = new ObjectInputStream(new FileInputStream(ACCREDIT_FILE));
            int count = 0;
            do{
                count++;
                data = (DataSerial) is.readObject();
                if(data!=null&&data.accredit!=null&&data.accredit.length()>12){
                    accredit=data.accredit;
                }
                Thread.sleep(10);
                Log.d(TAG, "-----loadAccredit--accredit="+accredit);
            }while(data!=null && count<1);//最多读出一条
        }catch(Exception e){
        }finally{
            if(is!=null){
                try{
                    is.close();
                }catch(Exception e){
                }
            }
            Log.d(TAG, "load Accredit over ");
        }
	}

	ISTVEpg(){
		loadHistory();
		loadAccredit();
	}

	synchronized void addContentModel(String lang, String name, String title, String aname, String atitle, String personAttrs){
		ISTVContentModel cm = cmMap.get(name);
		if(cm==null){
			cm = new ISTVContentModel();
			cm.name  = name;
			cm.personAttr = personAttrs;
			cmMap.put(name, cm);
		}
		cm.title.put(lang, title);
		
		ISTVContentModel.Attribute attr = cm.attrs.get(aname);
		if(attr==null){
			attr = cm.addAttr(aname);
		}
		attr.title.put(lang, atitle);
		gotContentModel=true;
	}

	synchronized boolean hasGotContentModel(){
		return gotContentModel;
	}

	public synchronized ISTVContentModel getContentModel(String name){
		ISTVContentModel cm = cmMap.get(name);
		return cm;
	}

	public synchronized ISTVContentModel.Attribute getContentModelAttr(String name, String aname){
		ISTVContentModel cm = cmMap.get(name);
		if(cm==null)
			return null;

		ISTVContentModel.Attribute attr = cm.attrs.get(aname);
		return attr;
	}

	synchronized void setActiveStatus(boolean status){
		if(active!=status){
			active = status;
		}
	}

	public synchronized boolean getActiveStatus(){
		return active;
	}

	synchronized void setLoginStatus(boolean status){
		if(login!=status){
			login = status;
		}
	}

	public synchronized boolean getLoginStatus(){
		return login;
	}

	synchronized void setVodHomeList(Collection<ISTVItem> items){
		homeSet.clear();
		for(ISTVItem i : items){
			ISTVItem old;
			old = itemMap.put(i.pk, i);
			if(old!=null){
				i.mergeInfo(old);
			}
			homeSet.add(i.pk);
		}
		gotHome = true;
	}

	public synchronized Collection<ISTVItem> getVodHomeList(){
		if(gotHome){
			ArrayList<ISTVItem> items = new ArrayList<ISTVItem>();
			for(Integer id : homeSet){
				ISTVItem item = itemMap.get(id);
				if(item!=null){
					items.add(item);
				}
			}

			return items;
		}

		return null;
	}

	synchronized void setTopVideo(ISTVTopVideo tv){
		topVideo = tv;
		gotTopVideo = true;
	}

	public synchronized ISTVTopVideo getTopVideo(){
		return topVideo;
	}


	synchronized void setChannelList(Collection<ISTVChannel> channels){
		if(channels!=null){
			channelMap.clear();

			for(ISTVChannel ch : channels){
				channelMap.put(ch.channel, ch);
			}

			channelList = channels;
			gotChannelList = true;
		}
	}

	public synchronized Collection<ISTVChannel> getChannelList(){
		if(gotChannelList){
			return channelList;
		}

		return null;
	}

	synchronized void addSectionList(String chanID, Collection<ISTVSection> sections){
		ISTVChannel ch = channelMap.get(chanID);

		if((ch!=null) && !ch.gotSectionList){
			for(ISTVSection s : sections){
				sectionMap.put(s.slug, s);
			}

			ch.setSectionList(sections);
		}
	}

	public synchronized Collection<ISTVSection> getSectionList(String chanID){
		ISTVChannel ch = channelMap.get(chanID);
		ArrayList<ISTVSection> sections = new ArrayList<ISTVSection>();

		if((ch==null) || !ch.gotSectionList){
			return null;
		}

		for(String slug : ch.sections){
			ISTVSection sec = sectionMap.get(slug);
			if(sec!=null)
				sections.add(sec);
		}

		return sections;
	}

	public synchronized int getItemCount(String secID){
		ISTVSection sec = sectionMap.get(secID);

		if((sec!=null) && sec.gotPageCount)
			return sec.count;

		return -1;
	}

	synchronized void addItem(ISTVItem item){
		ISTVItem old;

		old = itemMap.put(item.pk, item);
		if(old!=null){
			item.mergeInfo(old);
		}
	}

	synchronized void addItemList(Collection<ISTVItem> items){
		for(ISTVItem i : items){
			ISTVItem old;

			old = itemMap.put(i.pk, i);
			if(old!=null){
				i.mergeInfo(old);
			}
		}
	}

	synchronized void addItemList(String secID, int count, int page, Collection<ISTVItem> items){
		ISTVSection sec = sectionMap.get(secID);

		if((sec!=null) && !sec.gotPage(page)){

			for(ISTVItem i : items){
				ISTVItem old;

				old = itemMap.put(i.pk, i);
				if(old!=null){
					i.mergeInfo(old);
				}
			}

			sec.addPage(count, page, items);
		}
	}

	public synchronized ISTVItem getItem(int pk){
		ISTVItem item = itemMap.get(pk);

		return item;
	}

	public synchronized Collection<ISTVItem> getItemList(int pks[]){
		ArrayList<ISTVItem> items = new ArrayList<ISTVItem>();

		for(int i : pks){
			ISTVItem item = itemMap.get(i);
			if(item!=null)
				items.add(item);
		}

		return items;
	}

	public synchronized Collection<ISTVItem> getItemList(String secID, int page){
		ISTVSection sec = sectionMap.get(secID);
		ArrayList<ISTVItem> items;

		if((sec==null) || !sec.gotPageCount || !sec.gotPage(page))
			return null;

		items = new ArrayList<ISTVItem>();

		int start = page * sec.countPerPage;
		int end   = start + sec.countPerPage;
		if(end > sec.items.length)
			end = sec.items.length;
		int i = start;

		while(i<end){
			if(sec.items[i]!=-1){
				ISTVItem item = itemMap.get(sec.items[i]);
				items.add(item);
			}
			i++;
		}

		return items;
	}

	synchronized void addItemDetail(ISTVItem item){
		ISTVItem old;

		item.setGotDetail();

		old = itemMap.put(item.pk, item);
		if(old!=null){
			item.mergeInfo(old);
		}
	}

	synchronized void addClip(ISTVClip clip){
		clipMap.put(clip.pk, clip);
	}

	public synchronized ISTVClip getClip(int pk){
		return clipMap.get(pk);
	}

	synchronized void addHotWords(Collection<String> words){
		hotWords.clear();
		for(String w : words){
			hotWords.add(w);
		}
		gotHotWords = true;
	}

	public synchronized Collection<String> getHotWords(){
		if(!gotHotWords)
			return null;

		return (Collection<String>)hotWords.clone();
	}

	public void removeHistoryPK(int pk){
	    Integer e = new Integer(pk);
	    Log.d(TAG, "begin remove history pk "+pk+";historySet.contains="+historySet.contains(e));
		if(historySet.remove(e)){
		    historyArray.remove(e);
            Log.d(TAG, "historySet remove history pk "+pk);
		}
		if(historyArray.contains(e)){
		    historyArray.remove(e);
		    historySet.remove(e);
            Log.d(TAG, "historyArray remove history pk "+pk);
		}
		historyChanged=true;
	}

	private void addHistoryPK(int pk, boolean update){
		if(update){
			historyArray.add(pk);
		}else{
			historyArray.add(historyArray.size(), pk);
		}

		historySet.add(pk);

		Log.d(TAG, "add history pk "+pk+";itemPK="+itemMap.get(pk).itemPK);

		clearOldHistory();
		historyChanged=true;
	}

	private void addHistoryPK(int pk){
		addHistoryPK(pk, true);
	}

	private void clearOldHistory(){
		while(historySet.size()>HISTORY_COUNT){
			Integer pk = historyArray.remove(0);
			historySet.remove(pk);
			Log.d(TAG, "remove old history pk "+pk);
		}
	}

	synchronized void setHistoryList(Collection<ISTVItem> items){
		for(ISTVItem i : items){
			ISTVItem old = itemMap.get(i.pk);

			if(old==null){
				itemMap.put(i.pk, i);
				old = i;
			}else{
				old.mergeInfo(i);
			}

			addHistory(old, old.offset, false);
		}
	}

	public synchronized boolean hasGotServerHistoryList(){
		return gotHistory;
	}

	public synchronized boolean hasImportHistoryList(){
		return importHistory;
	}

	public synchronized void setImportHistoryList(){
		importHistory=true;
	}

	public synchronized Collection<ISTVItem> getHistoryList(){
	    Log.d(TAG, "-------getHistoryList---------");
		ArrayList<ISTVItem> items = new ArrayList<ISTVItem>();

		for(int i : historySet){//128712
		    Log.d(TAG, "---------->>>>>>>>>>>>>>>>>>>i="+i);
			ISTVItem item = itemMap.get(i);
			if(item!=null){
				items.add(item);
				SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Log.d(TAG, "get history: "+item.title +" | time="+df.format(item.updateDate)+" | pk="+item.pk+" | itemPK="+item.itemPK);
			}
		}

		return items;
	}

	synchronized void setHistoryListFromServer(Collection<ISTVItem> items){
		if(gotHistory)
			return;

		Log.d(TAG, "add history list from server count "+items.size());

		setHistoryList(items);

		historyChanged = true;
		gotHistory = true;
	}

	public synchronized int getOffset(int itemPK, int subItemPK){
		ISTVItem item;

		if((itemPK==-1) && (subItemPK==-1))
			return 0;

		if(subItemPK!=-1){
			item = itemMap.get(subItemPK);
		}else{
			item = itemMap.get(itemPK);
		}

		if(item==null)
			return 0;

		Log.d(TAG, "get offset "+item.pk+" "+item.offset);

		return item.offset;
	}

	public synchronized boolean addHistory(int itemPK, int subItemPK, int offset){
		return addHistory(itemPK, subItemPK, offset, true);
	}

	public synchronized boolean addHistory(int itemPK, int subItemPK, int offset, boolean update){
		ISTVItem item, old;
		boolean ret = false;

		Log.d(TAG, "add history itemPK "+itemPK+" subItemPK "+subItemPK+" offset "+offset);

		if((itemPK==-1) && (subItemPK==-1))
			return false;

		if(subItemPK!=-1){
            item = itemMap.get(subItemPK);
            if (item == null)
                return false;
            if (itemPK != -1) {
                item.itemPK = itemPK;
            }
            item.isSubItem = true;
        } else {
            item = itemMap.get(itemPK);
            if (item == null)
                return false;
		}

		item.updateDate = new Date(System.currentTimeMillis());
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Log.d(TAG, "set history: "+item.title +" |"+df.format(item.updateDate));

		return addHistory(item, offset, update);
	}

	public synchronized boolean addHistory(ISTVItem item, int offset){
		return addHistory(item, offset, true);
	}

	public synchronized boolean addHistory(ISTVItem item, int offset, boolean update){

		if(item.isSubItem){
			boolean remove = false;
			ISTVItem old = null;

			for(int pk : historySet){
				old = getItem(pk);
				if(old!=null && old.isSubItem && old.itemPK==item.itemPK){
					//当old.pk大时，不return false bug:0006047
					if(old.pk>item.pk){
						Log.d(TAG, "episode history do not need to be added pk:"+item.pk+" old:"+old.pk);
//						return false;
					}else 
					if(old.pk==item.pk){
						if((offset==-1)||(item.offset!=-1)){// && offset>item.offset)){
							Log.d(TAG, "episode history update pk:"+item.pk);
							item.offset = offset;
							historyChanged = true;
							return true;
						}else{
							Log.d(TAG, "episode history do not need update pk:"+item.pk);
							return false;
						}
					}else{
						Log.d(TAG, "episode history remove old pk:"+item.pk+" old:"+old.pk);
						remove = true;
						break;
					}
				}
			}

			if(remove && old!=null){
				removeHistoryPK(old.pk);
			}
			
		}

		item.offset = offset;
		addHistoryPK(item.pk, update);
		return true;
	}

	public synchronized void clearHistory(){
		Log.d(TAG, "---clearHistory---");
		for(Integer pk : historySet){
			Log.d(TAG, "---pk---:"+pk);
			ISTVItem item = getItem(pk);
			if(item!=null){
				item.offset = 0;
				if(item.isSubItem){
					ISTVItem pitem = getItem(item.itemPK);
					if(pitem!=null && pitem.subItems!=null){
						for(int spk : pitem.subItems){
							ISTVItem citem = getItem(spk);
							if(citem!=null){
								citem.offset = 0;
							}
						}
					}
				}
			}
		}

		historySet.clear();
		historyArray.clear();
		Log.d(TAG, "---historySet.clear()---"+historySet.size());
		Log.d(TAG, "---historyArray.clear()---"+historyArray.size());
		File path = new File(HISTORY_FILE);
		path.delete();
		
		historyChanged = true;
	}

	public synchronized boolean hasGotBookmarkList(){
		return gotBookmark;
	}
	
	public synchronized void setGotBookmarkList(Boolean bo){
		gotBookmark=bo;
		if(bo=false){
			bookmarkSet.clear();
			Log.d(TAG, "---------bookmarkSet.clear");
		}
	}

	synchronized void setBookmarkList(Collection<ISTVItem> items){
		bookmarkSet.clear();

		for(ISTVItem i : items){
			ISTVItem old = itemMap.get(i.pk);
			if(old!=null){
				old.bookmarked = true;
				if(old.contentModel==null||old.contentModel.equals("")){
				    old.contentModel=i.contentModel;
				}
			}else{
				i.bookmarked = true;
				itemMap.put(i.pk, i);
			}
			bookmarkSet.add(i.pk);
		}

		gotBookmark = true;
	}

	public synchronized Collection<ISTVItem> getBookmarkList(){
		ArrayList<ISTVItem> items;

		items = new ArrayList<ISTVItem>();
		for(int i : bookmarkSet){
			ISTVItem item = itemMap.get(i);
			if(item!=null)
				items.add(item);
		}

		return items;
	}

	public synchronized boolean getBookmarkFlag(int pk){
		ISTVItem item = itemMap.get(pk);
		if(item!=null){
			return item.bookmarked;
		}
		return false;
	}

	public synchronized void addBookmark(int pk){
		ISTVItem item = itemMap.get(pk);
		if(item!=null){
			item.bookmarked = true;
		}
		bookmarkSet.add(pk);
	}

	public synchronized void removeBookmark(int pk){
		ISTVItem item = itemMap.get(pk);
		if(item!=null){
			item.bookmarked = false;
		}
		bookmarkSet.remove(pk);
	}

	public synchronized void clearBookmark(){
		for(int i : bookmarkSet){
			ISTVItem item = itemMap.get(i);
			if(item!=null){
				item.bookmarked = false;
			}
		}
		bookmarkSet.clear();
	}
}

