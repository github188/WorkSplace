package com.bestv.ott.appstore.common;

import com.bestv.ott.appstore.utils.AppLog;

/**
 * 分页bean
 * @author Administrator
 *
 */
public class PageBean {
    
    public static final String TAG = "com.joysee.appstore.home.PageBean";
    
    private int pageTag;//标签
    private int pageNo=1;//当前页数,默认为1
    private int pageSize=8;//每页显示数量,默认为8
    private int pageTotal;//总页数
    private int totalSize;//总个数
    private String keyWord ; //搜索关键字
    private int typeId=-1;//种类，-1表示不要
    
    public boolean nextPage(){
        AppLog.d("pageBean", "pageNo="+pageNo+";pageTotal="+pageTotal);
        if(pageNo==pageTotal){
            return false;
        }
        pageNo++;
        return true;
    }
    
    public boolean previousPage(){
        if(pageNo==1||pageTotal==0){
            return false;
        }
        pageNo--;
        return true; 
    }
    
    public String getKeyWord(){
    	return keyWord;
    }
    
    public void setKeyWord(String keyWord){
    	this.keyWord = keyWord;
    }
    public int getPageTag() {
        return pageTag;
    }
    public void setPageTag(int pageTag) {
        this.pageTag = pageTag;
    }
    public int getPageNo() {
        return pageNo;
    }
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(int pageTotal) {
        this.pageTotal = pageTotal;
    }
    
    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
    
    
    public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	/**
     * 通过数据总个数，计算总页数
     * @param size
     * @return
     */
    public void calculatePageTotal(int size){
        this.totalSize=size;
        this.pageTotal=size/pageSize+(size%pageSize==0?0:1);
    }
    
    public String toString(){
        String to="PageBean:[pageNo="+pageNo+";pageSize="+pageSize+";pageTotal="+pageTotal+";totalSize="+totalSize+"]";
        return to;
    }


}
