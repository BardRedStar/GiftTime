package com.redstar.gifttime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;


public class AuthFragment extends Fragment {

    private View view;
    private OnFragmentInteractionListener mListener;
    public AuthFragment context;

    public AuthFragment() {
    }

    /**
     * Creates new {@link AuthFragment} instance.
     *
     * @return new {@link AuthFragment} object
     */
    public static AuthFragment newInstance() {
        AuthFragment fragment = new AuthFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_auth, container, false);

        /// Auth Button callback
        AppCompatButton button = view.findViewById(R.id.authButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// Get data from the bosex and throw it to activity
                EditText box = view.findViewById(R.id.authEmailBox);
                String email = box.getText().toString();
                box = view.findViewById(R.id.authPasswordBox);
                String password = box.getText().toString();

                mListener.onAuthFragmentInteraction(email, password);
            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        ///Method to throw data to activity
        void onAuthFragmentInteraction(String email, String password);
    }
}
