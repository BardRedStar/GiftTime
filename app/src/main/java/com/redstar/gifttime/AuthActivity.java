package com.redstar.gifttime;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

public class AuthActivity extends AppCompatActivity implements AuthFragment.OnFragmentInteractionListener
        , RegisterFragment.OnFragmentInteractionListener {

    public FragmentManager fm;
    public HTTPServer mServer;

    /*
        0 - Auth screen
        1 - Register screen
     */
    public int screenMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        fm = getSupportFragmentManager();
        fm.beginTransaction().hide(fm.findFragmentById(R.id.registerFragment)).commit();

        TextView tw = (TextView) findViewById(R.id.regTextView);
        tw.setPaintFlags(tw.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeScreen(1);
            }
        });
        mServer = new HTTPServer();
    }

    @Override
    public void onBackPressed() {
        if (screenMode == 0) {
            super.onBackPressed();
        } else
            changeScreen(0);
    }

    public void changeScreen(int newScreenMode) {
        switch (newScreenMode) {
            case 0:
                fm.beginTransaction().hide(fm.findFragmentById(R.id.registerFragment))
                        .show(fm.findFragmentById(R.id.authFragment)).commit();
                break;
            case 1:
                fm.beginTransaction().hide(fm.findFragmentById(R.id.authFragment))
                        .show(fm.findFragmentById(R.id.registerFragment)).commit();
                break;
        }
        screenMode = newScreenMode;
    }

    @Override
    public void onAuthFragmentInteraction(final String email, final String password) {
        if (email.equals("")) {
            Toast.makeText(this, "Введите email!", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.equals("")) {
            Toast.makeText(this, "Введите пароль!", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                final JSONObject json = mServer.tryLogIn(email, password);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (json != null) {
                            try {
                                if (json.has("message")) {
                                    Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                                } else {
                                    Intent intent = new Intent(getBaseContext(), CardActivity.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("password", password);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else
                            Toast.makeText(getApplicationContext(), "Ошибка соединения с сервером!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.start();
    }

    @Override
    public void onRegisterFragmentInteraction(final String userName, final String email, final String password) {
        if (email.equals("")) {
            Toast.makeText(this, "Введите email!", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.equals("")) {
            Toast.makeText(this, "Введите пароль!", Toast.LENGTH_LONG).show();
            return;
        }

        if (userName.equals("")) {
            Toast.makeText(this, "Введите имя пользователя!", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 6 || password.length() > 20) {
            Toast.makeText(this, "Недопустимая длина пароля (6-20 символов)!", Toast.LENGTH_LONG).show();
            return;
        }

        if (userName.length() > 25) {
            Toast.makeText(this, "Недопустимая длина имени (1-25 символов)!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Некорректный email!", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread() {
            @Override
            public void run() {
                final JSONObject json = mServer.trySignUp(userName, email, password);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (json != null) {
                            try {
                                if (json.has("errors")) {
                                    JSONObject jsonErrors = json.getJSONObject("errors");
                                    Log.w("Gift Time", json.toString());
                                    if (jsonErrors.has("user")) {
                                        Toast.makeText(getApplicationContext(), json.getJSONObject("user").getString("msg"), Toast.LENGTH_LONG).show();
                                    } else if (jsonErrors.has("password")) {
                                        Toast.makeText(getApplicationContext(), json.getJSONObject("password").getString("msg"), Toast.LENGTH_LONG).show();
                                    } else if (jsonErrors.has("email")) {
                                        Toast.makeText(getApplicationContext(), json.getJSONObject("email").getString("msg"), Toast.LENGTH_LONG).show();
                                    }
                                } else if (json.has("message")) {
                                    Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                                } else {
                                    Intent intent = new Intent(getBaseContext(), CardActivity.class);
                                    intent.putExtra("user", json.toString());

                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else
                            Toast.makeText(getApplicationContext(), "Ошибка соединения с сервером!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }.start();
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
