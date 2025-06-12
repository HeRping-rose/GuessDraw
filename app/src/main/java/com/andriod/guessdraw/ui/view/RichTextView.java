package com.andriod.guessdraw.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class RichTextView extends View {

    public RichTextView(Context context) {
        super(context);
    }

    public RichTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RichTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RichTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public enum Alignment {
        START(0), CENTER(1), END(2) ;
        private final int value;
        Alignment(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

        public static Alignment getAlignment(int value) {
            for (Alignment alignment : Alignment.values()) {
                if (alignment.value == value) {
                    return alignment;
                }
            }
            // throw new IllegalArgumentException("Invalid value for Alignment: " + value);
            return Alignment.START;

        }
    }
    public enum Gravity {
        // 静态类对象 TOP
        TOP(0), CENTER(1), BOTTOM(2);
        private final int value;
        Gravity(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public static Gravity getGravity(int value) {
            for (Gravity gravity : Gravity.values()) {
                if (gravity.value == value) {
                    return gravity;
                }
            }
            // throw new IllegalArgumentException("Invalid value for Gravity: " + value);
            return Gravity.TOP;
        }

    }
}
