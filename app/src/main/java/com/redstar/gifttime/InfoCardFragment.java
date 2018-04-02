package com.redstar.gifttime;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class InfoCardFragment extends Fragment {


    private FragmentListener mListener;
    private View view;

    public InfoCardFragment() {

    }

    public static InfoCardFragment newInstance(String param1, String param2) {

        return new InfoCardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_info_card, container, false);
        return view;
    }

    public void setDefaultValues(String companyName, String cardDescription, Bitmap cardCodePhoto, Bitmap cardPhoto) {
        TextView tv = view.findViewById(R.id.infoCompanyNameTextView);
        tv.setText(companyName);

        tv = view.findViewById(R.id.infoCardDescriptionTextView);
        tv.setText(cardDescription);

        ImageView img = view.findViewById(R.id.infoCardCodePhotoImageView);
        img.setImageBitmap(cardCodePhoto);

        img = view.findViewById(R.id.infoCardPhotoImageView);
        img.setImageBitmap(cardPhoto);
    }

    public void setCardImage(Bitmap cardImage) {
        ImageView img = view.findViewById(R.id.infoCardPhotoImageView);
        img.setImageBitmap(cardImage);
    }

    public void setCardCodeImage(Bitmap cardCodeImage) {
        ImageView img = view.findViewById(R.id.infoCardCodePhotoImageView);
        img.setImageBitmap(cardCodeImage);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
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

    public interface FragmentListener {

    }
}
