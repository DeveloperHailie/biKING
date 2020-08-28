package com.sungshin.biking;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sungshin.biking.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.regex.Pattern;

public class LoginActivity  extends AppCompatActivity {

    // 비밀번호 정규식
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{6,16}$");

    // 파이어베이스 인증 객체 생성
    private FirebaseAuth firebaseAuth;

    // 이메일과 비밀번호
    private EditText editTextEmail;
    private EditText editTextPassword;

    private String email = "";
    private String password = "";

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private SignInButton signInButton;
    HashMap<String, User> tempBase = firebaseDB.tempBase;
    User myUser = firebaseDB.myUser;

    //private Button btn_custom_login;
    //private Button btn_custom_login_out;
    private static final String TAG = "";
    SessionCallback callback;
    //Session session;
    private ImageView fakeGoogle;
    private ImageView fakeKakao;

    private String kakaoId;
    private String kakaoName;
    private String kakaopassword = "123456";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        requestMe();
       // Log.e("here", Utility.getKeyHash(this));

        // 파이어베이스 인증 객체 선언
        getAppKeyHash();
        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.et_eamil);
        editTextPassword = findViewById(R.id.et_password);
        signInButton = findViewById(R.id.signInButton);
        fakeGoogle = findViewById(R.id.fakegoogle);
        fakeKakao = findViewById(R.id.fakekakao);

        fakeKakao.setImageResource(R.drawable.kakao_login);
        fakeGoogle.setImageResource(R.drawable.google_login);

        firebaseDB.maps2Condition = false;


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


    }
    class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            Log.e("here1",  "here");
            UserManagement.getInstance().me(new MeV2ResponseCallback() {

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);

                }

                @Override
                public void onSuccess(MeV2Response result) {
                    kakaoId = Long.toString(result.getId());
                    Log.i("KAKAO_API", "사용자 아이디: " + result.getId());
                    //Toast.makeText(LoginActivity.this, "카카오 로그인 성공", Toast.LENGTH_SHORT).show();
                    UserAccount kakaoAccount = result.getKakaoAccount();
//                    Intent intent = new Intent(getApplication(), MainActivity.class);
//                    Log.i("KAKAO_API", "왜안돼");
//                    startActivity(intent);
//                    Log.i("KAKAO_API", "왜안돼2");

                    if (kakaoAccount != null) {
                        // 이메일
                        String email = kakaoAccount.getEmail();
                        Log.i("KAKAO_API", "여기1");

                        if (email != null) {
                            Log.i("KAKAO_API", "email: " + email);
                            Log.i("KAKAO_API", "여기2");

                        } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                            // 동의 요청 후 이메일 획득 가능
                            // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.
                            Log.i("KAKAO_API", "여기3");

                        } else {
                            // 이메일 획득 불가
                            Log.i("KAKAO_API", "여기4");
                        }

                        // 프로필
                        Profile profile = kakaoAccount.getProfile();
                        Log.i("KAKAO_API", "여기5");
                        if (profile != null) {
                            Log.d("KAKAO_API", "nickname: " + profile.getNickname());
                            kakaoName = profile.getNickname();
                            Log.d("KAKAO_API", "profile image: " + profile.getProfileImageUrl());
                            Log.d("KAKAO_API", "thumbnail image: " + profile.getThumbnailImageUrl());
                            Log.i("KAKAO_API", "여기6");

                        } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                            // 동의 요청 후 프로필 정보 획득 가능
                            Log.i("KAKAO_API", "여기7");

                        } else {
                            // 프로필 획득 불가
                            Log.i("KAKAO_API", "여기8");
                        }
//                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                        startActivity(intent);
//                        finish();
                    }
                    Log.e("여기요","여기");
                    kakaoLogin(kakaoId,kakaoName);
                    //createUser(kakaoName, kakaoId, kakaopassword);
                    Log.e("여기요2","여기는?");


                }
            });
//


        }
        // 세션 실패시
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            // Utility.getKeyHash();
            Log.e("here2",  "here2");
            Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();

        }
    }
    public void requestMe() {
        //유저의 정보를 받아오는 함수
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.e("KAKAO_API", "사용자 정보 요청 실패: " + errorResult);
            }

            @Override
            public void onSuccess(MeV2Response result) {
                Log.i("KAKAO_API", "사용자 아이디: " + result.getId());

                UserAccount kakaoAccount = result.getKakaoAccount();
                if (kakaoAccount != null) {

                    // 이메일
                    String email = kakaoAccount.getEmail();

                    if (email != null) {
                        Log.i("KAKAO_API", "email: " + email);

                    } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                        // 동의 요청 후 이메일 획득 가능
                        // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.

                    } else {
                        // 이메일 획득 불가
                    }

                    // 프로필
                    Profile profile = kakaoAccount.getProfile();

                    if (profile != null) {
                        Log.d("KAKAO_API", "nickname: " + profile.getNickname());
                        Log.d("KAKAO_API", "profile image: " + profile.getProfileImageUrl());
                        Log.d("KAKAO_API", "thumbnail image: " + profile.getThumbnailImageUrl());

                    } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                        // 동의 요청 후 프로필 정보 획득 가능

                    } else {
                        // 프로필 획득 불가
                    }
                }
            }
        });
    }





    @Override
    protected void onStart() {
        super.onStart();
        firebaseDB.updateTempBase();
    }

    public void singUp(View view) {
        Intent intent = new Intent(getApplication(), RegisterActivity.class);
        startActivity(intent);
    }

    public void signIn(View view) {
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        if(email.isEmpty()||password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        loginUser(email, password);
    }
    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        // 카카오톡|스토리 간편로그인 실행 결과를 받아서 SDK로 전달
//        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
//            return;
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Toast.makeText(LoginActivity.this, "구글 로그인 성공", Toast.LENGTH_SHORT).show();
                            Log.d("에러","onComplete");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "구글 로그인 실패", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }
    // 로그인 후 activity 전환 __ google login 시
    private void updateUI(FirebaseUser user) { //update ui code here
        Log.d("에러","UpdateUI");
        if (user != null) {
            String email = user.getEmail();
            String name = user.getDisplayName();
            if(firebaseDB.myData(email)==null){
                firebaseDB.signUpFirebase(email,name);
                Toast.makeText(LoginActivity.this, "구글 회원가입 성공", Toast.LENGTH_SHORT).show();
                onRestart();
            }
            else{
                Toast.makeText(LoginActivity.this, "구글 로그인 성공", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }

        }
    }
    // 이메일 유효성 검사
    private boolean isValidEmail() {
        if (email.isEmpty()) {
            // 이메일 공백
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(LoginActivity.this, "이메일 형식 불일치", Toast.LENGTH_SHORT).show();
            // 이메일 형식 불일치
            return false;
        } else {
            return true;
        }
    }

    // 비밀번호 유효성 검사
    private boolean isValidPasswd() {
        if (password.isEmpty()) {
            // 비밀번호 공백
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            // 비밀번호 형식 불일치
            Toast.makeText(LoginActivity.this, "비밀번호 정규식 ^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    // 회원가입
    private void createUser(String name, String email, String password) {
        final String id = email;
        final String userName = name;


        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("로그 들어왔나요","네");
                        Log.d("로그 닉네임", ""+userName);
                        Log.d("로그 아이디", ""+id);
                        if (task.isSuccessful()) {
                            // 회원가입 성공
                            Log.d("등록됐나요","네");
                            Toast.makeText(LoginActivity.this,"카카오 정보 등록 성공", Toast.LENGTH_SHORT).show();
                        } else {
                            // 회원가입 실패
                            Log.d("등록됐나요","아니오");
                            Toast.makeText(LoginActivity.this,"카카오 정보 등록 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 로그인
    private void loginUser(String email, String password)
    {
        final String id =email;
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                            firebaseDB.myData(id);
                            Intent intent = new Intent(getApplication(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            // 로그인 실패
                            Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.d("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }

    public void kakaoLogin(String email, String name){
        if(firebaseDB.myData(email)==null){
            firebaseDB.signUpFirebase(email,name);
            Toast.makeText(LoginActivity.this, "카카오톡 회원가입 성공", Toast.LENGTH_SHORT).show();
            onRestart();
        }
        else{
            Toast.makeText(LoginActivity.this, "카카오톡 로그인 성공", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}