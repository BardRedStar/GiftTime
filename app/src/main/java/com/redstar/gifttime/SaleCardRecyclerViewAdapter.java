package com.redstar.gifttime;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.redstar.gifttime.MainFragment.FragmentListener;


import java.util.List;


public class SaleCardRecyclerViewAdapter extends RecyclerView.Adapter<SaleCardRecyclerViewAdapter.ViewHolder> {

    /// Cards list
    private final List<SaleCard> mValues;
    private final FragmentListener mListener;

    public SaleCardRecyclerViewAdapter(List<SaleCard> items, FragmentListener listener) {
        mValues = items;
        mListener = listener;
    }

    /// When item was created
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_salecard, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to items.
     *
     * @param holder Holder with data of current item
     * @param position Item position in list
     */

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        /// Set data to fields
        holder.mItem = mValues.get(position);
        holder.mCompanyName.setText(mValues.get(position).companyName);
        holder.mCardDescription.setText(mValues.get(position).cardDescription);

        Bitmap bm = BitmapFactory.decodeByteArray(mValues.get(position).cardPhoto, 0, mValues.get(position).cardPhoto.length);
        holder.mCompanyLogo.setImageBitmap(bm);

        /// Set up click listener to throw data to activity
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem, position);
                }
            }
        });
    }

    /// Returns items count
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * Holder to attach data to items
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mCompanyName;
        public final TextView mCardDescription;
        public final ImageView mCompanyLogo;
        public SaleCard mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCompanyName = view.findViewById(R.id.companyName);
            mCardDescription = view.findViewById(R.id.cardDescription);
            mCompanyLogo = view.findViewById(R.id.companyLogo);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mCompanyName.getText() + "'";
        }
    }
}
