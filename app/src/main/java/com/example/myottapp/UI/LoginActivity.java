package com.example.myottapp.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.example.myottapp.R;
import com.example.myottapp.models.SessionManager;
import com.example.myottapp.models.UserDetails;

public class LoginActivity extends Activity {
    public static String flag="";
    private EditText etEmail, etPass;
    private ProgressBar progressBar;
    Button loginBtn;
    UserDetails userDetails;
    SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        etEmail = findViewById(R.id.log_mail_id);
        etPass = findViewById(R.id.log_password);
        progressBar = findViewById(R.id.login_progress);
        loginBtn = findViewById(R.id.login_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loginBtn.setVisibility(View.INVISIBLE);
                if (!isValidEmailAddress(etEmail.getText().toString())) {
                    loginBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(LoginActivity.this,"Please enter valid email", Toast.LENGTH_LONG).show();
                } else if (etPass.getText().toString().equals("")) {
                    loginBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(LoginActivity.this,"Please enter password", Toast.LENGTH_LONG).show();
                } else {
                    String email = etEmail.getText().toString();
                    String pass = etPass.getText().toString();
                    login(email, pass);
                }
            }
        });

    }
    final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            System.out.println("Login successfull");
            CognitoSettings cognitoSettings = new CognitoSettings(LoginActivity.this);

            System.out.println("Name: "+String.valueOf(userSession.getUsername())+" token: "+userSession.getAccessToken()+" Refresh: "+userSession.getRefreshToken());
            userDetails = new UserDetails(userSession.getUsername(),userSession.getAccessToken().toString(),userSession.getRefreshToken().toString());
            sessionManager.createSession(cognitoSettings.getUserPool().getCurrentUser().getUserId());
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            etEmail.setText("");
            etPass.setText("");
            finish();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            System.out.println("in getAuthentication");

            AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, String.valueOf(etPass.getText()),null);

            authenticationContinuation.setAuthenticationDetails(authenticationDetails);

            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
            System.out.println("in getMFACode");
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            System.out.println("in authenticationChallenge");
        }

        @Override
        public void onFailure(Exception exception) {
            System.out.println("Error : "+exception.getLocalizedMessage());
        }
    };

    private void login(String email, final String password) {
        progressBar.setVisibility(View.VISIBLE);

        CognitoSettings cognitoSettings = new CognitoSettings(LoginActivity.this);
        CognitoUser thisUser = cognitoSettings.getUserPool()
                .getUser(String.valueOf(email));

        thisUser.getSessionInBackground(authenticationHandler);
        progressBar.setVisibility(View.GONE);
    }


    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}