package com.talk.myapp.wetalk.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.talk.myapp.wetalk.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class VerificationCodeInputActivity extends Activity implements View.OnClickListener {

    TextView phoneNumberTextView;
    LinearLayout editNumber;
    LinearLayout codeConfirm;
    List<EditText> etList;
    EditText et1,et2,et3,et4;
    String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_verification_code_input);

        //初始化view
        phoneNumberTextView = findViewById(R.id.verification_code_phone_number_text);
        editNumber = findViewById(R.id.edit_number);
        codeConfirm = findViewById(R.id.code_confirm_linear_layout);

        et1 = findViewById(R.id.verification_code_et_1);
        et2 = findViewById(R.id.verification_code_et_2);
        et3 = findViewById(R.id.verification_code_et_3);
        et4 = findViewById(R.id.verification_code_et_4);
        etList = new ArrayList<>();
        etList.add(et1);
        etList.add(et2);
        etList.add(et3);
        etList.add(et4);

        //获取上个页面输入的电话号码
        Intent intent = getIntent();
        String phoneNumber = intent.getStringExtra("phoneNumber");
        phoneNumberTextView.setText(phoneNumber);

        //监听点击事件
        editNumber.setOnClickListener(this);
        codeConfirm.setOnClickListener(this);


        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Log.d("beforeTextChanged",charSequence.toString()+i+i1+i2);
            }

            //验证码退格键的处理
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Log.d("onTextChanged",charSequence.toString()+i+i1+i2);
                if(charSequence.length()==1){
                    int currentEditTextId = getCurrentFocus().getId();
                    for(i=0; i<etList.size();i++){
                        if(etList.get(i).getId() == currentEditTextId){
                            int index = i+ 1;
                            //如果还有下一个edittext，就让下一个获取焦点
                            if(index != etList.size()){
                                etList.get(index).requestFocus();
                            }
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
//                Log.d("afterTextChanged",editable.toString());
            }
        };

        //添加edittext监听事件
        et1.addTextChangedListener(tw);
        et2.addTextChangedListener(tw);
        et3.addTextChangedListener(tw);
        et4.addTextChangedListener(tw);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.edit_number:
                finish();
                break;
            case R.id.code_confirm_linear_layout:
                //TODO 判断验证码是否正确
                //如果验证码不正确，弹出toast提示
                //如果验证码正确，则到用户名输入页面,保存手机号？token？
                String editText1 = et1.getText().toString();
                String editText2 = et2.getText().toString();
                String editText3 = et3.getText().toString();
                String editText4 = et4.getText().toString();
                if(!editText1.isEmpty() && !editText2.isEmpty() && !editText3.isEmpty() && !editText4.isEmpty()){
                    verificationCode = et1.getText().toString()+et2.getText().toString()
                            +et3.getText().toString()+et4.getText().toString();
                    //TODO 比较验证码
//                    if(验证码一致){
//                        跳转到下一个画面
                    Intent intent = new Intent(this,UserNameInputActivity.class);
                    startActivity(intent);
//                    }else{
//                         Toast.makeText(this,"验证码输入不正确，请重新输入",Toast.LENGTH_SHORT).show();
//                    }
                }else {
                    Toast.makeText(this,"验证码输入不正确",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
