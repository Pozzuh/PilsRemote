package nl.svia.pilsremote.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import nl.svia.pilsremote.R;

public class AmountPicker extends FrameLayout {
    private int mValue;
    private int mMin;
    private int mMax;

    private ImageView mRemoveView;
    private ImageView mAddView;
    private TextView mNumberView;

    private OnValueChangeListener mListener;

    public AmountPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initAttributes(context, attrs);
        initView(context);
    }

    public AmountPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        initAttributes(context, attrs);
        initView(context);
    }

    public AmountPicker(Context context) {
        super(context);

        mValue = 1;
        mMin = 0;
        mMax = 10;
        initView(context);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AmountPicker,
                0, 0);

        try {
            mValue = a.getInteger(R.styleable.AmountPicker_value, 1);
            mMin = a.getInteger(R.styleable.AmountPicker_min, 0);
            mMax = a.getInteger(R.styleable.AmountPicker_max, 10);
        } finally {
            a.recycle();
        }
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.amount_picker, null);
        mRemoveView = (ImageView) view.findViewById(R.id.remove_button);
        mAddView = (ImageView) view.findViewById(R.id.add_button);
        mNumberView = (TextView) view.findViewById(R.id.number);

        if (mNumberView == null)
            Log.d("AmountPicker", "null");

        mRemoveView.setClickable(true);
        mAddView.setClickable(true);

        mRemoveView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setValue(mValue - 1);
            }
        });

        mAddView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setValue(mValue + 1);
            }
        });

        setValueInternal(mValue, false);

        addView(view);
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int v) {
        setValueInternal(v, true);
    }

    private void setValueInternal(int v, boolean callListener) {
        if (v < mMin) {
            v = mMin;
        } else if (v > mMax) {
            v = mMax;
        }

        mValue = v;

        mNumberView.setText(String.valueOf(mValue));

        invalidate();
        requestLayout();

        if (callListener && mListener != null) {
            mListener.onValueChanged(mValue);
        }
    }

    public int getMin() {
        return mMin;
    }

    public int getMax() {
        return mMax;
    }

    public interface OnValueChangeListener {
        void onValueChanged(int value);
    }
}
