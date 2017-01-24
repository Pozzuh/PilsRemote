package nl.svia.pilsremote.adapters;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;

import nl.svia.pilsremote.R;
import nl.svia.pilsremote.misc.ProductModel;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ProductAdapter";

    private Context mContext;
    private ProductViewHolderListener mProductViewHolderListener;
    private HeaderViewHolderListener mHeaderViewHolderListener;

    private final static int TYPE_HEADER = 0;
    private final static int TYPE_USER = 1;

    private Comparator<ProductModel> mComparator = new Comparator<ProductModel>() {
        @Override
        public int compare(ProductModel a, ProductModel b) {
            return a.getName().compareTo(b.getName());
        }
    };

    private final SortedList<ProductModel> mList = new SortedList<>(ProductModel.class,
            new SortedList.Callback<ProductModel>() {
                @Override
                public int compare(ProductModel a, ProductModel b) {
                    return mComparator.compare(a, b);
                }

                @Override
                public void onInserted(int position, int count) {
                    notifyItemRangeInserted(position, count);
                }

                @Override
                public void onRemoved(int position, int count) {
                    notifyItemRangeRemoved(position, count);
                }

                @Override
                public void onMoved(int fromPosition, int toPosition) {
                    notifyItemMoved(fromPosition, toPosition);
                }

                @Override
                public void onChanged(int position, int count) {
                    notifyItemRangeChanged(position, count);
                }

                @Override
                public boolean areContentsTheSame(ProductModel oldItem, ProductModel newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areItemsTheSame(ProductModel item1, ProductModel item2) {
                    return item1.getId() == item2.getId();
                }
            });

    public ProductAdapter(Context context, ProductViewHolderListener userListener,
                          HeaderViewHolderListener headerListener) {
        this.mContext = context;
        this.mProductViewHolderListener = userListener;
        this.mHeaderViewHolderListener = headerListener;

        Log.d(TAG, "Product adapter created");
    }

    @Override
    public int getItemViewType(int position) {
        /*
         * We show the footer when there is only one user visible, but we never show the
         * header and the footer at the same time.
         */
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_USER;
        }
    }

    public ProductModel getItem(int i) {
        return mList.get(i - 1);
    }

    public void add(ProductModel model) {
        mList.add(model);
    }

    public void remove(ProductModel model) {
        mList.remove(model);
    }

    public void add(List<ProductModel> models) {
        mList.addAll(models);
    }

    public void remove(List<ProductModel> models) {
        mList.beginBatchedUpdates();
        for (ProductModel model : models) {
            mList.remove(model);
        }
        mList.endBatchedUpdates();
    }

    public void removeAll() {
        mList.clear();
    }

    public void replaceAll(List<ProductModel> models) {
        mList.beginBatchedUpdates();

        mList.clear();
        mList.addAll(models);
        mList.endBatchedUpdates();
    }

    /**
     * Deletes all but one user
     *
     * @param user the user to keep
     */
    public void replaceOne(ProductModel user) {
        mList.beginBatchedUpdates();

        mList.clear();
        mList.add(user);

        mList.endBatchedUpdates();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        Context c = viewGroup.getContext();

        if (viewType == TYPE_HEADER) {
            Log.d(TAG, "oncreate viewholder header");
            v = LayoutInflater.from(c).inflate(R.layout.list_item_product_header, viewGroup, false);
            return new HeaderViewHolder(v, mHeaderViewHolderListener);
        } else {
            // TYPE_PRODUCT
            v = LayoutInflater.from(c).inflate(R.layout.list_item_product, viewGroup, false);
            return new ProductViewHolder(v, mProductViewHolderListener);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof ProductViewHolder) {
            ProductViewHolder userViewHolder = (ProductViewHolder) viewHolder;

            // Minus 1 because of the header
            ProductModel obj = getItem(i);
            String name = obj.getName();

            userViewHolder.title.setText(name);
            userViewHolder.price.setText(mContext.getString(R.string.product_price, obj.getPrice()));
        }
    }

    @Override
    public int getItemCount() {
        // + 1 for header of footer
        return mList.size() + 1;
    }

    class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView price;

        private ProductViewHolderListener mListener;

        public ProductViewHolder(View itemView, ProductViewHolderListener listener) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.name);
            price = (TextView) itemView.findViewById(R.id.price);

            mListener = listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(view, this.getAdapterPosition());
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        private HeaderViewHolderListener mListener;
        private TextView mBalanceText;

        public HeaderViewHolder(View itemView, HeaderViewHolderListener listener) {
            super(itemView);

            mBalanceText = (TextView) itemView.findViewById(R.id.balanceText);
            mListener = listener;
            mListener.onCreated(this);
            Log.d("ProductAdapter", "new HeaderViewHolder");
        }



        public TextView getBalanceText() {
            return mBalanceText;
        }
    }

    public interface ProductViewHolderListener {
        void onItemClick(View view, int index);
    }

    public interface HeaderViewHolderListener {
        void onCreated(HeaderViewHolder headerViewHolder);
    }
}