package com.foolingdutchman.simplefoldabletextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.foolingdutchman.library.R;

import java.lang.reflect.Field;

/**
 * Created by hehao on 2016/12/11.
 */

public class SimpleFoldableTextView extends TextView {
    public static final int STATE_FOLD = 0;
    public static final int STATE_EXPAND = 1;

    private static final String CLASS_NAME_VIEW = "android.view.View";
    private static final String CLASS_NAME_LISTENER_INFO = "android.view.View$ListenerInfo";
    private static final String ELLIPSIS_HINT = "..";
    private static final String GAP_TO_EXPAND_HINT = " ";
    private static final String GAP_TO_FOLD_HINT = " ";
    private static final int MAX_LINES_ON_FOLD = 3;
    private static final int TO_EXPAND_HINT_COLOR = 0xFF3498DB;
    private static final int TO_FOLD_HINT_COLOR = 0xFFE74C3C;
    private static final int TO_EXPAND_HINT_COLOR_BG_PRESSED = 0x55999999;
    private static final int TO_FOLD_HINT_COLOR_BG_PRESSED = 0x55999999;
    private static final boolean TOGGLE_ENABLE = true;
    private static final boolean SHOW_TO_EXPAND_HINT = true;
    private static final boolean SHOW_TO_FOLD_HINT = true;
    private static final float FOLD_HINT_RELATIVE_SIZE=0.8f;
    private static final float EXPAND_HINT_RELATIVE_SIZE=0.8f;
    private String mEllipsisHint;
    private String mToExpandHint;
    private String mToFoldHint;
    private String mGapToExpandHint = GAP_TO_EXPAND_HINT;
    private String mGapToFoldHint = GAP_TO_FOLD_HINT;
    private boolean mToggleEnable = TOGGLE_ENABLE;
    private boolean mShowToExpandHint = SHOW_TO_EXPAND_HINT;
    private boolean mShowToFoldHint = SHOW_TO_FOLD_HINT;
    private int mMaxLinesOnFold = MAX_LINES_ON_FOLD;
    private int mToExpandHintColor = TO_EXPAND_HINT_COLOR;
    private int mToFoldHintColor = TO_FOLD_HINT_COLOR;
    private int mToExpandHintColorBgPressed = TO_EXPAND_HINT_COLOR_BG_PRESSED;
    private int mToFoldHintColorBgPressed = TO_FOLD_HINT_COLOR_BG_PRESSED;
    private int mCurrState = STATE_FOLD;
    private float mFoldHintRelativeSize=FOLD_HINT_RELATIVE_SIZE;
    private float mExpandHintRelativeSize=EXPAND_HINT_RELATIVE_SIZE;
    //  used to add to the tail of modified text, the "shrink" and "expand" text
    private TouchableSpan mTouchableSpan;
    private BufferType mBufferType = BufferType.NORMAL;
    private TextPaint mTextPaint;
    private Layout mLayout;
    private int mTextLineCount = -1;
    private int mLayoutWidth = 0;
    private int mFutureTextViewWidth = 0;

    //  the original text of this view
    private CharSequence mOrigText;

    //  used to judge if the listener of corresponding to the onclick event of ExpandableTextView
    //  is specifically for inner toggle
    private SimpleFoldableClickListener mExpandableClickListener;
    private OnExpandListener mOnExpandListener;

    public SimpleFoldableTextView(Context context) {
        super(context);
        init();
    }

    public SimpleFoldableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context,attrs);
        init();
    }

    public SimpleFoldableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context,attrs);
        init();
    }

    private void initAttr(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleFoldableTextView);
        if (a == null) {
            return;
        }
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.SimpleFoldableTextView_maxLines_onFold) {
                mMaxLinesOnFold = a.getInteger(attr, MAX_LINES_ON_FOLD);
            }else if (attr == R.styleable.SimpleFoldableTextView_EllipsisHint){
                mEllipsisHint = a.getString(attr);
            }else if (attr == R.styleable.SimpleFoldableTextView_expandHint_text) {
                mToExpandHint = a.getString(attr);
            }else if (attr == R.styleable.SimpleFoldableTextView_foldHint_text) {
                mToFoldHint = a.getString(attr);
            }else if (attr == R.styleable.SimpleFoldableTextView_EnableToggle) {
                mToggleEnable = a.getBoolean(attr, TOGGLE_ENABLE);
            }else if (attr == R.styleable.SimpleFoldableTextView_ToExpandHintShow){
                mShowToExpandHint = a.getBoolean(attr, SHOW_TO_EXPAND_HINT);
            }else if (attr == R.styleable.SimpleFoldableTextView_ToFoldHintShow){
                mShowToFoldHint = a.getBoolean(attr, SHOW_TO_FOLD_HINT);
            }else if (attr == R.styleable.SimpleFoldableTextView_expandHint_color){
                mToExpandHintColor = a.getInteger(attr, TO_EXPAND_HINT_COLOR);
            }else if (attr == R.styleable.SimpleFoldableTextView_foldHint_color){
                mToFoldHintColor = a.getInteger(attr, TO_FOLD_HINT_COLOR);
            }else if (attr == R.styleable.SimpleFoldableTextView_ToExpandHintColorBgPressed){
                mToExpandHintColorBgPressed = a.getInteger(attr, TO_EXPAND_HINT_COLOR_BG_PRESSED);
            }else if (attr == R.styleable.SimpleFoldableTextView_ToFoldHintColorBgPressed){
                mToFoldHintColorBgPressed = a.getInteger(attr, TO_FOLD_HINT_COLOR_BG_PRESSED);
            }else if (attr == R.styleable.SimpleFoldableTextView_InitState){
                mCurrState = a.getInteger(attr, STATE_FOLD);
            } else if (attr == R.styleable.SimpleFoldableTextView_GapToExpandHint) {
                mGapToExpandHint = a.getString(attr);
            } else if (attr == R.styleable.SimpleFoldableTextView_GapToFoldHint) {
                mGapToFoldHint = a.getString(attr);
            } else if (attr==R.styleable.SimpleFoldableTextView_foldHint_RelativeSize) {
                mFoldHintRelativeSize=a.getFloat(attr,FOLD_HINT_RELATIVE_SIZE);
            } else if (attr==R.styleable.SimpleFoldableTextView_expandHint_RelativeSize) {
                mExpandHintRelativeSize=a.getFloat(attr,EXPAND_HINT_RELATIVE_SIZE);
            }
        }
        a.recycle();
    }

    private void init() {
        mTouchableSpan = new TouchableSpan();
        setMovementMethod(new LinkTouchMovementMethod());
        if(TextUtils.isEmpty(mEllipsisHint)) {
            mEllipsisHint = ELLIPSIS_HINT;
        }
        if(TextUtils.isEmpty(mToExpandHint)){
            mToExpandHint = getResources().getString(R.string.to_Expand_hint);
        }
        if(TextUtils.isEmpty(mToFoldHint)){
            mToFoldHint = getResources().getString(R.string.to_Fold_hint);
        }
        if(mToggleEnable){
            mExpandableClickListener = new SimpleFoldableClickListener();
            setOnClickListener(mExpandableClickListener);
        }
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
                setTextInternal(getNewTextByConfig(), mBufferType);
            }
        });
    }

    /**
     * used in ListView or RecyclerView to update ExpandableTextView
     * @param text
     *          original text
     * @param futureTextViewWidth
     *          the width of ExpandableTextView in px unit,
     *          used to get max line number of original text by given the width
     * @param expandState
     *          expand or shrink
     */
    public void updateForRecyclerView(CharSequence text, int futureTextViewWidth, int expandState){
        mFutureTextViewWidth = futureTextViewWidth;
        mCurrState = expandState;
        setText(text);
    }

    public void updateForRecyclerView(CharSequence text, BufferType type, int futureTextViewWidth){
        mFutureTextViewWidth = futureTextViewWidth;
        setText(text, type);
    }

    public void updateForRecyclerView(CharSequence text, int futureTextViewWidth){
        mFutureTextViewWidth = futureTextViewWidth;
        setText(text);
    }

    /**
     * get the current state of ExpandableTextView
     * @return
     *      STATE_FOLD if in shrink state
     *      STATE_EXPAND if in expand state
     */
    public int getExpandState(){
        return mCurrState;
    }

    /**
     * refresh and get a will-be-displayed text by current configuration
     * @return
     *      get a will-be-displayed text
     */
    private CharSequence getNewTextByConfig(){
        if(TextUtils.isEmpty(mOrigText)){
            return mOrigText;
        }

        mLayout = getLayout();
        if(mLayout != null){
            mLayoutWidth = mLayout.getWidth();
        }

        if(mLayoutWidth <= 0){
            if(getWidth() == 0) {
                if (mFutureTextViewWidth == 0) {
                    return mOrigText;
                } else {
                    mLayoutWidth = mFutureTextViewWidth - getPaddingLeft() - getPaddingRight();
                }
            }else{
                mLayoutWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            }
        }

        mTextPaint = getPaint();

        mTextLineCount = -1;
        switch (mCurrState){
            case STATE_FOLD: {
                mLayout = new DynamicLayout(mOrigText, mTextPaint, mLayoutWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                mTextLineCount = mLayout.getLineCount();

                if (mTextLineCount <= mMaxLinesOnFold) {
                    return mOrigText;
                }
                int indexEnd = getValidLayout().getLineEnd(mMaxLinesOnFold - 1);
                int indexStart = getValidLayout().getLineStart(mMaxLinesOnFold - 1);
                int indexEndTrimmed = indexEnd
                        - getLengthOfString(mEllipsisHint)
                        - (mShowToExpandHint ? getLengthOfString(mToExpandHint) + getLengthOfString(mGapToExpandHint) : 0);
                if (indexEndTrimmed <= 0) {
                    return mOrigText.subSequence(0, indexEnd);
                }

                int remainWidth = getValidLayout().getWidth() -
                        (int) (mTextPaint.measureText(mOrigText.subSequence(indexStart, indexEndTrimmed).toString()) + 0.5);
                float widthTailReplaced = mTextPaint.measureText(getContentOfString(mEllipsisHint)
                        + (mShowToExpandHint ? (getContentOfString(mToExpandHint) + getContentOfString(mGapToExpandHint)) : ""));

                int indexEndTrimmedRevised = indexEndTrimmed;
                if (remainWidth > widthTailReplaced) {
                    int extraOffset = 0;
                    int extraWidth = 0;
                    while (remainWidth > widthTailReplaced + extraWidth) {
                        extraOffset++;
                        if (indexEndTrimmed + extraOffset <= mOrigText.length()) {
                            extraWidth = (int) (mTextPaint.measureText(
                                    mOrigText.subSequence(indexEndTrimmed, indexEndTrimmed + extraOffset).toString()) + 0.5);
                        } else {
                            break;
                        }
                    }
                    indexEndTrimmedRevised += extraOffset - 1;
                } else {
                    int extraOffset = 0;
                    int extraWidth = 0;
                    while (remainWidth + extraWidth < widthTailReplaced) {
                        extraOffset--;
                        if (indexEndTrimmed + extraOffset > indexStart) {
                            extraWidth = (int) (mTextPaint.measureText(mOrigText.subSequence(indexEndTrimmed + extraOffset, indexEndTrimmed).toString()) + 0.5);
                        } else {
                            break;
                        }
                    }
                    indexEndTrimmedRevised += extraOffset;
                }

                SpannableStringBuilder ssbFold = new SpannableStringBuilder(mOrigText, 0, indexEndTrimmedRevised)
                        .append(mEllipsisHint);
                if (mShowToExpandHint) {
                    ssbFold.append(getContentOfString(mGapToExpandHint) + getContentOfString(mToExpandHint));
                    ssbFold.setSpan(mTouchableSpan, ssbFold.length() - getLengthOfString(mToExpandHint), ssbFold.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ssbFold.setSpan(new RelativeSizeSpan(mExpandHintRelativeSize), ssbFold.length() - getLengthOfString(mToExpandHint), ssbFold.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                }
                return ssbFold;
            }
            case STATE_EXPAND: {
                if (!mShowToFoldHint) {
                    return mOrigText;
                }
                mLayout = new DynamicLayout(mOrigText, mTextPaint, mLayoutWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                mTextLineCount = mLayout.getLineCount();

                if (mTextLineCount <= mMaxLinesOnFold) {
                    return mOrigText;
                }

                SpannableStringBuilder ssbExpand = new SpannableStringBuilder(mOrigText)
                        .append(mGapToFoldHint).append(mToFoldHint);
                ssbExpand.setSpan(mTouchableSpan, ssbExpand.length() - getLengthOfString(mToFoldHint), ssbExpand.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssbExpand.setSpan(new RelativeSizeSpan(mFoldHintRelativeSize), ssbExpand.length() - getLengthOfString(mToFoldHint), ssbExpand.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return ssbExpand;
            }
        }
        return mOrigText;
    }

    public void setExpandListener(OnExpandListener listener){
        mOnExpandListener = listener;
    }

    private Layout getValidLayout(){
        return mLayout != null ? mLayout : getLayout();
    }

    private void toggle(){
        switch (mCurrState){
            case STATE_FOLD:
                mCurrState = STATE_EXPAND;
                if(mOnExpandListener != null){
                    mOnExpandListener.onExpand(this);
                }
                break;
            case STATE_EXPAND:
                mCurrState = STATE_FOLD;
                if(mOnExpandListener != null){
                    mOnExpandListener.onFold(this);
                }
                break;
        }
        setTextInternal(getNewTextByConfig(), mBufferType);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        mOrigText = text;
        mBufferType = type;
        setTextInternal(getNewTextByConfig(), type);
    }

    private void setTextInternal(CharSequence text, BufferType type){
        super.setText(text, type);
    }

    private int getLengthOfString(String string){
        if(string == null)
            return 0;
        return string.length();
    }

    private String getContentOfString(String string){
        if(string == null)
            return "";
        return string;
    }

    public interface OnExpandListener{
        void onExpand(SimpleFoldableTextView view);
        void onFold(SimpleFoldableTextView view);
    }

    private class SimpleFoldableClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            toggle();
        }
    }

    public View.OnClickListener getOnClickListener(View view) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getOnClickListenerV14(view);
        } else {
            return getOnClickListenerV(view);
        }
    }

    private View.OnClickListener getOnClickListenerV(View view) {
        View.OnClickListener retrievedListener = null;
        try {
            Field field = Class.forName(CLASS_NAME_VIEW).getDeclaredField("mOnClickListener");
            field.setAccessible(true);
            retrievedListener = (View.OnClickListener) field.get(view);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retrievedListener;
    }

    private View.OnClickListener getOnClickListenerV14(View view) {
        View.OnClickListener retrievedListener = null;
        try {
            Field listenerField = Class.forName(CLASS_NAME_VIEW).getDeclaredField("mListenerInfo");
            Object listenerInfo = null;

            if (listenerField != null) {
                listenerField.setAccessible(true);
                listenerInfo = listenerField.get(view);
            }

            Field clickListenerField = Class.forName(CLASS_NAME_LISTENER_INFO).getDeclaredField("mOnClickListener");

            if (clickListenerField != null && listenerInfo != null) {
                clickListenerField.setAccessible(true);
                retrievedListener = (View.OnClickListener) clickListenerField.get(listenerInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retrievedListener;
    }


    /**
     * Copy from:
     *  http://stackoverflow.com/questions
     *  /20856105/change-the-text-color-of-a-single-clickablespan-when-pressed-without-affecting-o
     * By:
     *  Steven Meliopoulos
     */
    private class TouchableSpan extends ClickableSpan {
        private boolean mIsPressed;
        public void setPressed(boolean isSelected) {
            mIsPressed = isSelected;
        }

        @Override
        public void onClick(View widget) {
            if(hasOnClickListeners()
                    && (getOnClickListener(SimpleFoldableTextView.this) instanceof SimpleFoldableClickListener)) {
            }else{
                toggle();
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            switch (mCurrState){
                case STATE_FOLD:
                    ds.setColor(mToExpandHintColor);
                    ds.bgColor = mIsPressed ? mToExpandHintColorBgPressed : 0;
                    break;
                case STATE_EXPAND:
                    ds.setColor(mToFoldHintColor);
                    ds.bgColor = mIsPressed ? mToFoldHintColorBgPressed : 0;
                    break;
            }
            ds.setUnderlineText(false);
        }
    }

    /**
     * Copy from:
     *  http://stackoverflow.com/questions
     *  /20856105/change-the-text-color-of-a-single-clickablespan-when-pressed-without-affecting-o
     * By:
     *  Steven Meliopoulos
     */
    public class LinkTouchMovementMethod extends LinkMovementMethod {
        private TouchableSpan mPressedSpan;

        @Override
        public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPressedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(true);
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                            spannable.getSpanEnd(mPressedSpan));
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                TouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                    mPressedSpan.setPressed(false);
                    mPressedSpan = null;
                    Selection.removeSelection(spannable);
                }
            } else {
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(false);
                    super.onTouchEvent(textView, spannable, event);
                }
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            }
            return true;
        }

        private TouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            TouchableSpan[] link = spannable.getSpans(off, off, TouchableSpan.class);
            TouchableSpan touchedSpan = null;
            if (link.length > 0) {
                touchedSpan = link[0];
            }
            return touchedSpan;
        }
    }

    public void setExpandHintRelativeSize(float expandHintRelativeSize) {
        mExpandHintRelativeSize = expandHintRelativeSize;
        invalidate();
    }

    public void setFoldHintRelativeSize(float foldHintRelativeSize) {
        mFoldHintRelativeSize = foldHintRelativeSize;
        invalidate();
    }

    public void setToFoldHintColor(int toFoldHintColor) {
        mToFoldHintColor = toFoldHintColor;
        invalidate();
    }

    public void setToExpandHintColor(int toExpandHintColor) {
        mToExpandHintColor = toExpandHintColor;
        invalidate();
    }

    public void setMaxLinesOnFold(int maxLinesOnFold) {
        mMaxLinesOnFold = maxLinesOnFold;
        invalidate();
    }

    public void setToExpandHint(String toExpandHint) {
        mToExpandHint = toExpandHint;
        invalidate();
    }

    public void setToFoldHint(String toFoldHint) {
        mToFoldHint = toFoldHint;
        invalidate();
    }
}
