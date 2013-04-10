
package com.joysee.adtv.logic;
import com.joysee.adtv.logic.bean.EmailContent;
import com.joysee.adtv.logic.bean.EmailHead;
import com.joysee.adtv.logic.bean.LicenseInfo;
import com.joysee.adtv.logic.bean.WatchTime;

import java.util.ArrayList;
import java.util.Vector;
/**
 * 设置相关JNI及其逻辑控制
 * @author wuhao
 *
 */
public class CaManager {
    private CaManager() {
    }

    private static CaManager mCaManager = new CaManager();
    
    public static CaManager getCaManager() {
        return mCaManager;
    }
    /**
     * 修改密码
     * @param oldPwd
     * @param newPwd
     * @return 0 操作成功 1 未知错误 3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
    public native int nativeChangePinCode(String oldPwd, String newPwd);
    
    /**
     * 获取观看等级
     * @return 等级
     */
    public native int nativeGetWatchLevel();
    
    /**
     * 设置观看等级
     * @param psd pin密码
     * @param level
     * @return 0 操作成功 1 未知错误 3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
    public native int nativeSetWatchLevel(String psd, int level);
    
    /**
     * 设置工作时段 时分秒 需密码
     * @param pwd pin密码
     * @param watchTime 观看事件bean类
     * @return 0 操作成功 1 未知错误 3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
    public native int nativeSetWatchTime(String pwd,WatchTime watchTime);
    
    /**
     * 获取工作时段
     * @param tagWatchTime
     * @return >=0 成功
     */
    public native int nativeGetWatchTime(WatchTime watchTime);
    
    /**
     * 获取授权信息列表
     * 
     * @param operID 运营商ID
     * @param Vector tagEntitle
     * @return 0 操作成功 1 未知错误 3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
     public native int nativeGetAuthorization(int operID,Vector<LicenseInfo> vec);
    /** 
     * 获取卡序列号，成功返回序号字符串，失败返回空字符串 
     */
    public native String nativeGetCardSN();
    
    /**
     * 获取运营商ID
     * @param vector
     * @return >=0 成功
     */
    public native int nativeGetOperatorID(Vector<Integer> vector);
    
    /**
     * 参数邮件头列表.
     * @return >=0成功
     */
     public native int nativeGetEmailHeads(ArrayList<EmailHead> emailHeadList);
    
    /**
     * @param id 邮件ID
     * @param head 邮件头
     * @return >=0成功
     */
     public native int nativeGetEmailHead(int id, EmailHead head);
    
    /**
     * @param id 邮件ID
     * @param content 邮件内容
     * @return >=0成功
     */
     public native int nativeGetEmailContent(int id, EmailContent content);
    
    /**
     * @param id 删除邮件
     * @return >=0成功
     */
    public native int nativeDelEmail(int id);
    
    /**
     * 查看邮件空间
     * @return 可用空间
     */
    public native int nativeGetEmailIdleSpace();
    
    /**
     * 得到当前邮件数量
     * @return 当前邮件总数
     */
    public native int nativeGetEmailUsedSpace();
    /**
     * 获取STBID
     * @return 
     */
    public native String nativeGetSTBID();
    /**
     * 获取运营商用户信息
     * @param operatorID  运营商ID
     * @param acs 特征值
     * @return
     */
    public native int nativeGetOperatorACs(int operatorID, ArrayList<Integer> acs);
    /**
     * CA 版本号
     * @return
     */
    public native String nativeGetCAVersionInfo();
}
