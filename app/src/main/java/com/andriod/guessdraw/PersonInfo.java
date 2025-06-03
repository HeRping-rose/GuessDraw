package com.andriod.guessdraw;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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