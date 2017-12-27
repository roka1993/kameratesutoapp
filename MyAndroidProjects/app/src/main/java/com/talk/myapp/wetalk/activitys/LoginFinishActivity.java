package com.talk.myapp.wetalk.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.talk.myapp.wetalk.R;

public class LoginFinishActivity extends Activity implements View.OnClickListener{

    private LinearLayout startLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login_finish);

        startLinearLayout = findViewById(R.id.start_linear_layout);
        startLinearLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_linear_layout:
                Intent intent = new Intent(LoginFinishActivity.this,CameraActivity.class);
                startActivity(intent);
                break;
        }
    }
}
