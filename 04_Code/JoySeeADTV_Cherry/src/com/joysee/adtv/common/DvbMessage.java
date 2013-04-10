package com.joysee.adtv.common;

public final class DvbMessage {

	
	public int what;
	
	public int arg1;
	
	public int arg2;
	
	public Object obj;
	
	// sometimes we store linked lists of these things
    /*package*/ DvbMessage next;
    
    private static final Object sPoolSync = new Object();
    private static DvbMessage sPool;
    private static int sPoolSize = 0;

    private static final int MAX_POOL_SIZE = 10;
    
    /**
     * Return a new DVBMessage instance from the global pool. Allows us to
     * avoid allocating new objects in many cases.
     */
    public static DvbMessage obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
            	DvbMessage m = sPool;
                sPool = m.next;
                m.next = null;
                sPoolSize--;
                return m;
            }
        }
        return new DvbMessage();
    }
    
    /**
     * Same as {@link #obtain()}, but copies the values of an existing
     * message (including its target) into the new one.
     * @param orig Original DVBMessage to copy.
     * @return A DVBMessage object from the global pool.
     */
    public static DvbMessage obtain(DvbMessage orig) {
    	DvbMessage m = obtain();
        m.what = orig.what;
        m.arg1 = orig.arg1;
        m.arg2 = orig.arg2;
        m.obj = orig.obj;
        return m;
    }
    
    /**
     * Same as {@link #obtain()}, but sets the values for 
     * <em>what</em> members on the DVBMessage.
     * @param what  Value to assign to the <em>what</em> member.
     * @return A DVBMessage object from the global pool.
     */
    public static DvbMessage obtain(int what) {
    	DvbMessage m = obtain();
        m.what = what;

        return m;
    }
    
    /**
     * Same as {@link #obtain()}, but sets the values of the  <em>what</em>, 
     * <em>arg1</em>, <em>arg2</em>, and <em>obj</em> members.
     * 
     * @param what  The <em>what</em> value to set.
     * @param arg1  The <em>arg1</em> value to set.
     * @param arg2  The <em>arg2</em> value to set.
     * @param obj  The <em>obj</em> value to set.
     * @return  A DVBMessage object from the global pool.
     */
    public static DvbMessage obtain(int what,
            int arg1, int arg2, Object obj) {
    	DvbMessage m = obtain();
        m.what = what;
        m.arg1 = arg1;
        m.arg2 = arg2;
        m.obj = obj;

        return m;
    }
    public static DvbMessage obtain(int what,int arg1) {
    	DvbMessage m = obtain();
        m.what = what;
        m.arg1 = arg1;

        return m;
    }
    public static DvbMessage obtain(int what,Object obj) {
    	DvbMessage m = obtain();
        m.what = what;
        m.obj = obj;

        return m;
    }
    
    /**
     * Return a DVBMessage instance to the global pool.  You MUST NOT touch
     * the DVBMessage after calling this function -- it has effectively been
     * freed.
     */
    public void recycle() {
        clearForRecycle();

        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }
    
    /*package*/ void clearForRecycle() {
        what = 0;
        arg1 = 0;
        arg2 = 0;
        obj = null;
    }

}
