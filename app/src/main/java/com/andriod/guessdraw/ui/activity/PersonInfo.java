package com.andriod.guessdraw.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.andriod.guessdraw.databinding.ActivityPersonInfoBinding;

public class PersonInfo extends AppCompatActivity {
    private ActivityPersonInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonInfoBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

    }
}