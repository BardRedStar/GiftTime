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

        AppCompatButton button = (AppCompatButton) view.findViewById(R.id.regButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public void onButtonPressed(Uri uri) {

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
        void onRegisterFragmentInteraction(String userName, String email, String password);
    }
}
