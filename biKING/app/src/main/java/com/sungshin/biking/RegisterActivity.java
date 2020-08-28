package com.sungshin.biking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class RegisterActivity extends Activity {
    // 파이어베이스 인증 객체 생성
    private FirebaseAuth firebaseAuth;

    // 이메일과 비밀번호
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName;

    private String email = "";
    private String password = "";
    private String name = "";

    private Button register;

    private String str_password;
    private String str_email;
    private String str_name;

    Boolean pwPossible = false;
    Boolean namePossible = false;
    Boolean emailPossible = false;
    HashMap<String, User> tempBase = firebaseDB.tempBase;
    User myUser = firebaseDB.myUser;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseDB.updateTempBase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        editTextName = (EditText)findViewById(R.id.et_name);
        editTextEmail = (EditText)findViewById(R.id.et_email);
        editTextPassword = (EditText)findViewById(R.id.et_Password);
        register = (Button)findViewById(R.id.btn_signUp);

        firebaseDB.updateTempBase();

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                str_password = editTextPassword.getText().toString();

                if (str_password.length() < 6) {
                    findViewById(R.id.NoticePW).setVisibility(View.VISIBLE);
                    register.setEnabled(false);
                } else {
                    findViewById(R.id.NoticePW).setVisibility(View.INVISIBLE);
                    pwPossible = true;

                }
                buttonUse();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                str_name = editTextName.getText().toString();
                buttonUse();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                str_email = editTextEmail.getText().toString();
                buttonUse();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        register.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();
                name = editTextName.getText().toString();
                createUser(name, email, password);
            }
        });
    }

    private void buttonUse(){
        boolemail();
        boolname();
        if((emailPossible && namePossible && pwPossible))
            register.setEnabled(true); //버튼 활성화
        else
            register.setEnabled(false); // 버튼 비활성화
    }
    private void boolemail(){
        if (str_email != null && str_email.length() != 0)
            emailPossible = true;
        else {
            emailPossible = false;
            register.setEnabled(false);
        }
    }

    private void boolname(){
        if (str_name != null && str_name.length() != 0)
            namePossible = true;
        else {
            register.setEnabled(false);
            namePossible = false;
        }
    }

    // 회원가입
    private void createUser(String name, String email, String password) {
        final String id = email; final String userName = name;

        firebaseAuth.createUserWithEmailAndPassword( email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        System.out.println(userName);
                        if (task.isSuccessful()) {
                            // 회원가입 성공
                            Toast.makeText(RegisterActivity.this,"회원가입 성공", Toast.LENGTH_SHORT).show();
                            firebaseDB.signUpFirebase(id,userName);
                            Intent intent = new Intent(getApplication(), LoginActivity.class);
                            startActivity(intent);
                        } else {
                            // 회원가입 실패
                            findViewById(R.id.NoticeEmail).setVisibility(View.VISIBLE);
                            // Toast.makeText(RegisterActivity.this,"회원가입 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}