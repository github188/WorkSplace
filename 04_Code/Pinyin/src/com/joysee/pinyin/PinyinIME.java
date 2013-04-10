
package com.joysee.pinyin;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.joysee.pinyin.SoftKeyboard.KeyRow;

public class PinyinIME extends InputMethodService {

    /**
     * Used to show the floating window.
     */
    private PopupTimer mFloatingWindowTimer = new PopupTimer();

    /**
     * TAG for debug.
     */
    static final String TAG = "PinyinIME";

    private DecodingInfo mDecInfo = new DecodingInfo();
    /**
     * The current IME status.
     */
    private ImeState mImeState = ImeState.STATE_IDLE;
    /**
     * Used to switch input mode.
     */
    private InputModeSwitcher mInputModeSwitcher;
    /**
     * For English input.
     */
    private EnglishInputProcessor mImEn;

    /**
     * Connection used to bind the decoding service.
     */
    private PinyinDecoderServiceConnection mPinyinDecoderServiceConnection;

    /**
     * The floating container which contains the composing view. If necessary,
     * some other view like candiates container can also be put here.
     */
    private LinearLayout mFloatingContainer;

    /**
     * View to show the composing string.
     */
    private ComposingView mComposingView;

    /**
     * Window to show the composing string.
     */
    private PopupWindow mFloatingWindow;

    /**
     * View to show candidates list.
     */
    private CandidatesContainer mCandidatesContainer;

    /**
     * If is is true, IME will simulate key events for delete key, and send the
     * events back to the application.
     */
    private static final boolean SIMULATE_KEY_DELETE = true;

    private Environment mEnvironment;
    private SkbContainer mSkbContainer;
    private ArrayList<Integer> mRowList;
    private int mFocusRowNum;

    private boolean isFocusOnSkb = true;

    @Override
    public void onFinishInputView(boolean finishingInput) {
        PinyinLog.d(TAG, "onFinishInputView");
        mSkbContainer.resetFocus();
        isFocusOnSkb = true;
        mCandidatesContainer.getCandidateView().isEnableFocus = false;
        resetToIdleState(false);
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onFinishCandidatesView(boolean finishingInput) {
        PinyinLog.d(TAG, "onFinishCandidatesView");
        super.onFinishCandidatesView(finishingInput);
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        PinyinLog.d(TAG, "onStartInput..");
        updateIcon(mInputModeSwitcher.requestInputWithHkb(attribute));
        resetToIdleState(false);
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public void setExtractView(View view) {
        view.setLayoutParams(new LayoutParams(0, 0));
        view.setVisibility(View.GONE);
        super.setExtractView(view);
    }

    @Override
    public void onCreate() {
        PinyinLog.d(TAG, "onCreate");
        mEnvironment = Environment.getInstance();
        mInputModeSwitcher = new InputModeSwitcher(this);
        super.onCreate();
        startPinyinDecoderService();
        mImEn = new EnglishInputProcessor();
        Settings.getInstance(PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()));
        mEnvironment.onConfigurationChanged(getResources().getConfiguration(),
                this);
        mRowList = new ArrayList<Integer>();
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        PinyinLog.d(TAG, "onStartInputView");
        Dialog window = getWindow();
        android.view.WindowManager.LayoutParams params = window.getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.horizontalMargin = (float) (mEnvironment.getScreenWidth() * 0.28);
        PinyinLog.d(TAG, "onStartInputView(),params.horizontalMargin = " + params.horizontalMargin);
        params.width = (int) (mEnvironment.getScreenWidth() * 0.95);
        PinyinLog.d(TAG, "onStartInputView(),params.width = " + params.width);
        window.getWindow().setAttributes(params);
        
        updateIcon(mInputModeSwitcher.requestInputWithSkb(editorInfo));
        resetToIdleState(false);
        setCandidatesViewShown(false);
        mSkbContainer.updateInputMode();
        initRowNum();
        int focusRowNum = mSkbContainer.getFocusRowNum();
        PinyinLog.d(TAG, "onStartInputView(),focusRowNum = " + focusRowNum);
        mFocusRowNum = mRowList.indexOf(Integer.valueOf(focusRowNum));
        
        int focusKeyNum = mSkbContainer.getFocusKeyNum();
        PinyinLog.d(TAG, "mFocusRowNum = " + mFocusRowNum);
        if(mFocusRowNum == -1){
            mFocusRowNum = 3;
        }
        mSkbContainer.setFocus(mRowList.get(mFocusRowNum), focusKeyNum);
    }

    private void initRowNum() {
        mRowList.clear();
        SoftKeyboard softKeyBoKeyboard = mSkbContainer.getSoftKeyboard();
        int rowNum = softKeyBoKeyboard.getRowNum();
        for (int i = 0; i < rowNum; i++) {
            KeyRow keyRow = softKeyBoKeyboard.getKeyRowForDisplay(i);
            if (keyRow != null) {
                PinyinLog.d(TAG, i + "keyRow!=null");
                mRowList.add(i);
            }
        }
    }

    @Override
    public View onCreateCandidatesView() {
        PinyinLog.d(TAG, "onCreateCandidatesView");
        LayoutInflater inflater = getLayoutInflater();
        // Inflate the floating container view
        mFloatingContainer = (LinearLayout) inflater.inflate(
                R.layout.floating_container, null);

        // The first child is the composing view. //拼音view
        mComposingView = (ComposingView) mFloatingContainer.getChildAt(0);

        mCandidatesContainer = (CandidatesContainer) inflater.inflate(
                R.layout.candidates_container, null);

        mCandidatesContainer.initialize();

        mFloatingWindow = new PopupWindow(this);
        mFloatingWindow.setClippingEnabled(false);
        mFloatingWindow.setBackgroundDrawable(null);
        mFloatingWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        mFloatingWindow.setContentView(mFloatingContainer);

        return mCandidatesContainer;
    }

    @Override
    public View onCreateInputView() {
        PinyinLog.d(TAG, "onCreateInputView");
        LayoutInflater inflater = getLayoutInflater();
        mSkbContainer = (SkbContainer) inflater.inflate(R.layout.skb_container,
                null);
        mSkbContainer.setInputModeSwitcher(mInputModeSwitcher);
        return mSkbContainer;
    }

    private void updateIcon(int iconId) {
        if (iconId > 0) {
            showStatusIcon(iconId);
        } else {
            hideStatusIcon();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	PinyinLog.d(TAG, "keyCode = " + keyCode);
//    	if(keyCode == KeyEvent.KEYCODE_F12){
//    		dismissCandidateWindow();
//    	}
    	if(keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK){
    		updateIcon(mInputModeSwitcher.switchToEngMode());
            resetToIdleState(false);
    	}
        if (processKey(event, 0 != event.getRepeatCount()))
            return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//    	if(keyCode == KeyEvent.KEYCODE_F11){
//    		updateIcon(mInputModeSwitcher.switchModeForUserKey(-2));
//            resetToIdleState(false);
//            mSkbContainer.updateInputMode();
//            initRowNum();
//            int focusKeyNum = mSkbContainer.getFocusKeyNum();
//            PinyinLog.d(TAG, "mFocusRowNum" + mFocusRowNum);
//            int focusRowNum = mRowList.get(mFocusRowNum);
//            PinyinLog.d(TAG, "fousRowNum = " + focusRowNum);
//            mSkbContainer.setFocus(focusRowNum, focusKeyNum);
//    	}
        if (processKey(event, true))
            return true;
        return super.onKeyUp(keyCode, event);
    }

    private boolean processKey(KeyEvent event, boolean realAction) {

        PinyinLog.d(TAG, "process key enter");
        if (ImeState.STATE_BYPASS == mImeState) {
            PinyinLog.d(TAG, "ImeState.STATE_BYPASS == mImeState");
            return false;
        }
        int keyCode = event.getKeyCode();
        // SHIFT-SPACE is used to switch between Chinese and English
        // when HKB is on.
        // if (KeyEvent.KEYCODE_SPACE == keyCode && event.isShiftPressed())
        // {//按shift-space可以切换中英文
        // PinyinLog.d(TAG,"KeyEvent.KEYCODE_SPACE == keyCode && event.isShiftPressed()");
        // if (!realAction) return true;
        //
        // updateIcon(mInputModeSwitcher.switchLanguageWithHkb());
        // resetToIdleState(false);
        //
        // int allMetaState = KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON
        // | KeyEvent.META_ALT_RIGHT_ON | KeyEvent.META_SHIFT_ON
        // | KeyEvent.META_SHIFT_LEFT_ON
        // | KeyEvent.META_SHIFT_RIGHT_ON | KeyEvent.META_SYM_ON;
        // getCurrentInputConnection().clearMetaKeyStates(allMetaState);
        // return true;
        // }

        // If HKB is on to input English, by-pass the key event so that
        // default key listener will handle it.
        if (mInputModeSwitcher.isEnglishWithHkb()) {
            PinyinLog.d(TAG,
                    "mInputModeSwitcher.isEnglishWithHkb() = "
                            + mInputModeSwitcher.isEnglishWithHkb());
            return false;
        }

        PinyinLog.d(TAG, "process function enter");
        if (processFunctionKeys(keyCode, realAction)) {
            PinyinLog.d(TAG, "processFunctionKeys return true");
            return true;
        }

        int keyChar = 0;
        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
//        	setBackground(keyCode);
            keyChar = keyCode - KeyEvent.KEYCODE_A + 'a';
        } else if (keyCode >= KeyEvent.KEYCODE_0
                && keyCode <= KeyEvent.KEYCODE_9) {
            keyChar = keyCode - KeyEvent.KEYCODE_0 + '0';
        } else if (keyCode == KeyEvent.KEYCODE_COMMA) {
            keyChar = ',';
        } else if (keyCode == KeyEvent.KEYCODE_PERIOD) {
            keyChar = '.';
        } else if (keyCode == KeyEvent.KEYCODE_SPACE) {
            keyChar = ' ';
        } else if (keyCode == KeyEvent.KEYCODE_APOSTROPHE) {
            keyChar = '\'';
        }

        if (mInputModeSwitcher.isEnglishWithSkb()) {// 英文
            PinyinLog.d(TAG, "isEnglishWithSkb");

            return mImEn.processKey(getCurrentInputConnection(), event,
                    mInputModeSwitcher.isEnglishUpperCaseWithSkb(), realAction);

        } else if (mInputModeSwitcher.isChineseText()) {// 中文
            if (mImeState == ImeState.STATE_IDLE ||
                    mImeState == ImeState.STATE_APP_COMPLETION) {
                mImeState = ImeState.STATE_IDLE;
                PinyinLog.d(TAG, "STATE_IDLE");
                return processStateIdle(keyChar, keyCode, event, realAction);

            } else if (mImeState == ImeState.STATE_INPUT) {
                PinyinLog.d(TAG, "STATE_INPUT");
                return processStateInput(keyChar, keyCode, event, realAction);

            } else if (mImeState == ImeState.STATE_PREDICT) {
                PinyinLog.d(TAG, "STATE_PREDICT");
                return processStatePredict(keyChar, keyCode, event, realAction);

            } else if (mImeState == ImeState.STATE_COMPOSING) {
                PinyinLog.d(TAG, "STATE_COMPOSING");
                return processStateEditComposing(keyChar, keyCode, event,
                        realAction);
            }
        } else {
//            if (0 != keyChar && realAction) {
//                commitResultText(String.valueOf((char) keyChar));
//                return true;
//            }
        }
        return false;
    }

//    private void setBackground(int keyCode) {
//    	Log.d("songwenxuan","keyCode = " + keyCode);
//    	
//    	SoftKeyboard softKeyboard = mSkbContainer.getSoftKeyboard();
//    	String locationStr = softKeyboard.mSoftKeyLocation.get(keyCode);
//    	Log.d("songwenxuan","locationStr = " + locationStr);
//    	String[] location = locationStr.split("|");
//    	mSkbContainer.setFocus(Integer.parseInt(location[0]), Integer.parseInt(location[1]));
//	}

	private boolean processFunctionKeys(int keyCode, boolean realAction) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            PinyinLog.d(TAG, "KEYCODE_DPAD_CENTER");
            if ((null != mSkbContainer) && mSkbContainer.isShown() && isFocusOnSkb) {// 键盘区焦点
                if (!realAction) return true;
                SoftKeyboard softKeyBoKeyboard = mSkbContainer.getSoftKeyboard();
                SoftKey key = softKeyBoKeyboard.getKey(mSkbContainer.getFocusRowNum(),
                        mSkbContainer.getFocusKeyNum());
                PinyinLog.d(TAG, "keycode = " + key.getKeyCode());
                if (key.isUserDefKey()) {//功能键。
                    PinyinLog.d(TAG, "key.isUserDefKey()");
                    Log.d("songwenxuan","isUserDefKey() = " + key.getKeyCode());
                    updateIcon(mInputModeSwitcher.switchModeForUserKey(key.getKeyCode()));
                    resetToIdleState(false);
                    mSkbContainer.updateInputMode();
                    initRowNum();
                    int focusKeyNum = mSkbContainer.getFocusKeyNum();
                    PinyinLog.d(TAG, "mFocusRowNum" + mFocusRowNum);
                    int focusRowNum = mRowList.get(mFocusRowNum);
                    PinyinLog.d(TAG, "fousRowNum = " + focusRowNum);
                    mSkbContainer.setFocus(focusRowNum, focusKeyNum);
                    return true;
                } else if(key.isUniStrKey()) {//特殊字符按键
                    boolean kUsed = false;
                    String keyLabel = key.getKeyLabel();
                    if (mInputModeSwitcher.isChineseTextWithSkb()
                            && (ImeState.STATE_INPUT == mImeState || ImeState.STATE_COMPOSING == mImeState)) {
                        if (mDecInfo.length() > 0 && keyLabel.length() == 1
                                && keyLabel.charAt(0) == '\'') {
                            processSurfaceChange('\'', 0);
                            kUsed = true;
                        }
                    }
                    if (!kUsed) {
                        if (ImeState.STATE_INPUT == mImeState) {
                            PinyinLog.d(TAG, "processFunctionKeys(),ImeState.STATE_INPUT == mImeState");
                            commitResultText(mDecInfo
                                    .getCurrentFullSent(mCandidatesContainer
                                            .getActiveCandiatePos()));
                        } else if (ImeState.STATE_COMPOSING == mImeState) {
                            commitResultText(mDecInfo.getComposingStr());
                        }
                        commitResultText(keyLabel);
                        resetToIdleState(false);
                    }
                    return true;
                }
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, key.getKeyCode());
                processKey(event, true);
                PinyinLog.d(TAG, "KEYCODE_DPAD_CENTER isFocusOnSkb return true");
                return true;
            } else if (null != mCandidatesContainer && mCandidatesContainer.isShown()
                    && !mDecInfo.isCandidatesListEmpty() && !isFocusOnSkb) {
                if (!realAction)
                    return true;
                chooseCandidate(-1);
                setCandidateFocus(false);
//                resetToIdleState(false);
                mSkbContainer.setFocus(1, 5);
                mFocusRowNum = mRowList.indexOf(Integer.valueOf(1));
                isFocusOnSkb = true;
                PinyinLog.d(TAG, "KEYCODE_DPAD_CENTER !isFocusOnSkb return true");
                return true;
            }
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            PinyinLog.d(TAG, "KEYCODE_DPAD_LEFT");
            PinyinLog.d(TAG, "keyCode == KeyEvent.KEYCODE_DPAD_LEFT");
            if ((null != mSkbContainer) && mSkbContainer.isShown() && isFocusOnSkb) {
                if (!realAction)
                    return true;
                int focusRowNum = mSkbContainer.getFocusRowNum();
                int focusKeyNum = mSkbContainer.getFocusKeyNum();
                if (--focusKeyNum < 0) {
                    PinyinLog.d(TAG, "KEYCODE_DPAD_LEFT isFocusOnSkb focusKeyNum<0 return true");
                    return true;
                }
                mSkbContainer.setFocus(focusRowNum, focusKeyNum);
                PinyinLog.d(TAG, "KEYCODE_DPAD_LEFT isFocusOnSkb return true");
                return true;
            } else if (null != mCandidatesContainer && mCandidatesContainer.isShown()
                    && !mDecInfo.isCandidatesListEmpty() && !isFocusOnSkb) {
                if (!realAction)
                    return true;
                mCandidatesContainer.activeCurseBackward();
                PinyinLog.d(TAG, "KEYCODE_DPAD_LEFT !isFocusOnSkb return true");
                return true;
            }
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            PinyinLog.d(TAG, "KEYCODE_DPAD_RIGHT");
            if ((null != mSkbContainer) && mSkbContainer.isShown() && isFocusOnSkb) {
                if (!realAction)
                    return true;
                int focusRowNum = mSkbContainer.getFocusRowNum();
                int focusKeyNum = mSkbContainer.getFocusKeyNum();
                SoftKeyboard softKeyBoKeyboard = mSkbContainer.getSoftKeyboard();
                KeyRow keyRow = softKeyBoKeyboard.getKeyRowForDisplay(focusRowNum);
                if (++focusKeyNum > keyRow.mSoftKeys.size() - 1) {
                    PinyinLog.d(TAG,
                            "KEYCODE_DPAD_LEFT isFocusOnSkb ++focusKeyNum > keyRow.mSoftKeys.size() - 1 return true");
                    return true;
                }
                mSkbContainer.setFocus(focusRowNum, focusKeyNum);
                PinyinLog.d(TAG, "KEYCODE_DPAD_RIGHT isFocusOnSkb return true");
                return true;
            } else if (null != mCandidatesContainer && mCandidatesContainer.isShown()
                    && !mDecInfo.isCandidatesListEmpty() && !isFocusOnSkb) {
                if (!realAction)
                    return true;
                mCandidatesContainer.activeCurseForward();
                PinyinLog.d(TAG, "KEYCODE_DPAD_RIGHT !isFocusOnSkb return true");
                return true;
            }
            return false;
        }
        //
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            PinyinLog.d(TAG, "KEYCODE_DPAD_UP");
            if ((null != mSkbContainer) && mSkbContainer.isShown() && isFocusOnSkb) {
                if (!realAction)
                    return true;
                // int focusRowNum = mSkbContainer.getFocusRowNum();
                int focusKeyNum = mSkbContainer.getFocusKeyNum();
                if (--mFocusRowNum < 0) {
                    if (null != mCandidatesContainer && mCandidatesContainer.isShown()
                            && !mDecInfo.isCandidatesListEmpty()) {
                        isFocusOnSkb = false;
                        setCandidateFocus(true);
                        mSkbContainer.setFocus(-1, -1);
                    }
                    mFocusRowNum = 0;
                    PinyinLog.d(TAG,
                            "KEYCODE_DPAD_UP isFocusOnSkb --mFocusRowNum < 0  return true");
                    return true;
                } else if (mFocusRowNum == 2) {// 太2了。
                    switch (focusKeyNum) {
                        case 2:
                            focusKeyNum = 4;
                            break;
                        case 3:
                            focusKeyNum = 6;
                            break;
                        case 4:
                            focusKeyNum = 8;
                            break;
                        default:
                            break;
                    }
                }
                PinyinLog.d(TAG, "row num" + mRowList.get(mFocusRowNum));
                mSkbContainer.setFocus(mRowList.get(mFocusRowNum), focusKeyNum);
                PinyinLog.d(TAG, "KEYCODE_DPAD_UP isFocusOnSkb return true");
                return true;
            } else if (null != mCandidatesContainer && mCandidatesContainer.isShown()
                    && !mDecInfo.isCandidatesListEmpty() && !isFocusOnSkb) {
                if (!realAction)
                    return true;
                PinyinLog.d(TAG, "KEYCODE_DPAD_UP !isFocusOnSkb return true");
                return true;
            }
            return false;
        }
        //
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            PinyinLog.d(TAG, "KEYCODE_DPAD_DOWN");
            if ((null != mSkbContainer) && mSkbContainer.isShown() && isFocusOnSkb) {
                if (!realAction)
                    return true;
                // int focusRowNum = mSkbContainer.getFocusRowNum();
                int focusKeyNum = mSkbContainer.getFocusKeyNum();
                if (++mFocusRowNum > 3) {
                    mFocusRowNum = 3;
                    PinyinLog.d(TAG,
                            "KEYCODE_DPAD_DOWN isFocusOnSkb ++mFocusRowNum > 3 return true");
                    return true;
                } else if (mFocusRowNum == 1 && focusKeyNum == 9) {
                    focusKeyNum = 8;
                } else if (mFocusRowNum == 3) {// 烂透了。
                    switch (focusKeyNum) {
                        case 2:
                            focusKeyNum = 1;
                            break;
                        case 3:
                        case 4:
                        case 5:
                            focusKeyNum = 2;
                            break;
                        case 6:
                        case 7:
                            focusKeyNum = 3;
                            break;
                        case 8:
                            focusKeyNum = 4;
                            break;
                        default:
                            break;
                    }
                }
                PinyinLog.d(TAG, "row num" + mRowList.get(mFocusRowNum));
                mSkbContainer.setFocus(mRowList.get(mFocusRowNum), focusKeyNum);
                PinyinLog.d(TAG, "KEYCODE_DPAD_DOWN isFocusOnSkb return true");
                return true;
            } else if (null != mCandidatesContainer && mCandidatesContainer.isShown()
                    && !mDecInfo.isCandidatesListEmpty() && !isFocusOnSkb) {
                if (!realAction)
                    return true;
                isFocusOnSkb = true;
                setCandidateFocus(false);
                mSkbContainer.setFocus(0, 5);
                PinyinLog.d(TAG, "KEYCODE_DPAD_DOWN !isFocusOnSkb return true");
                return true;
            }
            return false;
        }
        if (null == mCandidatesContainer || !mCandidatesContainer.isShown()
                || mDecInfo.isCandidatesListEmpty()) {
            PinyinLog.d(TAG, "imState == " + mImeState);
            if (keyCode == KeyEvent.KEYCODE_DEL &&
                    ImeState.STATE_PREDICT == mImeState) {
                if (!realAction)
                    return true;
                resetToIdleState(false);
                return true;
            }
            
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                PinyinLog.d(TAG, "KeyEvent.KEYCODE_DEL");
                if (!realAction)
                    return true;
                if (SIMULATE_KEY_DELETE) {
                    if(ImeState.STATE_COMPOSING == mImeState){
                        return false;
                    }
                    PinyinLog.d(TAG, "SIMULATE_KEY_DELETE");
                    simulateKeyEventDownUp(keyCode);
                } else {
                    PinyinLog.d(TAG, "getCurrentInputConnection().deleteSurroundingText(1, 0)");
                    getCurrentInputConnection().deleteSurroundingText(1, 0);
                }
                return true;
            }
            
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (!realAction)
                    return true;
                sendKeyChar('\n');
                return true;
            }
            
            if (keyCode == KeyEvent.KEYCODE_SPACE) {
                if (!realAction)
                    return true;
                sendKeyChar(' ');
                return true;
            }
        }
        return false;
    }

    private void setCandidateFocus(boolean enableFocus) {
        CandidateView candidateView = mCandidatesContainer.getCandidateView();
        candidateView.isEnableFocus = enableFocus;
        candidateView.invalidate();
    }

    @Override
    public void requestHideSelf(int flags) {
        if (mEnvironment.needDebug()) {
            PinyinLog.d(TAG, "DimissSoftInput.");
        }
        dismissCandidateWindow();
        super.requestHideSelf(flags);
    }

    public enum ImeState {
        STATE_BYPASS, STATE_IDLE, STATE_INPUT, STATE_COMPOSING, STATE_PREDICT,
        STATE_APP_COMPLETION
    }

    private boolean processStateIdle(int keyChar, int keyCode, KeyEvent event,
            boolean realAction) {
        PinyinLog.d(TAG, "processStateIdle");
        // In this status, when user presses keys in [a..z], the status will
        // change to input state.
        if (keyChar >= 'a' && keyChar <= 'z' && !event.isAltPressed()) {
            if (!realAction)
                return true;
            mDecInfo.addSplChar((char) keyChar, true);
            chooseAndUpdate(-1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (!realAction)
                return true;
            if (SIMULATE_KEY_DELETE) {
                simulateKeyEventDownUp(keyCode);
            } else {
                getCurrentInputConnection().deleteSurroundingText(1, 0);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (!realAction)
                return true;
            sendKeyChar('\n');
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_ALT_LEFT
                || keyCode == KeyEvent.KEYCODE_ALT_RIGHT
                || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
                || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            return true;
        } else if (event.isAltPressed()) {
            char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
            if (0 != fullwidth_char) {
                if (realAction) {
                    String result = String.valueOf(fullwidth_char);
                    commitResultText(result);
                }
                return true;
            } else {
                if (keyCode >= KeyEvent.KEYCODE_A
                        && keyCode <= KeyEvent.KEYCODE_Z) {
                    return true;
                }
            }
        } else if (keyChar != 0 && keyChar != '\t') {
            if (realAction) {
                if (keyChar == ',' || keyChar == '.') {
                    inputCommaPeriod("", keyChar, false, ImeState.STATE_IDLE);
                } else {
                    if (0 != keyChar) {
                        String result = String.valueOf((char) keyChar);
                        commitResultText(result);
                    }
                }
            }
            return true;
        }
        PinyinLog.d(TAG, "processStateIdle return false");
        return false;
    }

    private boolean processStateInput(int keyChar, int keyCode, KeyEvent event,
            boolean realAction) {
        PinyinLog.d(TAG, "processStateInput()");
        // If ALT key is pressed, input alternative key. But if the
        // alternative key is quote key, it will be used for input a splitter
        // in Pinyin string.
        if (event.isAltPressed()) {
            if ('\'' != event.getUnicodeChar(event.getMetaState())) {
                if (realAction) {
                    char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
                    if (0 != fullwidth_char) {
                        commitResultText(mDecInfo
                                .getCurrentFullSent(mCandidatesContainer
                                        .getActiveCandiatePos()) +
                                String.valueOf(fullwidth_char));
                        resetToIdleState(false);
                    }
                }
                return true;
            } else {
                keyChar = '\'';
            }
        }

        if (keyChar >= 'a' && keyChar <= 'z' || keyChar == '\''
                && !mDecInfo.charBeforeCursorIsSeparator()
                || keyCode == KeyEvent.KEYCODE_DEL) {
            if (!realAction)
                return true;
            return processSurfaceChange(keyChar, keyCode);
        } else if (keyChar == ',' || keyChar == '.') {
            if (!realAction)
                return true;
            inputCommaPeriod(mDecInfo.getCurrentFullSent(mCandidatesContainer
                    .getActiveCandiatePos()), keyChar, true,
                    ImeState.STATE_IDLE);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP
                || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (!realAction)
                return true;

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                mCandidatesContainer.activeCurseBackward();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mCandidatesContainer.activeCurseForward();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                // If it has been the first page, a up key will shift
                // the state to edit composing string.
                if (!mCandidatesContainer.pageBackward(false, true)) {
                    mCandidatesContainer.enableActiveHighlight(false);
                    changeToStateComposing(true);
                    updateComposingText(true);
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                mCandidatesContainer.pageForward(false, true);
            }
            return true;
        } else if (keyCode >= KeyEvent.KEYCODE_1
                && keyCode <= KeyEvent.KEYCODE_9) {
            if (!realAction)
                return true;

            int activePos = keyCode - KeyEvent.KEYCODE_1;
            int currentPage = mCandidatesContainer.getCurrentPage();
            if (activePos < mDecInfo.getCurrentPageSize(currentPage)) {
                activePos = activePos
                        + mDecInfo.getCurrentPageStart(currentPage);
                if (activePos >= 0) {
                    chooseAndUpdate(activePos);
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (!realAction)
                return true;
            if (mInputModeSwitcher.isEnterNoramlState()) {
                commitResultText(mDecInfo.getOrigianlSplStr().toString());
                resetToIdleState(false);
            } else {
                commitResultText(mDecInfo
                        .getCurrentFullSent(mCandidatesContainer
                                .getActiveCandiatePos()));
                sendKeyChar('\n');
                resetToIdleState(false);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (!realAction)
                return true;
            chooseCandidate(-1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!realAction)
                return true;
            resetToIdleState(false);
            requestHideSelf(0);
            return true;
        }
        return false;
    }

    private boolean processStatePredict(int keyChar, int keyCode,
            KeyEvent event, boolean realAction) {
        PinyinLog.d(TAG, "processStateInput");
        if (!realAction)
            return true;

        // If ALT key is pressed, input alternative key.
        if (event.isAltPressed()) {
            char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
            if (0 != fullwidth_char) {
                commitResultText(mDecInfo.getCandidate(mCandidatesContainer
                        .getActiveCandiatePos()) +
                        String.valueOf(fullwidth_char));
                resetToIdleState(false);
            }
            return true;
        }

        // In this status, when user presses keys in [a..z], the status will
        // change to input state.
        if (keyChar >= 'a' && keyChar <= 'z') {
            changeToStateInput(true);
            mDecInfo.addSplChar((char) keyChar, true);
            chooseAndUpdate(-1);
        } else if (keyChar == ',' || keyChar == '.') {
            inputCommaPeriod("", keyChar, true, ImeState.STATE_IDLE);
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP
                || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                mCandidatesContainer.activeCurseBackward();
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mCandidatesContainer.activeCurseForward();
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                mCandidatesContainer.pageBackward(false, true);
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                mCandidatesContainer.pageForward(false, true);
            }
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            resetToIdleState(false);
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            resetToIdleState(false);
            requestHideSelf(0);
        } else if (keyCode >= KeyEvent.KEYCODE_1
                && keyCode <= KeyEvent.KEYCODE_9) {
            int activePos = keyCode - KeyEvent.KEYCODE_1;
            int currentPage = mCandidatesContainer.getCurrentPage();
            if (activePos < mDecInfo.getCurrentPageSize(currentPage)) {
                activePos = activePos
                        + mDecInfo.getCurrentPageStart(currentPage);
                if (activePos >= 0) {
                    chooseAndUpdate(activePos);
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            sendKeyChar('\n');
            resetToIdleState(false);
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            chooseCandidate(-1);
        }

        return true;
    }

    private boolean processStateEditComposing(int keyChar, int keyCode,
            KeyEvent event, boolean realAction) {
        PinyinLog.d(TAG, "processStateEditComposing()");
        if (!realAction){
            PinyinLog.d(TAG, "!realAction");
            return true;
        }

        ComposingView.ComposingStatus cmpsvStatus =
                mComposingView.getComposingStatus();

        // If ALT key is pressed, input alternative key. But if the
        // alternative key is quote key, it will be used for input a splitter
        // in Pinyin string.
        if (event.isAltPressed()) {
            PinyinLog.d(TAG, "event.isAltPressed()");
            if ('\'' != event.getUnicodeChar(event.getMetaState())) {
                char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
                if (0 != fullwidth_char) {
                    String retStr;
                    if (ComposingView.ComposingStatus.SHOW_STRING_LOWERCASE == cmpsvStatus) {
                        retStr = mDecInfo.getOrigianlSplStr().toString();
                    } else {
                        retStr = mDecInfo.getComposingStr();
                    }
                    commitResultText(retStr + String.valueOf(fullwidth_char));
                    resetToIdleState(false);
                }
                return true;
            } else {
                keyChar = '\'';
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            PinyinLog.d(TAG, "keyCode == KeyEvent.KEYCODE_DPAD_DOWN");
            if (!mDecInfo.selectionFinished()) {
                changeToStateInput(true);
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            PinyinLog.d(TAG, "keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT");
            mComposingView.moveCursor(keyCode);
        } else if ((keyCode == KeyEvent.KEYCODE_ENTER && mInputModeSwitcher
                .isEnterNoramlState())
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (ComposingView.ComposingStatus.SHOW_STRING_LOWERCASE == cmpsvStatus) {
                PinyinLog.d(TAG, "ComposingView.ComposingStatus.SHOW_STRING_LOWERCASE == cmpsvStatus");
                String str = mDecInfo.getOrigianlSplStr().toString();
                if (!tryInputRawUnicode(str)) {
                    commitResultText(str);
                }
            } else if (ComposingView.ComposingStatus.EDIT_PINYIN == cmpsvStatus) {
                PinyinLog.d(TAG, "ComposingView.ComposingStatus.EDIT_PINYIN == cmpsvStatus");
                String str = mDecInfo.getComposingStr();
                if (!tryInputRawUnicode(str)) {
                    commitResultText(str);
                }
            } else {
                commitResultText(mDecInfo.getComposingStr());
            }
            resetToIdleState(false);
        } else if (keyCode == KeyEvent.KEYCODE_ENTER
                && !mInputModeSwitcher.isEnterNoramlState()) {
            PinyinLog.d(TAG, "keyCode == KeyEvent.KEYCODE_ENTER && !mInputModeSwitcher.isEnterNoramlState()");
            String retStr;
            if (!mDecInfo.isCandidatesListEmpty()) {
                retStr = mDecInfo.getCurrentFullSent(mCandidatesContainer
                        .getActiveCandiatePos());
            } else {
                retStr = mDecInfo.getComposingStr();
            }
            commitResultText(retStr);
            sendKeyChar('\n');
            resetToIdleState(false);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            PinyinLog.d(TAG, "processStateEditComposing(),keyCode == KeyEvent.KEYCODE_BACK");
            resetToIdleState(false);
            requestHideSelf(0);
            return true;
        } else {
            PinyinLog.d(TAG, "processStateEditComposing(),call processSurfaceChange()");
            return processSurfaceChange(keyChar, keyCode);
        }
        return true;
    }

    private boolean startPinyinDecoderService() {
        if (null == mDecInfo.mIPinyinDecoderService) {
            Intent serviceIntent = new Intent();
            serviceIntent.setClass(this, PinyinDecoderService.class);

            if (null == mPinyinDecoderServiceConnection) {
                mPinyinDecoderServiceConnection = new PinyinDecoderServiceConnection();
            }

            // Bind service
            if (bindService(serviceIntent, mPinyinDecoderServiceConnection,
                    Context.BIND_AUTO_CREATE)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Connection used for binding to the Pinyin decoding service.
     */
    public class PinyinDecoderServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDecInfo.mIPinyinDecoderService = IPinyinDecoderService.Stub
                    .asInterface(service);
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    public class DecodingInfo {

        /**
         * Maximum length of the Pinyin string
         */
        private static final int PY_STRING_MAX = 28;

        /**
         * Maximum number of candidates to display in one page.
         */
        private static final int MAX_PAGE_SIZE_DISPLAY = 20;

        /**
         * Spelling (Pinyin) string.
         */
        private StringBuffer mSurface;

        /**
         * Byte buffer used as the Pinyin string parameter for native function
         * call.
         */
        private byte mPyBuf[];

        /**
         * The length of surface string successfully decoded by engine.
         */
        private int mSurfaceDecodedLen;

        /**
         * Composing string.
         */
        private String mComposingStr;

        /**
         * Length of the active composing string.
         */
        private int mActiveCmpsLen;

        /**
         * Composing string for display, it is copied from mComposingStr, and
         * add spaces between spellings.
         **/
        private String mComposingStrDisplay;

        /**
         * Length of the active composing string for display.
         */
        private int mActiveCmpsDisplayLen;

        /**
         * The first full sentence choice.
         */
        private String mFullSent;

        /**
         * Number of characters which have been fixed.
         */
        private int mFixedLen;

        /**
         * If this flag is true, selection is finished.
         */
        private boolean mFinishSelection;

        /**
         * The starting position for each spelling. The first one is the number
         * of the real starting position elements.
         */
        private int mSplStart[];

        /**
         * Editing cursor in mSurface.
         */
        private int mCursorPos;

        /**
         * Remote Pinyin-to-Hanzi decoding engine service.
         */
        private IPinyinDecoderService mIPinyinDecoderService;

        /**
         * The complication information suggested by application.
         */
        private CompletionInfo[] mAppCompletions;

        /**
         * The total number of choices for display. The list may only contains
         * the first part. If user tries to navigate to next page which is not
         * in the result list, we need to get these items.
         **/
        public int mTotalChoicesNum;

        /**
         * Candidate list. The first one is the full-sentence candidate.
         */
        public List<String> mCandidatesList = new Vector<String>();

        /**
         * Element i stores the starting position of page i.
         */
        public Vector<Integer> mPageStart = new Vector<Integer>();

        /**
         * Element i stores the number of characters to page i.
         */
        public Vector<Integer> mCnToPage = new Vector<Integer>();

        /**
         * The position to delete in Pinyin string. If it is less than 0, IME
         * will do an incremental search, otherwise IME will do a deletion
         * operation. if {@link #mIsPosInSpl} is true, IME will delete the whole
         * string for mPosDelSpl-th spelling, otherwise it will only delete
         * mPosDelSpl-th character in the Pinyin string.
         */
        public int mPosDelSpl = -1;

        /**
         * If {@link #mPosDelSpl} is big than or equal to 0, this member is used
         * to indicate that whether the postion is counted in spelling id or
         * character.
         */
        public boolean mIsPosInSpl;

        public DecodingInfo() {
            mSurface = new StringBuffer();
            mSurfaceDecodedLen = 0;
        }

        public void reset() {
            mSurface.delete(0, mSurface.length());
            mSurfaceDecodedLen = 0;
            mCursorPos = 0;
            mFullSent = "";
            mFixedLen = 0;
            mFinishSelection = false;
            mComposingStr = "";
            mComposingStrDisplay = "";
            mActiveCmpsLen = 0;
            mActiveCmpsDisplayLen = 0;

            resetCandidates();
        }

        public boolean isCandidatesListEmpty() {
            return mCandidatesList.size() == 0;
        }

        public boolean isSplStrFull() {
            if (mSurface.length() >= PY_STRING_MAX - 1)
                return true;
            return false;
        }

        public void addSplChar(char ch, boolean reset) {
            if (reset) {
                mSurface.delete(0, mSurface.length());
                mSurfaceDecodedLen = 0;
                mCursorPos = 0;
                try {
                    mIPinyinDecoderService.imResetSearch();
                } catch (RemoteException e) {
                }
            }
            mSurface.insert(mCursorPos, ch);
            mCursorPos++;
        }

        // Prepare to delete before cursor. We may delete a spelling char if
        // the cursor is in the range of unfixed part, delete a whole spelling
        // if the cursor in inside the range of the fixed part.
        // This function only marks the position used to delete.
        public void prepareDeleteBeforeCursor() {
            if (mCursorPos > 0) {
                int pos;
                for (pos = 0; pos < mFixedLen; pos++) {
                    if (mSplStart[pos + 2] >= mCursorPos
                            && mSplStart[pos + 1] < mCursorPos) {
                        mPosDelSpl = pos;
                        mCursorPos = mSplStart[pos + 1];
                        mIsPosInSpl = true;
                        break;
                    }
                }
                if (mPosDelSpl < 0) {
                    mPosDelSpl = mCursorPos - 1;
                    mCursorPos--;
                    mIsPosInSpl = false;
                }
            }
        }

        public int length() {
            return mSurface.length();
        }

        public char charAt(int index) {
            return mSurface.charAt(index);
        }

        public StringBuffer getOrigianlSplStr() {
            return mSurface;
        }

        public int getSplStrDecodedLen() {
            return mSurfaceDecodedLen;
        }

        public int[] getSplStart() {
            return mSplStart;
        }

        public String getComposingStr() {
            return mComposingStr;
        }

        public String getComposingStrActivePart() {
            assert (mActiveCmpsLen <= mComposingStr.length());
            return mComposingStr.substring(0, mActiveCmpsLen);
        }

        public int getActiveCmpsLen() {
            return mActiveCmpsLen;
        }

        public String getComposingStrForDisplay() {
            return mComposingStrDisplay;
        }

        public int getActiveCmpsDisplayLen() {
            return mActiveCmpsDisplayLen;
        }

        public String getFullSent() {
            return mFullSent;
        }

        public String getCurrentFullSent(int activeCandPos) {
            try {
                String retStr = mFullSent.substring(0, mFixedLen);
                retStr += mCandidatesList.get(activeCandPos);
                return retStr;
            } catch (Exception e) {
                return "";
            }
        }

        public void resetCandidates() {
            mCandidatesList.clear();
            mTotalChoicesNum = 0;

            mPageStart.clear();
            mPageStart.add(0);
            mCnToPage.clear();
            mCnToPage.add(0);
        }

        public boolean candidatesFromApp() {
            return ImeState.STATE_APP_COMPLETION == mImeState;
        }

        public boolean canDoPrediction() {
            return mComposingStr.length() == mFixedLen;
        }

        public boolean selectionFinished() {
            return mFinishSelection;
        }

        // After the user chooses a candidate, input method will do a
        // re-decoding and give the new candidate list.
        // If candidate id is less than 0, means user is inputting Pinyin,
        // not selecting any choice.
        private void chooseDecodingCandidate(int candId) {
            if (mImeState != ImeState.STATE_PREDICT) {
                resetCandidates();
                int totalChoicesNum = 0;
                try {
                    if (candId < 0) {
                        if (length() == 0) {
                            totalChoicesNum = 0;
                        } else {
                            if (mPyBuf == null)
                                mPyBuf = new byte[PY_STRING_MAX];
                            for (int i = 0; i < length(); i++)
                                mPyBuf[i] = (byte) charAt(i);
                            mPyBuf[length()] = 0;

                            if (mPosDelSpl < 0) {
                                totalChoicesNum = mIPinyinDecoderService
                                        .imSearch(mPyBuf, length());
                            } else {
                                boolean clear_fixed_this_step = true;
                                if (ImeState.STATE_COMPOSING == mImeState) {
                                    clear_fixed_this_step = false;
                                }
                                totalChoicesNum = mIPinyinDecoderService
                                        .imDelSearch(mPosDelSpl, mIsPosInSpl,
                                                clear_fixed_this_step);
                                mPosDelSpl = -1;
                            }
                        }
                    } else {
                        totalChoicesNum = mIPinyinDecoderService
                                .imChoose(candId);
                    }
                } catch (RemoteException e) {
                }
                updateDecInfoForSearch(totalChoicesNum);
            }
        }

        private void updateDecInfoForSearch(int totalChoicesNum) {
            PinyinLog.d(TAG, "updateDecInfoForSearch(),totalChoicesNum = " + totalChoicesNum);
            mTotalChoicesNum = totalChoicesNum;
            if (mTotalChoicesNum < 0) {
                mTotalChoicesNum = 0;
                return;
            }

            try {
                String pyStr;

                mSplStart = mIPinyinDecoderService.imGetSplStart();
                PinyinLog.d(TAG, "updateDecInfoForSearch(), mSplStart = " + mSplStart);
                
                pyStr = mIPinyinDecoderService.imGetPyStr(false);//pinyin
                PinyinLog.d(TAG, "updateDecInfoForSearch(),pyStr="+pyStr);
                
                mSurfaceDecodedLen = mIPinyinDecoderService.imGetPyStrLen(true);//拼音长度
                
                assert (mSurfaceDecodedLen <= pyStr.length());
                PinyinLog.d(TAG, "updateDecInfoForSearch(),mSurfaceDecodedLen="+mSurfaceDecodedLen);

                mFullSent = mIPinyinDecoderService.imGetChoice(0); //第一个解析出来的字词,解析完全的。
                mFixedLen = mIPinyinDecoderService.imGetFixedLen();//
                
                PinyinLog.d(TAG, "updateDecInfoForSearch(),mFullSent="+mFullSent +"  mFixedLen="+mFixedLen);

                // Update the surface string to the one kept by engine.
                mSurface.replace(0, mSurface.length(), pyStr);//pinyin

                if (mCursorPos > mSurface.length())
                    mCursorPos = mSurface.length();
                
                //显示在ComposintView的字符串。
                mComposingStr = mFullSent.substring(0, mFixedLen)
                        + mSurface.substring(mSplStart[mFixedLen + 1]);
                PinyinLog.d(TAG, "updateDecInfoForSearch(),mComposingStr="+mComposingStr);
                
                mActiveCmpsLen = mComposingStr.length();
                if (mSurfaceDecodedLen > 0) {
                    mActiveCmpsLen = mActiveCmpsLen
                            - (mSurface.length() - mSurfaceDecodedLen);
                }

                // Prepare the display string.
                if (0 == mSurfaceDecodedLen) {
                    mComposingStrDisplay = mComposingStr;
                    mActiveCmpsDisplayLen = mComposingStr.length();
                } else {
                    mComposingStrDisplay = mFullSent.substring(0, mFixedLen);
                    for (int pos = mFixedLen + 1; pos < mSplStart.length - 1; pos++) {
                        mComposingStrDisplay += mSurface.substring(
                                mSplStart[pos], mSplStart[pos + 1]);
                        if (mSplStart[pos + 1] < mSurfaceDecodedLen) {
                            mComposingStrDisplay += " ";
                        }
                    }
                    mActiveCmpsDisplayLen = mComposingStrDisplay.length();
                    if (mSurfaceDecodedLen < mSurface.length()) {
                        mComposingStrDisplay += mSurface
                                .substring(mSurfaceDecodedLen);
                        PinyinLog.d(TAG, "updateDecInfoForSearch(),mComposingStrDisplay="+mComposingStrDisplay);
                    }
                }

                if (mSplStart.length == mFixedLen + 2) {
                    mFinishSelection = true;
                } else {
                    mFinishSelection = false;
                }
            } catch (RemoteException e) {
                Log.w(TAG, "PinyinDecoderService died", e);
            } catch (Exception e) {
                mTotalChoicesNum = 0;
                mComposingStr = "";
            }
            // Prepare page 0.
            if (!mFinishSelection) {
                preparePage(0);
            }
        }

        private void choosePredictChoice(int choiceId) {
            if (ImeState.STATE_PREDICT != mImeState || choiceId < 0
                    || choiceId >= mTotalChoicesNum) {
                return;
            }

            String tmp = mCandidatesList.get(choiceId);

            resetCandidates();

            mCandidatesList.add(tmp);
            mTotalChoicesNum = 1;

            mSurface.replace(0, mSurface.length(), "");
            mCursorPos = 0;
            mFullSent = tmp;
            mFixedLen = tmp.length();
            mComposingStr = mFullSent;
            mActiveCmpsLen = mFixedLen;

            mFinishSelection = true;
        }

        public String getCandidate(int candId) {
            // Only loaded items can be gotten, so we use mCandidatesList.size()
            // instead mTotalChoiceNum.
            if (candId < 0 || candId > mCandidatesList.size()) {
                return null;
            }
            return mCandidatesList.get(candId);
        }

        private void getCandiagtesForCache() {
            int fetchStart = mCandidatesList.size();
            int fetchSize = mTotalChoicesNum - fetchStart;
            if (fetchSize > MAX_PAGE_SIZE_DISPLAY) {
                fetchSize = MAX_PAGE_SIZE_DISPLAY;
            }
            try {
                List<String> newList = null;
                if (ImeState.STATE_INPUT == mImeState ||
                        ImeState.STATE_IDLE == mImeState ||
                        ImeState.STATE_COMPOSING == mImeState) {
                    newList = mIPinyinDecoderService.imGetChoiceList(
                            fetchStart, fetchSize, mFixedLen);
                } else if (ImeState.STATE_PREDICT == mImeState) {
                    newList = mIPinyinDecoderService.imGetPredictList(
                            fetchStart, fetchSize);
                } else if (ImeState.STATE_APP_COMPLETION == mImeState) {
                    newList = new ArrayList<String>();
                    if (null != mAppCompletions) {
                        for (int pos = fetchStart; pos < fetchSize; pos++) {
                            CompletionInfo ci = mAppCompletions[pos];
                            if (null != ci) {
                                CharSequence s = ci.getText();
                                if (null != s)
                                    newList.add(s.toString());
                            }
                        }
                    }
                }
                mCandidatesList.addAll(newList);
            } catch (RemoteException e) {
                Log.w(TAG, "PinyinDecoderService died", e);
            }
        }

        public boolean pageReady(int pageNo) { 
            // If the page number is less than 0, return false
            if (pageNo < 0)
                return false;

            // Page pageNo's ending information is not ready.
            if (mPageStart.size() <= pageNo + 1) {
                return false;
            }

            return true;
        }

        public boolean preparePage(int pageNo) {
            // If the page number is less than 0, return false
            if (pageNo < 0)
                return false;

            // Make sure the starting information for page pageNo is ready.
            if (mPageStart.size() <= pageNo) {
                return false;
            }

            // Page pageNo's ending information is also ready.
            if (mPageStart.size() > pageNo + 1) {
                return true;
            }

            // If cached items is enough for page pageNo.
            if (mCandidatesList.size() - mPageStart.elementAt(pageNo) >= MAX_PAGE_SIZE_DISPLAY) {
                return true;
            }

            // Try to get more items from engine
            getCandiagtesForCache();

            // Try to find if there are available new items to display.
            // If no new item, return false;
            if (mPageStart.elementAt(pageNo) >= mCandidatesList.size()) {
                return false;
            }

            // If there are new items, return true;
            return true;
        }

        public void preparePredicts(CharSequence history) {
            if (null == history)
                return;

            resetCandidates();

            if (Settings.getPrediction()) {
                String preEdit = history.toString();
                int predictNum = 0;
                if (null != preEdit) {
                    try {
                        mTotalChoicesNum = mIPinyinDecoderService
                                .imGetPredictsNum(preEdit);
                    } catch (RemoteException e) {
                        return;
                    }
                }
            }

            preparePage(0);
            mFinishSelection = false;
        }

        private void prepareAppCompletions(CompletionInfo completions[]) {
            resetCandidates();
            mAppCompletions = completions;
            mTotalChoicesNum = completions.length;
            preparePage(0);
            mFinishSelection = false;
            return;
        }

        public int getCurrentPageSize(int currentPage) {
            if (mPageStart.size() <= currentPage + 1)
                return 0;
            return mPageStart.elementAt(currentPage + 1)
                    - mPageStart.elementAt(currentPage);
        }

        public int getCurrentPageStart(int currentPage) {
            if (mPageStart.size() < currentPage + 1)
                return mTotalChoicesNum;
            return mPageStart.elementAt(currentPage);
        }

        public boolean pageForwardable(int currentPage) {
            if (mPageStart.size() <= currentPage + 1)
                return false;
            if (mPageStart.elementAt(currentPage + 1) >= mTotalChoicesNum) {
                return false;
            }
            return true;
        }

        public boolean pageBackwardable(int currentPage) {
            if (currentPage > 0)
                return true;
            return false;
        }

        public boolean charBeforeCursorIsSeparator() {
            int len = mSurface.length();
            if (mCursorPos > len)
                return false;
            if (mCursorPos > 0 && mSurface.charAt(mCursorPos - 1) == '\'') {
                return true;
            }
            return false;
        }

        public int getCursorPos() {
            return mCursorPos;
        }

        public int getCursorPosInCmps() {
            int cursorPos = mCursorPos;
            int fixedLen = 0;

            for (int hzPos = 0; hzPos < mFixedLen; hzPos++) {
                if (mCursorPos >= mSplStart[hzPos + 2]) {
                    cursorPos -= mSplStart[hzPos + 2] - mSplStart[hzPos + 1];
                    cursorPos += 1;
                }
            }
            return cursorPos;
        }

        public int getCursorPosInCmpsDisplay() {
            int cursorPos = getCursorPosInCmps();
            // +2 is because: one for mSplStart[0], which is used for other
            // purpose(The length of the segmentation string), and another
            // for the first spelling which does not need a space before it.
            for (int pos = mFixedLen + 2; pos < mSplStart.length - 1; pos++) {
                if (mCursorPos <= mSplStart[pos]) {
                    break;
                } else {
                    cursorPos++;
                }
            }
            return cursorPos;
        }

        public void moveCursorToEdge(boolean left) {
            if (left)
                mCursorPos = 0;
            else
                mCursorPos = mSurface.length();
        }

        // Move cursor. If offset is 0, this function can be used to adjust
        // the cursor into the bounds of the string.
        public void moveCursor(int offset) {
            if (offset > 1 || offset < -1)
                return;

            if (offset != 0) {
                int hzPos = 0;
                for (hzPos = 0; hzPos <= mFixedLen; hzPos++) {
                    if (mCursorPos == mSplStart[hzPos + 1]) {
                        if (offset < 0) {
                            if (hzPos > 0) {
                                offset = mSplStart[hzPos]
                                        - mSplStart[hzPos + 1];
                            }
                        } else {
                            if (hzPos < mFixedLen) {
                                offset = mSplStart[hzPos + 2]
                                        - mSplStart[hzPos + 1];
                            }
                        }
                        break;
                    }
                }
            }
            mCursorPos += offset;
            if (mCursorPos < 0) {
                mCursorPos = 0;
            } else if (mCursorPos > mSurface.length()) {
                mCursorPos = mSurface.length();
            }
        }

        public int getSplNum() {
            return mSplStart[0];
        }

        public int getFixedLen() {
            return mFixedLen;
        }
    }

    private void chooseAndUpdate(int candId) {
        PinyinLog.d(TAG, "chooseAndUpdate(),candId = " + candId);

        if (!mInputModeSwitcher.isChineseText()) {
            String choice = mDecInfo.getCandidate(candId);
            if (null != choice) {
                commitResultText(choice);
            }
            resetToIdleState(false);
            return;
        }

        if (ImeState.STATE_PREDICT != mImeState) {//打第一字时，状态还在idle.
            // Get result candidate list, if choice_id < 0, do a new decoding.
            // If choice_id >=0, select the candidate, and get the new candidate
            // list.
            PinyinLog.d(TAG, "chooseAndUpdate(), ImeState.STATE_PREDICT != mImeState   mImeState = " + mImeState);
            mDecInfo.chooseDecodingCandidate(candId);
        } else {//选择联想词
            // Choose a prediction item.
            PinyinLog.d(TAG, "chooseAndUpdate(), ImeState.STATE_PREDICT == mImeState");
            mDecInfo.choosePredictChoice(candId);
        }

        if (mDecInfo.getComposingStr().length() > 0) {
            String resultStr;
            resultStr = mDecInfo.getComposingStrActivePart();

            // choiceId >= 0 means user finishes a choice selection.
//            PinyinLog.d(TAG, "chooseAndUpdate(),candId = " + candId);
            PinyinLog.d(TAG, "chooseAndUpdate(),mDecInfo.canDoPrediction() == "+mDecInfo.canDoPrediction());
            if (candId >= 0 && mDecInfo.canDoPrediction()) {
                PinyinLog.d(TAG, "chooseAndUpdate(), candId >= 0 && mDecInfo.canDoPrediction()");
                commitResultText(resultStr);
                mImeState = ImeState.STATE_PREDICT;//联想词汇状态
                if (null != mSkbContainer && mSkbContainer.isShown()) {
                    mSkbContainer.toggleCandidateMode(false);
                }
                // Try to get the prediction list.
                if (Settings.getPrediction()) {
                    InputConnection ic = getCurrentInputConnection();
                    if (null != ic) {
                        CharSequence cs = ic.getTextBeforeCursor(3, 0);
                        if (null != cs) {
                            mDecInfo.preparePredicts(cs);
                        }
                    }
                } else {
                    mDecInfo.resetCandidates();
                }

                if (mDecInfo.mCandidatesList.size() > 0) {
                    showCandidateWindow(false);
                } else {
                    resetToIdleState(false);
                }
            } else {
                if (ImeState.STATE_IDLE == mImeState) {//空闲状态
                    PinyinLog.d(TAG, "chooseAndUpdate(), ImeState.STATE_IDLE == mImeState");
                    if (mDecInfo.getSplStrDecodedLen() == 0) {
                        PinyinLog.d(TAG, "mDecInfo.getSplStrDecodedLen() == 0");
                        changeToStateComposing(true);
                    } else {
                        changeToStateInput(true);
                    }
                } else {//非空闲状态
                    PinyinLog.d(TAG, "chooseAndUpdate(), ImeState.STATE_IDLE != mImeState");
                    if (mDecInfo.selectionFinished()) {
                        PinyinLog.d(TAG, "chooseAndUpdate(), mDecInfo.selectionFinished()");
                        changeToStateComposing(true);
                    }
                }
                PinyinLog.d(TAG, "chooseAndUpdate(),call showCandidateWindow()");
                showCandidateWindow(true);
            }
        } else {
            PinyinLog.d(TAG, "chooseAndUpdate(),call resetToIdleState(fasle)");
            resetToIdleState(false);
        }

    }

    private void simulateKeyEventDownUp(int keyCode) {
        InputConnection ic = getCurrentInputConnection();
        if (null == ic)
            return;

        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
    }

    private void commitResultText(String resultText) {// 把输入法中的字搞到editText中。
        InputConnection ic = getCurrentInputConnection();
        if (null != ic)
            ic.commitText(resultText, 1);
        if (null != mComposingView) {
            mComposingView.setVisibility(View.INVISIBLE);
            mComposingView.invalidate();
        }
    }

    private void inputCommaPeriod(String preEdit, int keyChar,
            boolean dismissCandWindow, ImeState nextState) {
        if (keyChar == ',')
            preEdit += '\uff0c';
        else if (keyChar == '.')
            preEdit += '\u3002';
        else
            return;
        commitResultText(preEdit);
        if (dismissCandWindow)
            resetCandidateWindow();
        mImeState = nextState;
    }

    private void resetToIdleState(boolean resetInlineText) {
        if (ImeState.STATE_IDLE == mImeState)
            return;

        mImeState = ImeState.STATE_IDLE;
        mDecInfo.reset();

        if (null != mComposingView)
            mComposingView.reset();
        if (resetInlineText)
            commitResultText("");
        resetCandidateWindow();
    }

    private boolean processSurfaceChange(int keyChar, int keyCode) {
        if (mDecInfo.isSplStrFull() && KeyEvent.KEYCODE_DEL != keyCode) {
            return true;
        }

        if ((keyChar >= 'a' && keyChar <= 'z')
                || (keyChar == '\'' && !mDecInfo.charBeforeCursorIsSeparator())
                || (((keyChar >= '0' && keyChar <= '9') || keyChar == ' ') && ImeState.STATE_COMPOSING == mImeState)) {
            mDecInfo.addSplChar((char) keyChar, false);
            chooseAndUpdate(-1);
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            mDecInfo.prepareDeleteBeforeCursor();
            chooseAndUpdate(-1);
        }
        return true;
    }

    private void changeToStateComposing(boolean updateUi) {// 组字状态
        mImeState = ImeState.STATE_COMPOSING;
        if (!updateUi)
            return;

        if (null != mSkbContainer && mSkbContainer.isShown()) {
            mSkbContainer.toggleCandidateMode(true);
        }
    }

    private void changeToStateInput(boolean updateUi) {// 输入状态
        mImeState = ImeState.STATE_INPUT;
        if (!updateUi)
            return;

        if (null != mSkbContainer && mSkbContainer.isShown()) {
            mSkbContainer.toggleCandidateMode(true);
        }
        showCandidateWindow(true);
    }

    private void updateComposingText(boolean visible) {
        if (!visible) {
            mComposingView.setVisibility(View.INVISIBLE);
        } else {
            mComposingView.setDecodingInfo(mDecInfo, mImeState);
            mComposingView.setVisibility(View.VISIBLE);
        }
        mComposingView.invalidate();
    }

    // If activeCandNo is less than 0, get the current active candidate number
    // from candidate view, otherwise use activeCandNo.
    private void chooseCandidate(int activeCandNo) {
        if (activeCandNo < 0) {
            activeCandNo = mCandidatesContainer.getActiveCandiatePos();
        }
        if (activeCandNo >= 0) {
            chooseAndUpdate(activeCandNo);
        }
    }

    private boolean tryInputRawUnicode(String str) {
        if (str.length() > 7) {
            if (str.substring(0, 7).compareTo("unicode") == 0) {
                try {
                    String digitStr = str.substring(7);
                    int startPos = 0;
                    int radix = 10;
                    if (digitStr.length() > 2 && digitStr.charAt(0) == '0'
                            && digitStr.charAt(1) == 'x') {
                        startPos = 2;
                        radix = 16;
                    }
                    digitStr = digitStr.substring(startPos);
                    int unicode = Integer.parseInt(digitStr, radix);
                    if (unicode > 0) {
                        char low = (char) (unicode & 0x0000ffff);
                        char high = (char) ((unicode & 0xffff0000) >> 16);
                        commitResultText(String.valueOf(low));
                        if (0 != high) {
                            commitResultText(String.valueOf(high));
                        }
                    }
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            } else if (str.substring(str.length() - 7, str.length()).compareTo(
                    "unicode") == 0) {
                String resultStr = "";
                for (int pos = 0; pos < str.length() - 7; pos++) {
                    if (pos > 0) {
                        resultStr += " ";
                    }

                    resultStr += "0x" + Integer.toHexString(str.charAt(pos));
                }
                commitResultText(String.valueOf(resultStr));
                return true;
            }
        }
        return false;
    }

    private void showCandidateWindow(boolean showComposingView) {
        if (mEnvironment.needDebug()) {
            PinyinLog.d(TAG, "Candidates window is shown. Parent = "
                    + mCandidatesContainer);
        }

        setCandidatesViewShown(true);

        if (null != mSkbContainer)
            mSkbContainer.requestLayout();

        if (null == mCandidatesContainer) {
            resetToIdleState(false);
            return;
        }
        updateComposingText(showComposingView);
        PinyinLog.d(TAG, "showCandidateWindow(), ImeState.STATE_COMPOSING != mImeState"+ (ImeState.STATE_COMPOSING != mImeState));
        mCandidatesContainer.showCandidates(mDecInfo,
                ImeState.STATE_COMPOSING != mImeState);//iii false
        mFloatingWindowTimer.postShowFloatingWindow();
    }

    public void dismissCandidateWindow() {
        if (mEnvironment.needDebug()) {
            PinyinLog.d(TAG, "Candidates window is to be dismissed");
        }
        if (null == mCandidatesContainer)
            return;
        try {
            mFloatingWindowTimer.cancelShowing();
            mFloatingWindow.dismiss();
        } catch (Exception e) {
            Log.e(TAG, "Fail to show the PopupWindow.");
        }
        setCandidatesViewShown(false);

        if (null != mSkbContainer && mSkbContainer.isShown()) {
            mSkbContainer.toggleCandidateMode(false);
        }
    }

    private void resetCandidateWindow() {
        if (mEnvironment.needDebug()) {
            PinyinLog.d(TAG, "Candidates window is to be reset");
        }
        if (null == mCandidatesContainer)
            return;
        try {
            mFloatingWindowTimer.cancelShowing();
            mFloatingWindow.dismiss();
        } catch (Exception e) {
            Log.e(TAG, "Fail to show the PopupWindow.");
        }

        if (null != mSkbContainer && mSkbContainer.isShown()) {
            mSkbContainer.toggleCandidateMode(false);
        }

        mDecInfo.resetCandidates();

        if (null != mCandidatesContainer && mCandidatesContainer.isShown()) {
            showCandidateWindow(false);
        }
    }

    private class PopupTimer extends Handler implements Runnable {
        private int mParentLocation[] = new int[2];

        void postShowFloatingWindow() {
            mFloatingContainer.measure(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            mFloatingWindow.setWidth(mFloatingContainer.getMeasuredWidth());
            mFloatingWindow.setHeight(mFloatingContainer.getMeasuredHeight());
            post(this);
        }

        void cancelShowing() {
            if (mFloatingWindow.isShowing()) {
                mFloatingWindow.dismiss();
            }
            removeCallbacks(this);
        }

        public void run() {
            mCandidatesContainer.getLocationInWindow(mParentLocation);

            if (!mFloatingWindow.isShowing()) {
                mFloatingWindow.showAtLocation(mCandidatesContainer,
                        Gravity.LEFT | Gravity.TOP, mParentLocation[0],
                        mParentLocation[1] - mFloatingWindow.getHeight());
            } else {
                mFloatingWindow
                        .update(mParentLocation[0],
                                mParentLocation[1] - mFloatingWindow.getHeight(),
                                mFloatingWindow.getWidth(),
                                mFloatingWindow.getHeight());
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mEnvironment.needDebug()) {
            PinyinLog.d(TAG, "onDestroy.");
        }
        unbindService(mPinyinDecoderServiceConnection);
        Settings.releaseInstance();
        super.onDestroy();
    }

}
