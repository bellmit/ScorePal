package uk.co.darkerwaters.scorepal.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class IndicatorRadioGroup extends LinearLayout {
    private int checkedId;
    private boolean protectFromCheckedChange;
    private IndicatorRadioGroup.PassThroughHierarchyChangeListener passThroughListener;
    private CheckableIndicatorButton.OnCheckedChangeListener childOnCheckedChangeListener;

    public IndicatorRadioGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorRadioGroup(Context context) {
        this(context, (AttributeSet)null, 0);
    }

    public IndicatorRadioGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.checkedId = -1;
        this.childOnCheckedChangeListener = new IndicatorRadioGroup.CheckedStateTracker();
        this.passThroughListener = new IndicatorRadioGroup.PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(this.passThroughListener);
    }

    public void addView(View child, int index, LayoutParams params) {
        if (child instanceof RadioIndicatorButton && ((RadioIndicatorButton)child).isChecked()) {
            this.protectFromCheckedChange = true;
            if (this.checkedId != -1) {
                this.setCheckedStateForView(this.checkedId, false);
            }
            this.protectFromCheckedChange = false;
            this.setCheckedId(child.getId());
        }
        super.addView(child, index, params);
    }

    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        if (this.passThroughListener != null) {
            this.passThroughListener.setOnHierarchyChangeListener(listener);
        }

    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (this.checkedId != -1) {
            this.protectFromCheckedChange = true;
            this.setCheckedStateForView(this.checkedId, true);
            this.protectFromCheckedChange = false;
            this.setCheckedId(this.checkedId);
        }

    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).setEnabled(enabled);
        }
    }

    public final void setCheckedStateForView(int viewId, boolean checked) {
        View checkedView = this.findViewById(viewId);
        if (checkedView != null && checkedView instanceof RadioIndicatorButton) {
            ((RadioIndicatorButton)checkedView).setChecked(checked);
        }

    }

    private void setCheckedId(int id) {
        this.checkedId = id;
    }

    private final class CheckedStateTracker implements CheckableIndicatorButton.OnCheckedChangeListener {
        public CheckedStateTracker() {
        }
        public void onCheckedChanged(View view, boolean isChecked) {
            if (!IndicatorRadioGroup.this.protectFromCheckedChange) {
                IndicatorRadioGroup.this.protectFromCheckedChange = true;
                if (IndicatorRadioGroup.this.checkedId != -1) {
                    IndicatorRadioGroup.this.setCheckedStateForView(IndicatorRadioGroup.this.checkedId, false);
                }
                IndicatorRadioGroup.this.protectFromCheckedChange = false;
                IndicatorRadioGroup.this.setCheckedId(view.getId());
            }
        }
    }

    private final class PassThroughHierarchyChangeListener implements OnHierarchyChangeListener {
        private OnHierarchyChangeListener onHierarchyChangeListener;

        public PassThroughHierarchyChangeListener() {
        }

        public void setOnHierarchyChangeListener(OnHierarchyChangeListener var1) {
            this.onHierarchyChangeListener = var1;
        }

        public void onChildViewAdded(View parent, View child) {
            if (parent == IndicatorRadioGroup.this && child instanceof RadioIndicatorButton) {
                if (child.getId() == -1) {
                    child.setId(View.generateViewId());
                }
                if (IndicatorRadioGroup.this.childOnCheckedChangeListener != null) {
                    ((RadioIndicatorButton)child).addOnCheckChangeListener(IndicatorRadioGroup.this.childOnCheckedChangeListener);
                }
            }
            if (this.onHierarchyChangeListener != null) {
                this.onHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        public void onChildViewRemoved(View parent, View child) {
            if (parent == IndicatorRadioGroup.this && child instanceof RadioIndicatorButton) {
                if (IndicatorRadioGroup.this.childOnCheckedChangeListener != null) {
                    ((RadioIndicatorButton)child).removeOnCheckChangeListener(IndicatorRadioGroup.this.childOnCheckedChangeListener);
                }
            }
            if (this.onHierarchyChangeListener != null) {
                this.onHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }
}
