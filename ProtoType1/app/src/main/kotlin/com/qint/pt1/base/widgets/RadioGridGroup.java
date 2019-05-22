/*
 * Author: Matthew Zhang
 * Created on: 4/11/19 10:32 AM
 * Copyright: (c) 2019. QINT.TV
 * 参考：
 * 1. https://gist.github.com/saiaspire/a73135cfee1110a64cb0ab3451b6ca33
 * 2. https://www.jianshu.com/p/b2ad0140ee8a
 */

package com.qint.pt1.base.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;

import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.gridlayout.widget.GridLayout;

/**
 * <p>This class is used to create a multiple-exclusion scope for a set of radio
 * buttons. Checking one radio button that belongs to a radio group unchecks
 * any previously checked radio button within the same group.</p>
 * <p/>
 * <p>Intially, all of the radio buttons are unchecked. While it is not possible
 * to uncheck a particular radio button, the radio group can be cleared to
 * remove the checked state.</p>
 * <p/>
 * <p>The selection is identified by the unique id of the radio button as defined
 * in the XML layout file.</p>
 * <p/>
 * <p>See
 * {@link android.widget.GridLayout.LayoutParams GridLayout.LayoutParams}
 * for layout attributes.</p>
 *
 * @see AppCompatRadioButton
 */
public class RadioGridGroup extends GridLayout {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private int mCheckedId = -1;
    private CompoundButton.OnCheckedChangeListener mChildOnCheckedChangeListener;
    private boolean mProtectFromCheckedChange = false;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private PassThroughHierarchyChangeListener mPassThroughListener;

    public RadioGridGroup(Context context) {
        super(context);
        init();
    }

    public RadioGridGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mChildOnCheckedChangeListener = new CheckedStateTracker();
        mPassThroughListener = new PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(mPassThroughListener);
    }

    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mPassThroughListener.mOnHierarchyChangeListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (mCheckedId != -1) {
            mProtectFromCheckedChange = true;
            setCheckedStateForView(mCheckedId, true);
            mProtectFromCheckedChange = false;
            setCheckedId(mCheckedId);
        }
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        setViewState(child);
        super.addView(child, index, params);
    }

    private void setViewState(View child) {
        if (child instanceof AppCompatRadioButton) {
            final AppCompatRadioButton button = (AppCompatRadioButton) child;
            if (button.isChecked()) {
                mProtectFromCheckedChange = true;
                if (mCheckedId != -1) {
                    setCheckedStateForView(mCheckedId, false);
                }
                mProtectFromCheckedChange = false;
                setCheckedId(button.getId());
            }else if (child instanceof ViewGroup) {
                ViewGroup view = (ViewGroup) child;
                for (int i = 0; i < view.getChildCount(); i++) {
                    setViewState(view.getChildAt(i));
                }
            }
        }
    }

    public void check(int id) {
        if (id != -1 && (id == mCheckedId)) {
            return;
        }

        if (mCheckedId != -1) {
            setCheckedStateForView(mCheckedId, false);
        }

        if (id != -1) {
            setCheckedStateForView(id, true);
        }

        setCheckedId(id);
    }

    private void setCheckedId(int id) {
        mCheckedId = id;
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
        }
    }

    private void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = findViewById(viewId);
        if (checkedView != null && checkedView instanceof AppCompatRadioButton) {
            ((AppCompatRadioButton) checkedView).setChecked(checked);
        }
    }

    public int getCheckedCheckableImageButtonId() {
        return mCheckedId;
    }

    public void clearCheck() {
        check(-1);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    @Override
    public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(RadioGridGroup.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(RadioGridGroup.class.getName());
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(RadioGridGroup group, int checkedId);
    }

    private class CheckedStateTracker implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mProtectFromCheckedChange) {
                return;
            }

            mProtectFromCheckedChange = true;
            if (mCheckedId != -1) {
                setCheckedStateForView(mCheckedId, false);
            }
            mProtectFromCheckedChange = false;

            int id = buttonView.getId();
            setCheckedId(id);
        }
    }

    private class PassThroughHierarchyChangeListener implements
            ViewGroup.OnHierarchyChangeListener {
        private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;

        public void onChildViewAdded(View parent, View child) {
            /*
            if (parent == RadioGridGroup.this && child instanceof AppCompatRadioButton) {
                int id = child.getId();
                // generates an id if it's missing
                if (id == View.NO_ID) {
                    id = generateViewId();
                    child.setId(id);
                }
                ((AppCompatRadioButton) child).setOnCheckedChangeListener(
                        mChildOnCheckedChangeListener);
            }
            */

            setListener(child);
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        public void onChildViewRemoved(View parent, View child) {
            /*
            if (parent == RadioGridGroup.this && child instanceof AppCompatRadioButton) {
                ((AppCompatRadioButton) child).setOnCheckedChangeListener(null);
            }
            */

            removeListener(child);
            if (mOnHierarchyChangeListener != null) {
                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }

        private void setListener(View child) {
            if (child instanceof AppCompatRadioButton) {
                int id = child.getId();
                // generates an id if it's missing
                if (id == View.NO_ID) {
                    id = generateViewId();
                    child.setId(id);
                }
                ((AppCompatRadioButton) child).setOnCheckedChangeListener(
                        mChildOnCheckedChangeListener);
            } else if (child instanceof ViewGroup) {
                ViewGroup view = (ViewGroup) child;
                for (int i = 0; i < view.getChildCount(); i++) {
                    setListener(view.getChildAt(i));
                }
            }
        }

        private void removeListener(View child) {
            if (child instanceof AppCompatRadioButton) {
                ((AppCompatRadioButton) child).setOnCheckedChangeListener(null);
            } else if (child instanceof ViewGroup) {
                ViewGroup view = (ViewGroup) child;
                for (int i = 0; i < view.getChildCount(); i++) {
                    removeListener(view.getChildAt(i));
                }
            }
        }

    }

    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();

            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.

            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
}