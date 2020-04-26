package com.example.startproject2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {
    ImageView imageView;
    TextView textView;
    Handler handler = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() { // 인트로 액티비티를 끝내고 main 액티비티로 전환
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        imageView = findViewById(R.id.imageView2);
        // 강원대 로고
        textView = findViewById(R.id.textView5);
        // 글자

        imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
        // 로고에 1초동안 360도 회전하는 애니메이션 설정
        textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        // 글자에 1초동안 페이드인 하는 애니메이션 설정
        handler.postDelayed(r,2000);
        // 2초 뒤에 main액티비티로 전환
    }

    @Override
    public void onBackPressed() {
        // 취소버튼 눌렸을 경우
        super.onBackPressed();
        handler.removeCallbacks(r);
    }
}
