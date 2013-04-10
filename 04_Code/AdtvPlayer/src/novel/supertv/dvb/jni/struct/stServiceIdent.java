package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 节目标示号。
 * 任何一个节目，均是由三个ID结合起来作为一个节目的唯一标志。
 * 这三个ID分别是service id、transponder id、original network id
 * 
 */
public class stServiceIdent implements Comparable<stServiceIdent> ,Parcelable{
    public int ServiceId = -1;
    public int TsId = -1;
    public int OrgNetId = -1;
    
    /**
     * 比较，用于排序，
     */
    public int compareTo(stServiceIdent another) {
        if(ServiceId != another.ServiceId)
            return ServiceId - another.ServiceId;
        if(TsId != another.TsId)
            return TsId - another.TsId;
        if(OrgNetId != another.OrgNetId)
            return OrgNetId - another.OrgNetId;
        return 0;
    }
    
    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 17;
        result = prime * result +  getServiceId();
        result = prime * result + getTsId();
        result = prime * result + getOrgNetId();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && ((obj instanceof stServiceIdent) == true)
                && (ServiceId == ((stServiceIdent)obj).ServiceId)
                && (TsId == ((stServiceIdent)obj).TsId)
                && (OrgNetId == ((stServiceIdent)obj).OrgNetId);
    }
    
    @Override
    public String toString() {
        return "[ServiceId=" + ServiceId + ", TsId=" + TsId + ", OrgNetId=" + OrgNetId + "]";
    }
    
    public stServiceIdent(){    
    }

    public stServiceIdent(int tServiceId, int tTsId,int tOrgNetId){
        ServiceId=tServiceId;
        TsId=tTsId;
        OrgNetId=tOrgNetId;
    }

    public stServiceIdent(Parcel in){
        setServiceId(in.readInt());
        setTsId(in.readInt());
        setOrgNetId(in.readInt());
    }
    
    public static final Parcelable.Creator<stServiceIdent> CREATOR
        = new Parcelable.Creator<stServiceIdent>() {
        public stServiceIdent createFromParcel(Parcel in) {
            return new stServiceIdent(in);
        }
        
        public stServiceIdent[] newArray(int size) {
            return new stServiceIdent[size];
        }
    };
    
    public static stServiceIdent getObjFromString(String strDesc) {
        if (strDesc == null || 0 == strDesc.length()) {
            return null;
        } else {
            String[] strArray = strDesc.split("[^0-9]+");
            if (strArray == null || strArray.length < 3) {
                return null;
            }
            
            int ServiceId = -1, TsId = -1, OrgNetId = -1;
            for (String str : strArray) {
                if (str != null && str.length() != 0) {
                    try{
                        int intVal = Integer.parseInt(str);
                        
                        if (ServiceId == -1) {
                            ServiceId = intVal;
                        }
                        else if (TsId == -1) {
                            TsId = intVal;
                        }
                        else if (OrgNetId == -1) {
                            OrgNetId = intVal;
                            break;
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                        break;
                    }
                }
            }
            
            if (ServiceId == -1 || TsId == -1 || OrgNetId == -1) {
                return null;
            } else {
                return new stServiceIdent(ServiceId, TsId, OrgNetId);
            }
        }
    }
    
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ServiceId);
        dest.writeInt(TsId);
        dest.writeInt(OrgNetId);
    }
    
    public int getServiceId() {
        return ServiceId;
    }
    public void setServiceId(int serviceId) {
        ServiceId = serviceId;
    }
    public int getTsId() {
        return TsId;
    }
    public void setTsId(int tsId) {
        TsId = tsId;
    }
    public int getOrgNetId() {
        return OrgNetId;
    }
    public void setOrgNetId(int orgNetId) {
        OrgNetId = orgNetId;
    }
    
}
