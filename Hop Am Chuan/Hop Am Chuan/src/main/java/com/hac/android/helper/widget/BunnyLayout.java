package com.hac.android.helper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Custom class using for handle event
 * Created by ThaoHQSE60963 on 1/17/14.
 */
public class BunnyLayout extends FrameLayout {

    private ArrayList<IKeyboardChanged> keyboardListener = new ArrayList<IKeyboardChanged>();

    public BunnyLayout(Context context) {
        super(context);
    }

    public BunnyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BunnyLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addKeyboardStateChangedListener(IKeyboardChanged listener) {
        keyboardListener.add(listener);
    }

    public void removeKeyboardStateChangedListener(IKeyboardChanged listener) {
        keyboardListener.remove(listener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        if (actualHeight > proposedheight) {
            notifyKeyboardShown();
        } else if (actualHeight < proposedheight) {
            notifyKeyboardHidden();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void notifyKeyboardHidden() {
        for (IKeyboardChanged listener : keyboardListener) {
            listener.onKeyboardHidden();
        }
    }

    private void notifyKeyboardShown() {
        for (IKeyboardChanged listener : keyboardListener) {
            listener.onKeyboardShown();
        }
    }

    public interface IKeyboardChanged {
        void onKeyboardShown();
        void onKeyboardHidden();
    }
}

