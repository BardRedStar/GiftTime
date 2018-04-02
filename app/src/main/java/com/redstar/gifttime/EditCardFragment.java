package com.redstar.gifttime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class EditCardFragment extends Fragment {
    private View view;

    private FragmentListener mListener;

    public EditCardFragment() {

    }

    /**
     * Creates new {@link CameraFragment} instance.
     *
     * @return new {@link CameraFragment} object
     */
    public static EditCardFragment newInstance() {
        return new EditCardFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_edit_card, container, false);
        /// Initialize autocomplete
        AutoCompleteTextView companyBox = view.findViewById(R.id.editCompanyNameBox);

        List<String> list = Arrays.asList(getResources().getStringArray(R.array.partners));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_dropdown_item_1line, list);
        companyBox.setAdapter(adapter);
        ///

        /// Set up button callbacks
        Button btn = view.findViewById(R.id.editFrontPhotoButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onCardPhotoButtonClick();
            }
        });

        btn = view.findViewById(R.id.editCardCodeButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {mListener.onCardCodePhotoButtonClick();
            }
        });
        return view;
    }

    /**
     * Sets values in fields.
     *
     * @param companyName Name of partner company
     * @param cardDescription Description of {@link SaleCard card}
     * @param cardCodePhoto card code photo bitmap
     * @param cardPhoto card photo bitmap
     */
    public void setDefaultValues(String companyName, String cardDescription, Bitmap cardCodePhoto, Bitmap cardPhoto) {
        AutoCompleteTextView tw = view.findViewById(R.id.editCompanyNameBox);
        tw.setText(companyName);

        EditText text = view.findViewById(R.id.editCardDescriptionBox);
        text.setText(cardDescription);

        ImageView img = view.findViewById(R.id.editCardCodeImageView);
        img.setImageBitmap(cardCodePhoto);

        img = view.findViewById(R.id.editCardImageView);
        img.setImageBitmap(cardPhoto);
    }

    /**
     * Sets card photo to {@link ImageView}
     *
     * @param cardImage Image to set
     */
    public void setCardImage(Bitmap cardImage) {
        ImageView img = view.findViewById(R.id.editCardImageView);
        img.setImageBitmap(cardImage);
    }

    /**
     * Sets card code photo to {@link ImageView}
     *
     * @param cardCodeImage Image to set
     */
    public void setCardCodeImage(Bitmap cardCodeImage) {
        ImageView img = view.findViewById(R.id.editCardCodeImageView);
        img.setImageBitmap(cardCodeImage);
    }

    /**
     * Gathers data from fields and images and creates new {@link SaleCard card object} on it.
     *
     * @return new {@link SaleCard} object with data placed in.
     */
    public SaleCard getSaleCard() {
        SaleCard result = new SaleCard();

        AutoCompleteTextView tw = getActivity().findViewById(R.id.editCompanyNameBox);
        result.companyName = tw.getText().toString();
        if (result.companyName.equals(""))
            return null;

        EditText text = getActivity().findViewById(R.id.editCardDescriptionBox);
        result.cardDescription = text.getText().toString();
        if (result.cardDescription.equals(""))
            return null;

        try {
            ByteArrayOutputStream baos;
            baos = new ByteArrayOutputStream();

            ImageView img = getActivity().findViewById(R.id.editCardCodeImageView);
            Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            result.cardCodePhoto = baos.toByteArray();
            baos.close();

            baos = new ByteArrayOutputStream();
            img = getActivity().findViewById(R.id.editCardImageView);
            bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            result.cardPhoto = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return result;
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
        /// Method calls when card photo button has been clicked
        void onCardPhotoButtonClick();

        /// Method calls when card code photo button has been clicked
        void onCardCodePhotoButtonClick();
    }
}
