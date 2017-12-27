package com.talk.myapp.wetalk.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.talk.myapp.wetalk.R;

public class PhoneNumberInputActivity extends Activity implements View.OnClickListener {

    LinearLayout linearLayout;
    EditText editText;
    TextView errorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_phone_number_input);

        linearLayout = findViewById(R.id.next_linear_layout);
        editText = findViewById(R.id.phone_number_edit_text);
        errorMsg = findViewById(R.id.error_msg);

        linearLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.next_linear_layout:
                String phoneNumber = editText.getText().toString();
                //判断手机号是否正确
                if(!phoneNumber.isEmpty() && phoneNumber.length()==11 && phoneNumber.startsWith("1")){
                    errorMsg.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(this ,VerificationCodeInputActivity.class);
                    intent.putExtra("phoneNumber",phoneNumber);
                    //TODO 发送验证码到手机
                    startActivity(intent);
                }else{
                    errorMsg.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
