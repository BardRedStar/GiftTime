package com.redstar.gifttime;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

public class CardActivity extends AppCompatActivity implements InfoCardFragment.FragmentListener,
        EditCardFragment.FragmentListener, MainFragment.FragmentListener, CameraFragment.FragmentListener {


    public FragmentManager fm;


    /// Variable for holding current screen frame
    private int screenMode = 0;

    /// Screen frames
    private static final int SCREEN_MAIN = 0;
    private static final int SCREEN_ADD = 1;
    private static final int SCREEN_INFO = 2;
    private static final int SCREEN_EDIT = 3;
    private static final int SCREEN_CAMERA = 4;
    ///


    /// Variable for holding current camera frame
    private int cameraMode = 0;

    /// Camera screen frames
    private static final int CAMERA_NONE = 0;
    private static final int CAMERA_CARDPHOTO = 1;
    private static final int CAMERA_CARDCODEPHOTO = 2;
    ///

    /// Selected position for RecycleView
    public int selectedPosition = -1;

    /// Nothing selected constant
    private static final int SELECT_NONE = -1;

    /// For client-server connection
    private JSONObject userInfo;
    private String userId;
    ///

    /// Permission camera constant
    private final int PERMISSION_CAMERA = 1;

    /// List of cards
    private final ArrayList<SaleCard> listCards = new ArrayList<>();

    /// Http-connection to server
    private HTTPServer mServer = new HTTPServer();

    /// Bottom Navigation Panel Listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_cards:
                    changeScreen(0);
                case R.id.navigation_settings:
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        setTitle("");


        fm = getSupportFragmentManager();
        /// Getting user data after logging in
        /*
        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("email");
        String password = bundle.getString("password");
        mServer = new HTTPServer(email, password);
        logIn(email, password);
        */
        ///

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        /// Check Camera permission for getting photos of cards
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_CAMERA);
            }
        }
        ///

        changeScreen(SCREEN_MAIN);

        ImageButton btn = (ImageButton) findViewById(R.id.backButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (screenMode == SCREEN_EDIT) {
                    changeScreen(SCREEN_INFO);
                } else if (screenMode == SCREEN_ADD || screenMode == SCREEN_INFO) {
                    changeScreen(SCREEN_MAIN);
                } else if (screenMode == SCREEN_CAMERA) {
                    if (selectedPosition != -1)
                        changeScreen(SCREEN_EDIT);
                    else
                        changeScreen(SCREEN_ADD);
                }
            }
        });

        btn = (ImageButton) findViewById(R.id.leftButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedPosition != -1) {
                    EditCardFragment fragment = (EditCardFragment) fm.findFragmentById(R.id.editFragment);
                    SaleCard item = listCards.get(selectedPosition);
                    Bitmap cardCodeBitmap = BitmapFactory.decodeByteArray(item.cardCodePhoto, 0, item.cardCodePhoto.length);
                    Bitmap cardBitmap = BitmapFactory.decodeByteArray(item.cardPhoto, 0, item.cardPhoto.length);

                    fragment.setDefaultValues(item.companyName, item.cardDescription, cardCodeBitmap, cardBitmap);
                    changeScreen(SCREEN_EDIT);
                } else
                    Toast.makeText(CardActivity.this, "Ошибка! Попробуйте выбрать карту еще раз!", LENGTH_LONG).show();
            }
        });

        btn = (ImageButton) findViewById(R.id.rightButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (screenMode == SCREEN_ADD) {
                    EditCardFragment editCardFragment = (EditCardFragment) fm.findFragmentById(R.id.editFragment);
                    SaleCard card = editCardFragment.getSaleCard();
                    if (card != null) {
                        addCard(userId, card);
                    } else
                        Toast.makeText(CardActivity.this, "Ошибка! Проверьте введенные данные!", LENGTH_LONG).show();
                } else if (screenMode == SCREEN_INFO) {
                    if (selectedPosition != -1) {
                        deleteCard(userId, selectedPosition);
                    } else {
                        changeScreen(SCREEN_MAIN);
                        Toast.makeText(CardActivity.this, "Ошибка! Попробуйте заново выбрать карту!", LENGTH_LONG).show();
                    }

                } else if (screenMode == SCREEN_EDIT) {
                    EditCardFragment editCardFragment = (EditCardFragment) fm.findFragmentById(R.id.editFragment);
                    SaleCard card = editCardFragment.getSaleCard();
                    if (card != null) {
                        if (selectedPosition != -1) {
                            editCard(userId, selectedPosition, card);
                        } else
                            Toast.makeText(CardActivity.this, "Ошибка! Попробуйте заново выбрать карту!", LENGTH_LONG).show();
                    } else
                        Toast.makeText(CardActivity.this, "Ошибка! Проверьте введенные данные!", LENGTH_LONG).show();

                }
            }
        });
    }

    private void logIn(final String email, final String password) {
        new Thread() {
            @Override
            public void run() {
                userInfo = mServer.tryLogIn(email, password);
                try {
                    if (userInfo != null) {
                        userId = userInfo.getString("_id");
                        loadCards(userId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void editCard(final String userId, final int position, final SaleCard card) {
        listCards.set(position, card);
        MainFragment fragment = (MainFragment) fm.findFragmentById(R.id.mainFragment);
        fragment.updateList(listCards);
        changeScreen(SCREEN_INFO);

        /*new Thread(){
            @Override
            public void run() {
                try {
                    int responseCode = mServer.tryDeleteCard(userId, card.cardId);
                    if (responseCode == 200) {
                        final JSONObject addedCard = mServer.tryAddCard(userId, card);
                        if (addedCard != null) {

                            if (addedCard.has("_id"))
                                card.cardId = addedCard.getString("_id");
                            else card.cardId = null;

                            listCards.set(position, card);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    MainFragment fragment = (MainFragment) fm.findFragmentById(R.id.mainFragment);
                                    fragment.updateList(listCards);
                                    changeScreen(SCREEN_INFO);
                                }
                            });
                        } else
                            Toast.makeText(getApplicationContext(), "Ошибка редактирования!", LENGTH_LONG).show();
                    } else
                        Toast.makeText(getApplicationContext(), "Ошибка редактирования!", LENGTH_LONG).show();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }.start();*/
    }

    private void deleteCard(final String userId, final int position) {
        if (listCards.remove(position) != null) {
            MainFragment fragment = (MainFragment) fm.findFragmentById(R.id.mainFragment);
            fragment.updateList(listCards);
            changeScreen(SCREEN_MAIN);
            Toast.makeText(getApplicationContext(), "Карта удалена!", LENGTH_LONG).show();
        }

        /*new Thread(){
            @Override
            public void run() {
                final int responseCode = mServer.tryDeleteCard(userId, listCards.get(position).cardId);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (responseCode == 200)
                        {
                            if (listCards.remove(position) != null) {
                                MainFragment fragment = (MainFragment) fm.findFragmentById(R.id.mainFragment);
                                fragment.updateList(listCards);
                                changeScreen(SCREEN_MAIN);
                                Toast.makeText(getApplicationContext(), "Карта удалена!", LENGTH_LONG).show();
                            }
                            else Toast.makeText(getApplicationContext(), "Ошибка удаления!", LENGTH_LONG).show();
                        }
                        else Toast.makeText(getApplicationContext(), "Ошибка удаления!", LENGTH_LONG).show();
                    }
                });
            }
        }.start();*/
    }

    private void addCard(final String userId, final SaleCard card) {
        listCards.add(card);
        MainFragment fragment = (MainFragment) fm.findFragmentById(R.id.mainFragment);
        fragment.updateList(listCards);
        changeScreen(SCREEN_MAIN);

        /*new Thread(){
            @Override
            public void run() {
                try {
                    final JSONObject addedCard = mServer.tryAddCard(userId, card);
                    if (addedCard != null) {
                        SaleCard sc = new SaleCard();
                        String base64;

                        if (addedCard.has("_id"))
                            sc.cardId = addedCard.getString("_id");
                        else sc.cardId = null;

                        if (addedCard.has("organizationName"))
                            sc.companyName = addedCard.getString("organizationName");
                        else sc.companyName = null;

                        if (addedCard.has("description"))
                            sc.cardDescription = addedCard.getString("description");
                        else sc.cardDescription = null;

                        if (addedCard.has("barCodePhoto")) {
                            base64 = addedCard.getString("barCodePhoto");
                            sc.cardCodePhoto = Base64.decode(base64, 0);
                        } else sc.cardCodePhoto = null;

                        if (addedCard.has("frontPhoto")) {
                            base64 = addedCard.getString("frontPhoto");
                            sc.cardPhoto = Base64.decode(base64, 0);
                        } else sc.cardPhoto = null;

                        listCards.add(sc);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (addedCard != null) {
                            MainFragment fragment = (MainFragment) fm.findFragmentById(R.id.mainFragment);
                            fragment.updateList(listCards);
                            changeScreen(SCREEN_MAIN);
                            }
                            else Toast.makeText(getApplicationContext(),
                                    "Ошибка соединения с сервером!", LENGTH_LONG).show();
                        }
                    });
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
        */
    }

    private void loadCards(final String userId) {
        new Thread() {
            @Override
            public void run() {
                try {
                    final JSONArray cards = mServer.tryGetCards(userId);
                    if (cards != null) {
                        SaleCard sc;
                        JSONObject card;
                        String base64;
                        for (int i = 0; i < cards.length(); i++) {
                            sc = new SaleCard();
                            card = cards.getJSONObject(i);
                            if (card.has("organizationName"))
                                sc.companyName = card.getString("organizationName");
                            else sc.companyName = null;

                            if (card.has("description"))
                                sc.cardDescription = card.getString("description");
                            else sc.cardDescription = null;

                            if (card.has("barCodePhoto")) {
                                base64 = card.getString("barCodePhoto");
                                sc.cardCodePhoto = Base64.decode(base64, 0);
                            } else sc.cardCodePhoto = null;

                            if (card.has("frontPhoto")) {
                                base64 = card.getString("frontPhoto");
                                sc.cardPhoto = Base64.decode(base64, 0);
                            } else sc.cardPhoto = null;

                            listCards.add(sc);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (cards != null) {
                                MainFragment fragment = (MainFragment) fm.findFragmentById(R.id.mainFragment);
                                fragment.updateList(listCards);
                            } else Toast.makeText(getApplicationContext(),
                                    "Ошибка соединения с сервером!", LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w("Gift Time", "Permission granted!");

                } else {
                    Log.w("Gift Time", "Permission denied!");
                }
                return;
            }
        }
    }

    private void releaseCamera() {
        CameraFragment fragment = (CameraFragment) fm.findFragmentById(R.id.cameraFragment);
        fragment.releaseCameraAndPreview();
    }

    private void openCamera() {
        CameraFragment fragment = (CameraFragment) fm.findFragmentById(R.id.cameraFragment);
        fragment.safeCameraOpen();
    }

    @Override
    public void onCardCodePhotoButtonClick() {
        cameraMode = CAMERA_CARDCODEPHOTO;
        changeScreen(SCREEN_CAMERA);
        openCamera();
    }

    @Override
    public void onCardPhotoButtonClick() {
        cameraMode = CAMERA_CARDPHOTO;
        changeScreen(SCREEN_CAMERA);
        openCamera();
    }

    @Override
    public void onPhotoTaken(Bitmap photo) {
        EditCardFragment editFragment = (EditCardFragment) fm.findFragmentById(R.id.editFragment);
        InfoCardFragment infoFragment = (InfoCardFragment) fm.findFragmentById(R.id.infoFragment);
        if (cameraMode == CAMERA_CARDPHOTO) {
            editFragment.setCardImage(photo);
            if (selectedPosition != -1)
                infoFragment.setCardImage(photo);
        } else if (cameraMode == CAMERA_CARDCODEPHOTO) {
            editFragment.setCardCodeImage(photo);
            if (selectedPosition != -1)
                infoFragment.setCardCodeImage(photo);
        }
        releaseCamera();

        if (selectedPosition != -1)
            changeScreen(SCREEN_EDIT);
        else
            changeScreen(SCREEN_ADD);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("Camera Gift Time", "Activity onPause");
        if (screenMode == 4)
            releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("Camera Gift Time", "Activity onPause");
        if (screenMode == 4)
            releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("Camera Gift Time", "Activity onResume");
        if (screenMode == 4)
            openCamera();
    }

    @Override
    public void onListFragmentInteraction(SaleCard item, int position) {
        selectedPosition = position;
        InfoCardFragment fragment = (InfoCardFragment) fm.findFragmentById(R.id.infoFragment);
        Bitmap cardCodeBitmap = BitmapFactory.decodeByteArray(item.cardCodePhoto, 0, item.cardCodePhoto.length);
        Bitmap cardBitmap = BitmapFactory.decodeByteArray(item.cardPhoto, 0, item.cardPhoto.length);

        fragment.setDefaultValues(item.companyName, item.cardDescription, cardCodeBitmap, cardBitmap);
        changeScreen(SCREEN_INFO);
    }

    @Override
    public void onAddButtonClicked() {
        selectedPosition = -1;
        EditCardFragment fragment = (EditCardFragment) fm.findFragmentById(R.id.editFragment);
        BitmapDrawable d1 = (BitmapDrawable) getResources().getDrawable(R.mipmap.technomax);
        BitmapDrawable d2 = (BitmapDrawable) getResources().getDrawable(R.mipmap.tuman);
        fragment.setDefaultValues("", "", d1.getBitmap(), d2.getBitmap());
        changeScreen(SCREEN_ADD);
    }


    public void changeScreen(int newScreenMode) {
        TextView tw;
        BottomNavigationView bnv;
        ImageButton imgBtn;
        switch (newScreenMode) {
            case SCREEN_MAIN:
                selectedPosition = -1;
                fm.beginTransaction().hide(fm.findFragmentById(R.id.infoFragment))
                        .hide(fm.findFragmentById(R.id.editFragment))
                        .hide(fm.findFragmentById(R.id.cameraFragment))
                        .show(fm.findFragmentById(R.id.mainFragment)).commit();
                tw = (TextView) findViewById(R.id.titleTextView);
                tw.setText("Мои карты");
                bnv = (BottomNavigationView) findViewById(R.id.navigation);
                bnv.setVisibility(View.VISIBLE);
                imgBtn = (ImageButton) findViewById(R.id.backButton);
                imgBtn.setVisibility(View.INVISIBLE);
                imgBtn = (ImageButton) findViewById(R.id.leftButton);
                imgBtn.setVisibility(View.INVISIBLE);
                imgBtn = (ImageButton) findViewById(R.id.rightButton);
                imgBtn.setVisibility(View.INVISIBLE);
                break;
            case SCREEN_ADD:
                fm.beginTransaction().hide(fm.findFragmentById(R.id.mainFragment))
                        .hide(fm.findFragmentById(R.id.infoFragment))
                        .hide(fm.findFragmentById(R.id.cameraFragment))
                        .show(fm.findFragmentById(R.id.editFragment)).commit();
                tw = (TextView) findViewById(R.id.titleTextView);
                tw.setText("Добавление карты");
                bnv = (BottomNavigationView) findViewById(R.id.navigation);
                bnv.setVisibility(View.INVISIBLE);
                imgBtn = (ImageButton) findViewById(R.id.rightButton);
                imgBtn.setVisibility(View.VISIBLE);
                imgBtn.setImageResource(R.mipmap.ic_check_black_24dp);
                imgBtn = (ImageButton) findViewById(R.id.backButton);
                imgBtn.setVisibility(View.VISIBLE);
                break;
            case SCREEN_INFO:
                fm.beginTransaction().hide(fm.findFragmentById(R.id.mainFragment))
                        .hide(fm.findFragmentById(R.id.editFragment))
                        .hide(fm.findFragmentById(R.id.cameraFragment))
                        .show(fm.findFragmentById(R.id.infoFragment)).commit();
                tw = (TextView) findViewById(R.id.titleTextView);
                tw.setText("Просмотр карты");
                bnv = (BottomNavigationView) findViewById(R.id.navigation);
                bnv.setVisibility(View.INVISIBLE);
                imgBtn = (ImageButton) findViewById(R.id.rightButton);
                imgBtn.setVisibility(View.VISIBLE);
                imgBtn.setImageResource(R.mipmap.ic_delete_black_24dp);
                imgBtn = (ImageButton) findViewById(R.id.leftButton);
                imgBtn.setImageResource(R.mipmap.ic_mode_edit_black_24dp);
                imgBtn.setVisibility(View.VISIBLE);
                imgBtn = (ImageButton) findViewById(R.id.backButton);
                imgBtn.setVisibility(View.VISIBLE);
                break;
            case SCREEN_EDIT:
                fm.beginTransaction().hide(fm.findFragmentById(R.id.mainFragment))
                        .hide(fm.findFragmentById(R.id.infoFragment))
                        .hide(fm.findFragmentById(R.id.cameraFragment))
                        .show(fm.findFragmentById(R.id.editFragment)).commit();
                tw = (TextView) findViewById(R.id.titleTextView);
                tw.setText("Редактирование карты");
                imgBtn = (ImageButton) findViewById(R.id.rightButton);
                imgBtn.setVisibility(View.VISIBLE);
                imgBtn.setImageResource(R.mipmap.ic_check_black_24dp);
                imgBtn = (ImageButton) findViewById(R.id.leftButton);
                imgBtn.setVisibility(View.INVISIBLE);
                break;
            case SCREEN_CAMERA:
                fm.beginTransaction().hide(fm.findFragmentById(R.id.mainFragment))
                        .hide(fm.findFragmentById(R.id.infoFragment))
                        .hide(fm.findFragmentById(R.id.editFragment))
                        .show(fm.findFragmentById(R.id.cameraFragment)).commit();
                tw = (TextView) findViewById(R.id.titleTextView);
                tw.setText("Сделайте фото");
                imgBtn = (ImageButton) findViewById(R.id.rightButton);
                imgBtn.setVisibility(View.INVISIBLE);
                imgBtn = (ImageButton) findViewById(R.id.leftButton);
                imgBtn.setVisibility(View.INVISIBLE);
                break;
        }
        screenMode = newScreenMode;
    }

    @Override
    public void onBackPressed() {
        if (screenMode == SCREEN_MAIN) {
            super.onBackPressed();
        } else if (screenMode == SCREEN_ADD || screenMode == SCREEN_INFO) {
            changeScreen(SCREEN_MAIN);
        } else if (screenMode == SCREEN_EDIT) {
            changeScreen(SCREEN_INFO);
        } else if (screenMode == SCREEN_CAMERA) {

            if (selectedPosition != -1)
                changeScreen(SCREEN_EDIT);
            else
                changeScreen(SCREEN_ADD);
        }
    }
}
