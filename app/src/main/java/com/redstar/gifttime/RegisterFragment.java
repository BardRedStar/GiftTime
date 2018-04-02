package com.redstar.gifttime;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


public class RegisterFragment extends Fragment {

    private View view;

    private OnFragmentInteractionListener mListener;

    public RegisterFragment() {

    }

    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register, container, false);

        /// Set up button callback
        AppCompatButton button = view.findViewById(R.id.regButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// Get data from fields and throw them to activity
                EditText box = view.findViewById(R.id.regEmailBox);
                String email = box.getText().toString();
                box = view.findViewById(R.id.regPasswordBox);
                String password = box.getText().toString();
                box = view.findViewById(R.id.userNameBox);
                String name = box.getText().toString();
                mListener.onRegisterFragmentInteraction(name, email, password);
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
        /**
         * Method to throw info to activity
         *
         * @param userName typed username
         * @param email typed email
         * @param password typed password
         */
        void onRegisterFragmentInteraction(String userName, String email, String password);
    }
}
