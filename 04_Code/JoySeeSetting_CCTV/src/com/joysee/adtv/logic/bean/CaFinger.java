package com.joysee.adtv.logic.bean;


/**
 * 封装OSD信息结构
 * @author wuhao
 */
public class CaFinger {
    private int ecmp_id;
    /** 智能卡 ID,要求十进制显示。为 0 时,取消指纹显示。 */
    private int card_id;

    public int getEcmp_id() {
        return ecmp_id;
    }

    public void setEcmp_id(int ecmp_id) {
        this.ecmp_id = ecmp_id;
    }

    public int getCard_id() {
        return card_id;
    }

    public void setCard_id(int card_id) {
        this.card_id = card_id;
    }
}
