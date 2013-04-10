package com.bestv.ott.appstore.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 * @Types   role : 应用大礼包
 *  
 * @Package name : com.joysee.webapp.entity.vo
 * @Project name : BtopInterface
 * @author  name : Andy King
 * @email   addr : wkw11@163.com
 * @Create  time : Aug 23, 2012 : 1:41:24 PM
 * @ver     curr : 1.0
 * </pre>
 */
public class AppPackageBean implements Parcelable {
	private static final long serialVersionUID = 1L;
	private String appPackageId; // 2012-08-24 大礼包ID
	private String name; // 2012-08-24 大礼包名称
	private Double originalPrice; // 2012-08-22 原价
	private String remark; // 2012-08-23 描述
	private Double promotionPrice; // 2012-08-20 促销价格preferentialPrice
	private String appPackageProductCode;

    // 2012-08-24 添加大礼包产品码
	private String image; // 2012-08-24 大礼包图片
	private String nativeImage;

	public String getNativeImage() {
        return nativeImage;
    }

    public void setNativeImage(String nativeImage) {
        this.nativeImage = nativeImage;
    }

    private AppsBean[] appsBean ;
	
	private int pageNo = 1;
	private int totalPages = 1 ;

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	// 2012-08-23 包中应用
	public AppPackageBean() {
		super();
	}

	public AppPackageBean(Parcel in) {
		super(); 
		this.setAppPackageId(in.readString());
		this.setName(in.readString());
		this.setOriginalPrice(in.readDouble());
		this.setRemark(in.readString());
		this.setPromotionPrice(in.readDouble());
		this.setAppPackageProductCode(in.readString());
		this.setImage(in.readString());
		this.setNativeImage(in.readString());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(appPackageId  ); //  appPackageId;
		dest.writeString(name  ); //  name;
		dest.writeDouble(originalPrice  ); //  originalPrice;
		dest.writeString(remark  ); //  remark;
		dest.writeDouble(promotionPrice  ); //  promotionPrice;
		dest.writeString(appPackageProductCode  ); //  appPackageProductCode;
		dest.writeString(image  ); //  image; 
		dest.writeString(nativeImage  );
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<AppPackageBean> CREATOR = new Parcelable.Creator<AppPackageBean>() {
		public AppPackageBean createFromParcel(Parcel in) {
			return new AppPackageBean(in);
		}

		public AppPackageBean[] newArray(int size) {
			return new AppPackageBean[size];
		}
	};

	public Double getOriginalPrice() {
		return originalPrice;
	}

	public void setOriginalPrice(Double originalPrice) {
		this.originalPrice = originalPrice;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Double getPromotionPrice() {
		return promotionPrice;
	}

	public void setPromotionPrice(Double promotionPrice) {
		this.promotionPrice = promotionPrice;
	}

	public String getAppPackageProductCode() {
		return appPackageProductCode;
	}

	public void setAppPackageProductCode(String appPackageProductCode) {
		this.appPackageProductCode = appPackageProductCode;
	}

	public String getAppPackageId() {
		return appPackageId;
	}

	public void setAppPackageId(String appPackageId) {
		this.appPackageId = appPackageId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}


	public AppsBean[] getAppsBean() {
		return appsBean;
	}

	public void setAppsBean(AppsBean[] appsBean) {
		this.appsBean = appsBean;
	}
	
}