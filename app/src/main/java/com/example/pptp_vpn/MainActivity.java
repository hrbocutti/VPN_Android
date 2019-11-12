package com.example.pptp_vpn;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.vpn.Client;
import com.example.vpn.Vpn;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USERNAME = "username";

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btConnect = findViewById(R.id.connect);
        btConnect.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongViewCast")
            @Override
            public void onClick(View v) {

                Intent intent = VpnService.prepare(getApplicationContext());
                if (intent != null) {
                    startActivityForResult(intent, 0);
                } else {
                    onActivityResult(0, RESULT_OK, null);
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Intent intent = new Intent(this, Client.class);

        EditText username = findViewById(R.id.username);
        EditText pw = findViewById(R.id.password);
        intent.putExtra(MainActivity.KEY_USERNAME, username.getText().toString());
        intent.putExtra(MainActivity.KEY_PASSWORD, pw.getText().toString());

        startService(intent);

    }
}
