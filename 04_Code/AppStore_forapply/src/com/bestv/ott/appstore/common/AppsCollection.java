package com.bestv.ott.appstore.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.bestv.ott.appstore.utils.AppLog;

/**
 * 每种应用最多装载5页(40个),首页上的显示从服务器上获取,当过了3页后，要重新加载和删除
 * 我的应用是从本地数据库上获取
 * @author Administrator
 *
 */

public class AppsCollection {
    
    private static final String TAG="com.joysee.appstore.common.AppsInfoMap";
    
    private int page_num=Constants.MAX_LENGTH;//默认每一页装的数据,可以改变
    
    /*false当插入时，不能改变appPageMap对应的值(服务器传过来的页数);true当插入时，改变appPageMap中对应的值*/
    private boolean page_flag=true;
    
    /*按类型，装所有的应用*/
    private Map<Integer, List<ApplicationBean>> appsMap=new TreeMap<Integer,List<ApplicationBean>>();
    
    /*按类型，装应用的页数据*/
    private Map<Integer,Integer> appPageMap=new TreeMap<Integer,Integer>();

    
    public AppsCollection(){
    }
    /**
     * 添加一个key对应的应用
     * @param key
     * @param app
     */
    public void addOneAppByKey(Integer key,ApplicationBean app){
        List<ApplicationBean> list=appsMap.get(key);
        if(null==list){
            List<ApplicationBean> arrayList=new ArrayList<ApplicationBean>();
            arrayList.add(app);
            list=arrayList;
            appsMap.put(key, arrayList);
        }else{
            list.add(app);
        }
        putAppPageMapByKey(key,list);
    }
    
    /**
     * 删除一个key对应的应用
     * @param key
     * @param app
     */
    public void deleteOneAppByKey(Integer key,ApplicationBean app){
        List<ApplicationBean> list=appsMap.get(key);
        if(null==list){
            AppLog.d(TAG, "list is null");
            return;
        }else{
            list.remove(app);
        }
        putAppPageMapByKey(key,list);
    }
    
    /**
     * 添加应用，并根据每页数量计数页数,用默认的(每页8个)
     * @param key
     * @param list
     * @param page_num
     */
    public void putAppsListByKey(Integer key,List<ApplicationBean> list){
        appsMap.put(key, list);
        putAppPageMapByKey(key,list);
    }
    
    /**
     * 设置key对应的页数,当page_flag为false时，不能改变appPageMap的值
     * @param key
     * @param list
     * @param page_num
     */
    private void putAppPageMapByKey(Integer key,List<ApplicationBean> list){
        if(!page_flag){
            return;
        }
        int allPage;
        if(null==list){
            allPage=0;
        }else{
            allPage=list.size()/page_num+(list.size()%page_num==0?0:1);
        }
        appPageMap.put(key, allPage);
    }
    /**
     * 设置页数
     * @param key
     * @param num
     */
    public void putAppPageMapByKeyAndNum(Integer key,Integer num){
        appPageMap.put(key, num);
    }

    /**
     * 获取某一页下所有数据
     * @param key
     * @param currentPage
     * @return
     */
    public List<ApplicationBean> getAppsListByKey(Integer key,int currentPage) {
        List<ApplicationBean> temp = (List<ApplicationBean>)appsMap.get(key);
        if(null==temp||temp.size()<1){
            AppLog.d(TAG, "list is null or size < 1");
            return null;
        }
        int allPage=appPageMap.get(key);
        if(currentPage<1||currentPage>allPage){
            AppLog.d(TAG, "currentPage is error");
            return null;
        }
        int start=(currentPage-1)*8;
        int end;
        if(allPage==1||currentPage==allPage){
            end=temp.size();
        }else{
            end=(currentPage)*8;
        }
        return temp.subList(start, end);
    }
    /**
     * 获取key下所有的应用
     * @param key
     * @return
     */
    public List<ApplicationBean> getAppsListByKey(Integer key) {
        List<ApplicationBean> temp = (List<ApplicationBean>)appsMap.get(key);
        if(null==temp||temp.size()<1){
            AppLog.d(TAG, "currentPage is error");
            return null;
        }
        return temp;
    }
    /**
     * 如果没找到，返回-1
     * @param type
     * @return
     */
    public int getAppPageByKey(int key){
        Integer temp=(Integer)appPageMap.get(key);
        if(null==temp){
            AppLog.d(TAG, " null ");
            return -1;
        }
        return temp;
    }
    
    public int getPage_num() {
        return this.page_num;
    }
    
    public void setPage_num(int pageNum) {
        this.page_num = pageNum;
    }
    public boolean isPage_flag() {
        return page_flag;
    }
    public void setPage_flag(boolean pageFlag) {
        page_flag = pageFlag;
    }
}
