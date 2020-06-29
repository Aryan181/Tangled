package com.example.mycurrentlocation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class LoginRegisterActivity extends AppCompatActivity {

    private static final String TAG = "LoginRegisterActivity";

    int AUTHUI_REQUEST_CODE = 10001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);


        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            startActivity(new Intent(this,MainActivity.class));
        }
    }

    public void handleLoginRegister(View view)
    {
        List<AuthUI.IdpConfig> provider = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(provider).setTosUrl("https://example.com").setLogo(R.drawable.location).build();

        startActivityForResult(intent,AUTHUI_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHUI_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // We have signed in the user or we have a new user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG, "onActivityResult: " + user.toString());
                //Checking for User (New/Old)
                if (user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp()) {
                    //This is a New User
                } else {
                    //This is a returning user
                }

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                this.finish();

            } else {
                // Signing in failed
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (response == null) {
                    Log.d(TAG, "onActivityResult: the user has cancelled the sign in request");
                } else {
                    Log.e(TAG, "onActivityResult: "+response.getError());
                }
            }
        }
    }

    public String UID()
    {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}