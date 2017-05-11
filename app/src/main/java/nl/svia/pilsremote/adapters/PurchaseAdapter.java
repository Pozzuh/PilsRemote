package nl.svia.pilsremote.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import nl.svia.pilsremote.R;
import nl.svia.pilsremote.misc.ProductModel;
import nl.svia.pilsremote.misc.PurchaseModel;

public class PurchaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PurchaseAdapter";

    private Context mContext;
    private SparseArray<String> mProductMap;

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    private final static int TYPE_PURCHASE = 0;

    private Comparator<PurchaseModel> mComparator = new Comparator<PurchaseModel>() {
        @Override
        public int compare(PurchaseModel a, PurchaseModel b) {
            long timeA = a.getTimestamp();
            long timeB = b.getTimestamp();

            if (timeA == timeB) {
                return 0;
            } else if (timeA > timeB) {
                return -1;
            } else {
                return 1;
            }
        }
    };

    private final SortedList<PurchaseModel> mList = new SortedList<>(PurchaseModel.class,
            new SortedList.Callback<PurchaseModel>() {
                @Override
                public int compare(PurchaseModel a, PurchaseModel b) {
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
                public boolean areContentsTheSame(PurchaseModel oldItem, PurchaseModel newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areItemsTheSame(PurchaseModel item1, PurchaseModel item2) {
                    return item1.getId() == item2.getId();
                }
            });

    public PurchaseAdapter(Context context) {
        this(context, null);
    }

    public PurchaseAdapter(Context context, @Nullable SparseArray<String> productMap) {
        this.mContext = context;
        this.mProductMap = productMap;

        Log.d(TAG, "Product adapter created");
    }

    @Override
    public int getItemViewType(int position) {
        // This view doesn't have a header, so we always return TYPE_PRODUCT
        return TYPE_PURCHASE;
    }

    public PurchaseModel getItem(int i) {
        return mList.get(i);
    }

    public void add(PurchaseModel model) {
        mList.add(model);
    }

    public void remove(PurchaseModel model) {
        mList.remove(model);
    }

    public void add(List<PurchaseModel> models) {
        mList.addAll(models);
    }

    public void remove(List<PurchaseModel> models) {
        mList.beginBatchedUpdates();
        for (PurchaseModel model : models) {
            mList.remove(model);
        }
        mList.endBatchedUpdates();
    }

    public void removeAll() {
        mList.clear();
    }

    public void replaceAll(List<PurchaseModel> models) {
        mList.beginBatchedUpdates();

        mList.clear();
        mList.addAll(models);
        mList.endBatchedUpdates();
    }

    public void replaceOne(PurchaseModel model) {
        mList.beginBatchedUpdates();

        mList.clear();
        mList.add(model);

        mList.endBatchedUpdates();
    }

    public SparseArray<String> getProductMap() {
        return mProductMap;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        Context c = viewGroup.getContext();

        if (viewType == TYPE_PURCHASE) {
            v = LayoutInflater.from(c).inflate(R.layout.list_item_purchase, viewGroup, false);
            return new PurchaseViewHolder(v);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof PurchaseViewHolder) {
            PurchaseViewHolder purchaseViewHolder = (PurchaseViewHolder) viewHolder;

            PurchaseModel obj = getItem(i);

            String nameText = mProductMap == null ? String.valueOf(obj.getProductId()) :
                    mProductMap.get(obj.getProductId());

            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(String.valueOf(nameText.charAt(0)),
                            mColorGenerator.getColor(nameText));

            CharSequence timeText = DateUtils.getRelativeTimeSpanString(obj.getTimestamp(),
                    System.currentTimeMillis(), 0);

            purchaseViewHolder.nameView.setText(nameText);
            purchaseViewHolder.imageView.setImageDrawable(drawable);
            purchaseViewHolder.priceView.setText(mContext.getString(R.string.product_price, obj.getPrice()));
            purchaseViewHolder.dateView.setText(timeText);
            purchaseViewHolder.amountView.setText(obj.getAmount() + "x");
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class PurchaseViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView priceView;
        TextView dateView;
        TextView amountView;

        public PurchaseViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.image);
            nameView = (TextView) itemView.findViewById(R.id.name);
            priceView = (TextView) itemView.findViewById(R.id.price);
            dateView = (TextView) itemView.findViewById(R.id.date);
            amountView = (TextView) itemView.findViewById(R.id.amount);

        }
    }
}