package com.joysee.adtv.aidl.ca;

import java.util.List;
import java.util.Map;



/**
 * 在这里定义了一些于Ca卡有关的接口用于在CaService中调用
 */
interface ICaSettingService {
    /**
     * 获取观看等级
     * @return 等级
     */
    int getWatchLevel();

    /**
     * 设置观看等级
     * 
     * @param pin pin密码
     * @param level
     * @return 0 操作成功 1 未知错误  3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
    int setWatchLevel(in String pin,in int level);

    /**
     * 设置工作时段 时分 需密码
     * @param pwd pin密码
     * @param iStarthour
     * @param iStartMin
     * @param iStartSec
     * @param iEndHour
     * @return 0 操作成功 1 未知错误  3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
    int setWatchTime(in String pwd,in int iStarthour,in int iStartMin,in int iEndHour,
            in int iEndMin);

    /**
     * 获取工作时段
     * @return int [flag,startHour,startMin,endHour,endMin]
     */
    int [] getWatchTime();

    /**
     * 修改密码
     * @param oldPwd
     * @param newPwd
     * @return 0 操作成功 1 未知错误  3 智能卡不在机顶盒内或者是无效卡 4 输入pin 码无效 不在0x00~0x09之间
     */
    int changePincode(in String oldPwd,in String newPwd);

    /**
     * 获取卡序列号，成功返回序号字符串，失败返回空字符串
     */
    String getCardSN();

    /**
     * 获取授权信息列表
     * @param operID 运营商ID
     * @return Map
     */
    List getAuthorization(in int operID);

    /**
     * 获取运营商ID 
     * @return 运营商ID数组
     */
    List getOperatorID();
}
