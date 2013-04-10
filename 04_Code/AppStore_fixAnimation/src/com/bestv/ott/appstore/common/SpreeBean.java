package com.bestv.ott.appstore.common;

public class SpreeBean {
	
	private int apppackageid;
	private String apppackageproductcode;
	private String apppage;
	private String serImageUrl;
	private String natImageUrl;
	public String getSerImageUrl() {
		return serImageUrl;
	}
	public void setSerImageUrl(String serImageUrl) {
		this.serImageUrl = serImageUrl;
	}
	public String getNatImageUrl() {
		return natImageUrl;
	}
	public void setNatImageUrl(String natImageUrl) {
		this.natImageUrl = natImageUrl;
	}
	private String name;
	private String originalprice;
	private String promotionprice;
	private String remark;
	
	
	public int getApppackageid() {
		return apppackageid;
	}
	public void setApppackageid(int apppackageid) {
		this.apppackageid = apppackageid;
	}
	public String getApppackageproductcode() {
		return apppackageproductcode;
	}
	public void setApppackageproductcode(String apppackageproductcode) {
		this.apppackageproductcode = apppackageproductcode;
	}
	public String getApppage() {
		return apppage;
	}
	public void setApppage(String apppage) {
		this.apppage = apppage;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOriginalprice() {
		return originalprice;
	}
	public void setOriginalprice(String originalprice) {
		this.originalprice = originalprice;
	}
	public String getPromotionprice() {
		return promotionprice;
	}
	public void setPromotionprice(String promotionprice) {
		this.promotionprice = promotionprice;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	
	
}
