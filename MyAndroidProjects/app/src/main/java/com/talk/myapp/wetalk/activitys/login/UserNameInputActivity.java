package com.talk.myapp.wetalk.activitys.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.talk.myapp.wetalk.R;
import com.talk.myapp.wetalk.activitys.login.LoginFinishActivity;

public class UserNameInputActivity extends Activity implements View.OnClickListener {

    private EditText userNameEditText;
    private LinearLayout finishLinearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_name_input);

        userNameEditText = findViewById(R.id.user_name_edit_text);
        finishLinearLayout = findViewById(R.id.finish_linear_layout);

        finishLinearLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.finish_linear_layout:
                //TODO 用户名check check通过再跳转
                String userName = userNameEditText.getText().toString();
                if(!userName.isEmpty()){
                    Intent intent = new Intent(this,LoginFinishActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(this,"请输入用户名",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
