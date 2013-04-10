package com.bestv.ott.appstore.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.bestv.ott.appstore.utils.FileUtils;

/**
 * 从服务器获取的数据保存到此对象中
 * @author Administrator
 *
 */
public class AppsBean implements Parcelable{

    /*目前有些字段是没有用到*/
    private int ID;//服务器端对应的ID
    private String appName;
    private String pkgName;
    private int typeID;
    private String version;
    private String remark;
    private String natImageUrl;  //本地图片路径
    private String serImageUrl; //远程图片路径
    private String apkUrl;     //远程apk路径
    private String createTime;
    private int size;
    private String typeName; 
    private String score; 
    private String downloadDir ;
    private int voteNum;//
    private String versionName;
    private String contentProvider;
    private double price;

    public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	private boolean isOrdered = false ;
    
	/*
	 * 应用支付(应用，大礼包)
	 */
	private double promotionPrice = 0; // 2012-08-20 促销价格preferentialPrice
	//private Date prefStartTime; // 2012-08-20 优惠开始时间
	//private Date prefEndTime; // 2012-08-20 优惠结束时间
	//private String itemCode; // 2012-08-20 itemCode = packageName+versionCode
	private String appProductCode; // 2012-08-20 应用产品码

	// 大礼包请请求
	//private String action; // 2012-08-20 去除請求大礼包的action
	//private String bgpId; // 2012-08-20 大礼包(Big gift package)
	private String appPackageProductCode;
	// 2012-08-22 大礼包产品码，用于client比较是否定购
	/*
	 * 添加精品，外设字段信息
	 * wzAsAppTab.setAppType((Integer) values[71]);
	 * wzAsAppTab.setExternalDevicetypeIds((String) values[72]);
	 */
	private boolean boutique; // 2012-08-22 精品ture,非精品false;
	private String peripherals; // 2012-08-22 外设（1,2,3,4）

	/*
	 * 原价
	 */
	private double originalPrice = 0; // 2012-08-22 原价

	private String appPackageId; // 2012-08-23 添加大礼包id,用这个字段访问大礼包详情  
	
	private String serviceCode;
	
	public AppsBean(){
    	
    }
	
    public AppsBean(Parcel in){
    	setID(in.readInt());
    	setAppName(in.readString());
    	setPkgName(in.readString());
    	setTypeID(in.readInt());
    	setVersion(in.readString());
    	setRemark(in.readString());
    	setNatImageUrl(in.readString());
    	setSerImageUrl(in.readString());
    	setApkUrl(in.readString());
    	setCreateTime(in.readString());
    	setSize(in.readInt());
    	setTypeName(in.readString()); 
    	setScore(in.readString());
    	setDownloadDir(in.readString());
    	setVoteNum(in.readInt());
    	setVersionName(in.readString());
    	
		this.setPromotionPrice(in.readDouble());
		this.setAppProductCode(in.readString());
		this.setAppPackageProductCode(in.readString());
		this.setBoutique(in.readInt()==1);
		this.setPeripherals(in.readString());
		this.setOriginalPrice(in.readDouble());
		this.setAppPackageId(in.readString());
		this.setServiceCode(in.readString());
    }
    

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ID);
		dest.writeString(appName);
		dest.writeString(pkgName);
		dest.writeInt(typeID);
		dest.writeString(version);
		dest.writeString(remark);
		dest.writeString(natImageUrl);
		dest.writeString(serImageUrl);
		dest.writeString(apkUrl);
		dest.writeString(createTime);
		dest.writeInt(size);
		dest.writeString(typeName); 
		dest.writeString(score);
		dest.writeString(downloadDir);
		dest.writeInt(voteNum);
		dest.writeString(versionName);
	    
		dest.writeDouble(promotionPrice);
		dest.writeString(appProductCode);
		dest.writeString(appPackageProductCode);
		dest.writeInt(boutique?1:0);
		dest.writeString(peripherals);
		dest.writeDouble(originalPrice);
		dest.writeString(appPackageId);
	    dest.writeString(serviceCode);
	}
    
	public static final Parcelable.Creator<AppsBean> CREATOR
    = new Parcelable.Creator<AppsBean>() {
        public AppsBean createFromParcel(Parcel in) {
            return new AppsBean(in);
        }

        public AppsBean[] newArray(int size) {
            return new AppsBean[size];
        }
    };
    
    public String getAppName() {
        return appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public String getPkgName() {
        return pkgName;
    }
    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public int getID() {
        return ID;
    }
    public void setID(int iD) {
        ID = iD;
    }
    public String getNatImageUrl() {
        return natImageUrl;
    }
    public void setNatImageUrl(String natImageUrl) {
        this.natImageUrl = natImageUrl;
    }
    public String getSerImageUrl() {
        return serImageUrl;
    }
    public void setSerImageUrl(String serImageUrl) {
        this.serImageUrl = serImageUrl;
    }
    public String getApkUrl() {
        return apkUrl;
    }
    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public int getTypeID() {
        return typeID;
    }
    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }
    
    public void deleteImage(){
        FileUtils.delete(natImageUrl);
    }
    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getDownloadDir() {
		return downloadDir;
	}
	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}
	
	
	public int getVoteNum() {
		return voteNum;
	}
	public void setVoteNum(int voteNum) {
		this.voteNum = voteNum;
	}
	
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	
    public double getPromotionPrice() {
		return promotionPrice;
	}
	public void setPromotionPrice(double promotionPrice) {
		this.promotionPrice = promotionPrice;
	}
	public String getAppProductCode() {
		return appProductCode;
	}
	public void setAppProductCode(String appProductCode) {
		this.appProductCode = appProductCode;
	}
	public String getAppPackageProductCode() {
		return appPackageProductCode;
	}
	public void setAppPackageProductCode(String appPackageProductCode) {
		this.appPackageProductCode = appPackageProductCode;
	}
	public boolean isBoutique() {
		return boutique;
	}
	public void setBoutique(boolean boutique) {
		this.boutique = boutique;
	}
	public String getPeripherals() {
		return peripherals;
	}
	public void setPeripherals(String peripherals) {
		this.peripherals = peripherals;
	}
	public double getOriginalPrice() {
		return originalPrice;
	}
	public void setOriginalPrice(double originalPrice) {
		this.originalPrice = originalPrice;
	}
	public String getAppPackageId() {
		return appPackageId;
	}
	public void setAppPackageId(String appPackageId) {
		this.appPackageId = appPackageId;
	}
	public String getContentProvider() {
		return contentProvider;
	}
	public void setContentProvider(String contentProvider) {
		this.contentProvider = contentProvider;
	}

	public boolean isOrdered() {
		return isOrdered;
	}

	public void setOrdered(boolean isOrdered) {
		this.isOrdered = isOrdered;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	
}
