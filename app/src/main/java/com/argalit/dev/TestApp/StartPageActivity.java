package com.argalit.dev.TestApp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.argalit.dev.TestApp.R;

public class StartPageActivity extends AppCompatActivity {

    private Button NeedNewAccountButton;
    private Button AlredyHaveAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        NeedNewAccountButton = (Button) findViewById(R.id.need_account_button);
        AlredyHaveAccountButton = (Button) findViewById(R.id.already_have_account_button);

        NeedNewAccountButton.setOnClickListener(new View.OnClickListener() {
            //Отправка пользователя на регистрацию
            @Override
            public void onClick(View v)
            {
                Intent registerIntent = new Intent(StartPageActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
        AlredyHaveAccountButton.setOnClickListener(new View.OnClickListener() {
            //отправка на авторатизацию
            @Override
            public void onClick(View v)
            {
                Intent loginIntent = new Intent(StartPageActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
