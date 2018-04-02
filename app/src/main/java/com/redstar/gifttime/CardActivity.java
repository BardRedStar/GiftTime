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

        /// Set callbacks for buttons from toolbar

        ImageButton btn = (ImageButton) findViewById(R.id.backButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (screenMode == SCREEN_EDIT) {
                    changeScreen(SCREEN_INFO);
                } else if (screenMode == SCREEN_ADD || screenMode == SCREEN_INFO) {
                    changeScreen(SCREEN_MAIN);
                } else if (screenMode == SCREEN_CAMERA) {
                    if (selectedPosition != SELECT_NONE)
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
                if (selectedPosition != SELECT_NONE) {
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
                    if (selectedPosition != SELECT_NONE) {
                        deleteCard(userId, selectedPosition);
                    } else {
                        changeScreen(SCREEN_MAIN);
                        Toast.makeText(CardActivity.this, "Ошибка! Попробуйте заново выбрать карту!", LENGTH_LONG).show();
                    }

                } else if (screenMode == SCREEN_EDIT) {
                    EditCardFragment editCardFragment = (EditCardFragment) fm.findFragmentById(R.id.editFragment);
                    SaleCard card = editCardFragment.getSaleCard();
                    if (card != null) {
                        if (selectedPosition != SELECT_NONE) {
                            editCard(userId, selectedPosition, card);
                        } else
                            Toast.makeText(CardActivity.this, "Ошибка! Попробуйте заново выбрать карту!", LENGTH_LONG).show();
                    } else
                        Toast.makeText(CardActivity.this, "Ошибка! Проверьте введенные данные!", LENGTH_LONG).show();

                }
            }
        });
    }

    /**
     * Function for logging in by email and password. Runs in new {@link Thread}.
     *
     * @param email Email string
     * @param password Password string
     */
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

    /**
     * Replaces old {@link SaleCard card} with new {@link SaleCard card} in
     * {@link ArrayList list of cards} by position.
     * <p>
     * <s>Creates a new {@link Thread} for throwing edit request to server </s>
     * </p>
     *
     * @param userId <s>userID for throw a request to server</s>
     * @param position card position in RecyclerView to replace card
     * @param card card to replace
     */
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

    /**
     * Removes {@link SaleCard card} from {@link ArrayList list of cards} by position.
     * <p>
     * <s>Creates a new {@link Thread} for throwing delete request to server </s>
     * </p>
     *
     * @param userId <s>userID for throw a request to server</s>
     * @param position card position in RecyclerView to delete card
     */
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

    /**
     * Adds new {@link SaleCard card} in {@link ArrayList list of cards}.
     * <p>
     * <s>Creates a new {@link Thread} for throwing post request to server </s>
     * </p>
     *
     * @param userId <s>userID for throw a request to server</s>
     * @param card new card to add in array
     */
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

    /** @deprecated
     * Creates get request to server and loads current cards. Gets and parses
     * {@link JSONObject JSON}.
     * <p>
     * Creates a new {@link Thread} for throwing post request to server
     * </p>
     *
     * @param userId UserID for load cards
     */
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

    /**
     * Gets result of permission request.
     *
     * @param requestCode constant to identify request
     * @param permissions array of permissions
     * @param grantResults array of request results for each requested permission
     */
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

    /**
     * Releases out {@link android.graphics.Camera camera}
     * and {@link android.view.SurfaceView preview}
     */
    private void releaseCamera() {
        CameraFragment fragment = (CameraFragment) fm.findFragmentById(R.id.cameraFragment);
        fragment.releaseCameraAndPreview();
    }

    /**
     * Opens {@link android.graphics.Camera camera} and {@link android.view.SurfaceView preview}
     */
    private void openCamera() {
        CameraFragment fragment = (CameraFragment) fm.findFragmentById(R.id.cameraFragment);
        fragment.safeCameraOpen();
    }

    /**
     * Callback from {@link EditCardFragment edit card screen}. Runs when
     * change card code photo button was pressed. Changes frame from
     * {@link EditCardFragment edit card screen} to {@link CameraFragment camera screen}.
     */
    @Override
    public void onCardCodePhotoButtonClick() {
        cameraMode = CAMERA_CARDCODEPHOTO;
        changeScreen(SCREEN_CAMERA);
        openCamera();
    }

    /**
     * Callback from {@link EditCardFragment edit card screen}. Runs when
     * change card photo button was pressed. Changes frame from
     * {@link EditCardFragment edit card screen} to {@link CameraFragment camera screen}.
     */
    @Override
    public void onCardPhotoButtonClick() {
        cameraMode = CAMERA_CARDPHOTO;
        changeScreen(SCREEN_CAMERA);
        openCamera();
    }

    /**
     * Callback from {@link CameraFragment camera screen}. Runs when
     * photo was created. Applies photo to appropriate {@link android.widget.ImageView}.
     *
     * @param photo Bitmap of created photo
     */
    @Override
    public void onPhotoTaken(Bitmap photo) {
        EditCardFragment editFragment = (EditCardFragment) fm.findFragmentById(R.id.editFragment);
        InfoCardFragment infoFragment = (InfoCardFragment) fm.findFragmentById(R.id.infoFragment);
        if (cameraMode == CAMERA_CARDPHOTO) {
            editFragment.setCardImage(photo);
            if (selectedPosition != SELECT_NONE)
                infoFragment.setCardImage(photo);
        } else if (cameraMode == CAMERA_CARDCODEPHOTO) {
            editFragment.setCardCodeImage(photo);
            if (selectedPosition != SELECT_NONE)
                infoFragment.setCardCodeImage(photo);
        }
        releaseCamera();

        if (selectedPosition != SELECT_NONE)
            changeScreen(SCREEN_EDIT);
        else
            changeScreen(SCREEN_ADD);
    }

    /**
     * Activity pause event. Releases {@link android.graphics.Camera Camera}.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.w("Camera Gift Time", "Activity onPause");
        if (screenMode == 4)
            releaseCamera();
    }

    /**
     * Activity destroy event. Releases {@link android.graphics.Camera Camera}.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("Camera Gift Time", "Activity onPause");
        if (screenMode == 4)
            releaseCamera();
    }

    /**
     * Activity resume event. Opens {@link android.graphics.Camera Camera} if it was open before
     * application was paused.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.w("Camera Gift Time", "Activity onResume");
        if (screenMode == 4)
            openCamera();
    }

    /**
     * Callback from {@link MainFragment main screen}. Runs when item from
     * {@link android.support.v7.widget.RecyclerView RecyclerView} was pressed. Remembers selected
     * position and clears {@link InfoCardFragment info screen}. Changes screen to
     * {@link InfoCardFragment info screen}.
     *
     * @param item Selected card from {@link android.support.v7.widget.RecyclerView RecyclerView}
     * @param position Position of selected {@link SaleCard card}
     */
    @Override
    public void onListFragmentInteraction(SaleCard item, int position) {
        selectedPosition = position;
        InfoCardFragment fragment = (InfoCardFragment) fm.findFragmentById(R.id.infoFragment);
        Bitmap cardCodeBitmap = BitmapFactory.decodeByteArray(item.cardCodePhoto, 0, item.cardCodePhoto.length);
        Bitmap cardBitmap = BitmapFactory.decodeByteArray(item.cardPhoto, 0, item.cardPhoto.length);

        fragment.setDefaultValues(item.companyName, item.cardDescription, cardCodeBitmap, cardBitmap);
        changeScreen(SCREEN_INFO);
    }

    /**
     * Callback from {@link MainFragment main screen}. Runs when
     * {@link android.widget.Button add button} was pressed. Used for changing screen.
     */
    @Override
    public void onAddButtonClicked() {
        selectedPosition = SELECT_NONE;
        EditCardFragment fragment = (EditCardFragment) fm.findFragmentById(R.id.editFragment);
        BitmapDrawable d1 = (BitmapDrawable) getResources().getDrawable(R.mipmap.technomax);
        BitmapDrawable d2 = (BitmapDrawable) getResources().getDrawable(R.mipmap.tuman);
        fragment.setDefaultValues("", "", d1.getBitmap(), d2.getBitmap());
        changeScreen(SCREEN_ADD);
    }

    /**
     * Changes screen to new by screen mode value.
     *
     * @param newScreenMode index of screen to change on. Check out constants on the top.
     */
    public void changeScreen(int newScreenMode) {
        TextView tw;
        BottomNavigationView bnv;
        ImageButton imgBtn;
        switch (newScreenMode) {
            case SCREEN_MAIN:
                selectedPosition = SELECT_NONE;
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

    /**
     *  Device back button press event. Needs for correct screen changing.
     */
    @Override
    public void onBackPressed() {
        if (screenMode == SCREEN_MAIN) {
            super.onBackPressed();
        } else if (screenMode == SCREEN_ADD || screenMode == SCREEN_INFO) {
            changeScreen(SCREEN_MAIN);
        } else if (screenMode == SCREEN_EDIT) {
            changeScreen(SCREEN_INFO);
        } else if (screenMode == SCREEN_CAMERA) {

            if (selectedPosition != SELECT_NONE)
                changeScreen(SCREEN_EDIT);
            else
                changeScreen(SCREEN_ADD);
        }
    }
}
