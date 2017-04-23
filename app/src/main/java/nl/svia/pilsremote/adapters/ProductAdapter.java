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

    private final static int TYPE_PRODUCT = 0;

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

    public ProductAdapter(Context context, ProductViewHolderListener userListener) {
        this.mContext = context;
        this.mProductViewHolderListener = userListener;

        Log.d(TAG, "Product adapter created");
    }

    @Override
    public int getItemViewType(int position) {
        // This view doesn't have a header, so we always return TYPE_PRODUCT
        return TYPE_PRODUCT;
    }

    public ProductModel getItem(int i) {
        return mList.get(i);
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

        if (viewType == TYPE_PRODUCT) {
            v = LayoutInflater.from(c).inflate(R.layout.list_item_product, viewGroup, false);
            return new ProductViewHolder(v, mProductViewHolderListener);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof ProductViewHolder) {
            ProductViewHolder userViewHolder = (ProductViewHolder) viewHolder;

            ProductModel obj = getItem(i);
            String name = obj.getName();

            userViewHolder.title.setText(name);
            userViewHolder.price.setText(mContext.getString(R.string.product_price, obj.getPrice()));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
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

    public interface ProductViewHolderListener {
        void onItemClick(View view, int index);
    }
}