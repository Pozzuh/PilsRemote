package nl.svia.pilsremote.adapters;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.Comparator;
import java.util.List;

import nl.svia.pilsremote.R;
import nl.svia.pilsremote.misc.UserModel;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private UserViewHolderListener mUserViewHolderListener;
    private FooterViewHolderListener mFooterViewHolderListener;

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    private final static int TYPE_HEADER = 0;
    private final static int TYPE_USER = 1;
    private final static int TYPE_FOOTER = 2;

    private Comparator<UserModel> mComparator = new Comparator<UserModel>() {
        @Override
        public int compare(UserModel a, UserModel b) {
            return a.getName().compareTo(b.getName());
        }
    };

    private final SortedList<UserModel> mList = new SortedList<>(UserModel.class, new SortedList.Callback<UserModel>() {
        @Override
        public int compare(UserModel a, UserModel b) {
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
        public boolean areContentsTheSame(UserModel oldItem, UserModel newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(UserModel item1, UserModel item2) {
            return item1.getId() == item2.getId();
        }
    });

    public UserAdapter(Context context, UserViewHolderListener userListener,
                       FooterViewHolderListener footerListener) {
        this.mContext = context;
        this.mUserViewHolderListener = userListener;
        this.mFooterViewHolderListener = footerListener;
    }

    @Override
    public int getItemViewType(int position) {
        /*
         * We show the footer when there is only one user visible, but we never show the
         * header and the footer at the same time.
         */
        if (position == 0 && headerVisible()) {
            return TYPE_HEADER;
        } else if (position == 1 && !headerVisible()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_USER;
        }
    }

    public boolean headerVisible() {
        return mList.size() != 1;
    }

    public boolean footerVisible() {
        return !headerVisible();
    }

    public UserModel getItem(int i) {
        return headerVisible() ? mList.get(i - 1) : mList.get(i);
    }

    public void add(UserModel model) {
        mList.add(model);
    }

    public void remove(UserModel model) {
        mList.remove(model);
    }

    public void add(List<UserModel> models) {
        mList.addAll(models);
    }

    public void remove(List<UserModel> models) {
        mList.beginBatchedUpdates();
        for (UserModel model : models) {
            mList.remove(model);
        }
        mList.endBatchedUpdates();
    }

    public void replaceAll(List<UserModel> models) {
        mList.beginBatchedUpdates();

//        int length = mList.size();
//        for (int i = length - 1; i >= 0; i--) {
//            final UserModel model = mList.get(i);
//            if (!models.contains(model)) {
//                mList.remove(model);
//            }
//        }

        mList.clear();
        mList.addAll(models);
        mList.endBatchedUpdates();
    }

    /**
     * Deletes all but one user
     * @param user the user to keep
     */
    public void replaceOne(UserModel user) {
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
            v = LayoutInflater.from(c).inflate(R.layout.list_item_user_header, viewGroup, false);
            return new HeaderViewHolder(v);
        } else if (viewType == TYPE_FOOTER) {
            v = LayoutInflater.from(c).inflate(R.layout.list_item_user_footer, viewGroup, false);
            return new FooterViewHolder(v, mFooterViewHolderListener);
        } else {
            // TYPE_USER
            v = LayoutInflater.from(c).inflate(R.layout.list_item_user, viewGroup, false);
            return new UserViewHolder(v, mUserViewHolderListener);
        }

    }

    public String getLetters(String name) {
        String letter = "";

        String[] parts = name.split(" ");

        int consumed = 0;
        for (String part : parts) {
            if (part.length() < 1) {
                continue;
            }

            char c = part.charAt(0);

            if (Character.isUpperCase(c)) {
                consumed += 1;
                letter += c;
            }

            if (consumed > 1) {
                break;
            }
        }

        if (consumed == 0) {
            letter = name.substring(0, 1);
        }

        return letter;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof UserViewHolder) {
            UserViewHolder userViewHolder = (UserViewHolder)viewHolder;

            // Minus 1 because of the header
            UserModel obj = getItem(i);
            String name = obj.getName();

            // Create a new TextDrawable for our image's background
            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(getLetters(name), mColorGenerator.getColor(name));

            userViewHolder.letter.setImageDrawable(drawable);
            userViewHolder.title.setText(name);
        }

    }

    @Override
    public int getItemCount() {
        // + 1 for header of footer
        return mList.size() + 1;
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title;
        ImageView letter;

        private UserViewHolderListener mListener;

        public UserViewHolder(View itemView, UserViewHolderListener listener) {
            super(itemView);
            letter = (ImageView) itemView.findViewById(R.id.letter);
            title = (TextView) itemView.findViewById(R.id.name);

            mListener = listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(view, this.getAdapterPosition());
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Button button;
        EditText pin;

        private FooterViewHolderListener mListener;

        public FooterViewHolder(View itemView, FooterViewHolderListener listener) {
            super(itemView);

            pin = (EditText) itemView.findViewById(R.id.pincode);

            button = (Button) itemView.findViewById(R.id.submit);
            button.setOnClickListener(this);

            mListener = listener;
        }

        @Override
        public void onClick(View view) {
            Log.d("UserAdapter", mList.get(0).toString());

            try {
                int num = Integer.parseInt(pin.getText().toString());
                mListener.onSubmitClick(UserAdapter.this.getItem(0).getId(), num, pin);
            } catch (NumberFormatException ignored) {

            }
        }
    }

    public interface UserViewHolderListener {
        void onItemClick(View view, int index);
    }

    public interface FooterViewHolderListener {
        void onSubmitClick(int userId, int pin, EditText editText);
    }
}