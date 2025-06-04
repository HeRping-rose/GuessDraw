package com.andriod.guessdraw.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.andriod.guessdraw.R;
import com.andriod.guessdraw.databinding.ActivityVerificationBinding;

public class Verification extends AppCompatActivity {
    private EditText[] editTexts;
    private ActivityVerificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化6个EditText
        editTexts = new EditText[6];
        editTexts[0] = binding.et1;
        editTexts[1] = binding.et2;
        editTexts[2] = binding.et3;
        editTexts[3] = binding.et4;
        editTexts[4] = binding.et5;
        editTexts[5] = binding.et6;

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        // 设置验证码输入框的输入监听
        for (int i = 0; i < editTexts.length; i++) {
            final int index = i;
            editTexts[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            editTexts[i].setImeOptions(EditorInfo.IME_ACTION_DONE);
            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < 5) {
                        editTexts[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        editTexts[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // 只保留首位字符（防止粘贴多个）
                    if (s.length() > 1) {
                        editTexts[index].setText(String.valueOf(s.charAt(0)));
                        editTexts[index].setSelection(1);
                    }
                }
            });
        }

        // 自动跳转逻辑：输入6位后自动校验
        for (int i = 0; i < editTexts.length; i++) {
            final int index = i;
            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (index == 5 && s.length() == 1) {
                        // 检查是否全部输入
                        boolean allFilled = true;
                        for (EditText et : editTexts) {
                            if (et.getText().toString().trim().isEmpty()) {
                                allFilled = false;
                                break;
                            }
                        }
                        if (allFilled) {
                            binding.btnContainer.performClick();
                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        binding.btnContainer.setOnClickListener(v -> {
            StringBuilder code = new StringBuilder();
            boolean hasEmpty = false;
            for (EditText et : editTexts) {
                String txt = et.getText().toString().trim();
                if (txt.isEmpty()) {
                    et.setError("请输入验证码");
                    setEditTextBorder(et, R.color.red);
                    hasEmpty = true;
                } else {
                    setEditTextBorder(et, R.color.green);
                }
                code.append(txt);
            }
            if (hasEmpty) return;
            String finalCode = code.toString();
            // 获取模拟验证码（通过Intent传递）
            String correctCode = getIntent().getStringExtra("mock_code");
            if (correctCode == null) correctCode = "123456"; // 默认模拟验证码
            if (finalCode.equals(correctCode)) {
                for (EditText et : editTexts) setEditTextBorder(et, R.color.green);
                startActivity(new Intent(Verification.this, PersonInfo.class));
                finish();
            } else {
                for (EditText et : editTexts) {
                    setEditTextBorder(et, R.color.red);
                    et.setText("");
                }
                editTexts[0].requestFocus();
                Toast.makeText(Verification.this, "验证码错误", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置60秒倒计时
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                binding.tvTimer.setText(String.valueOf(millisUntilFinished / 1000) + "s");
            }

            public void onFinish() {
                binding.tvTimer.setText("重新发送验证码");
                binding.textView4.setText("");
                // 可在此处添加倒计时结束后的操作
            }
        }.start();
    }

    // 设置EditText边框颜色
    private void setEditTextBorder(EditText et, int colorRes) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(12);
        drawable.setStroke(3, ContextCompat.getColor(this, colorRes));
        et.setBackground(drawable);
    }
}
