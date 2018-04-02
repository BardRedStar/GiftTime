package com.redstar.gifttime;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;


public class MainFragment extends Fragment {

    private FragmentListener mListener;
    private RecyclerView recyclerView;

    public MainFragment() {
    }

    /**
     * Creates new {@link MainFragment} instance.
     *
     * @return new {@link MainFragment} object
     */
    public static MainFragment newInstance(int columnCount) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt("fuck", columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Updates info in {@link RecyclerView RecyclerView}.
     * @param list {@link ArrayList ArrayList} with data to take from
     */
    public void updateList(ArrayList<SaleCard> list) {
        recyclerView.setAdapter(new SaleCardRecyclerViewAdapter(list, mListener));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        /// Set up button callback and recycler view
        ImageButton btn = view.findViewById(R.id.addCardImage);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddButtonClicked();
            }
        });

        recyclerView = view.findViewById(R.id.listSaleCard);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        updateList(new ArrayList<SaleCard>());

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener) {
            mListener = (FragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FragmentListener {

        /// Method calls when card photo button has been clicked
        void onAddButtonClicked();

        /**
         * Calls when user clicked on {@link SaleCard card} in list
         *
         * @param item {@link SaleCard card object} which has been clicked
         * @param position position of card in list
         */
        void onListFragmentInteraction(SaleCard item, int position);
    }
}
