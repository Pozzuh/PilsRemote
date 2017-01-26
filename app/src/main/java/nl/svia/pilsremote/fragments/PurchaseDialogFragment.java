package nl.svia.pilsremote.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import nl.svia.pilsremote.R;
import nl.svia.pilsremote.misc.NetworkFragmentGetter;
import nl.svia.pilsremote.misc.ProductModel;
import nl.svia.pilsremote.views.AmountPicker;

public class PurchaseDialogFragment extends DialogFragment
        implements DialogInterface.OnShowListener {
    private static final String TAG = "PurchaseDialogFragment";
    private static final String ARGUMENT_USER_ID = "ARG_USER_ID";
    private static final String ARGUMENT_PRODUCT = "ARG_PRODUCT";

    private NetworkFragment mNetworkFragment;

    private LinearLayout mProductPicker;
    private RelativeLayout mPincodePicker;

    private TextView mProductNameView;
    private TextView mProductPriceView;
    private AmountPicker mAmountView;
    private TextView mPinView;

    private ProgressBar mProgressBar;
    private TextView mEmptyView;

    private ProductModel mProduct;
    private int mUserId;

    private OnPurchaseListener mListener;

    public PurchaseDialogFragment() {
        // Empty constructor
    }

    public static PurchaseDialogFragment newInstance(int userId, Bundle product) {
        PurchaseDialogFragment fragment = new PurchaseDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARGUMENT_USER_ID, userId);
        bundle.putBundle(ARGUMENT_PRODUCT, product);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NetworkFragmentGetter) {
            mNetworkFragment = ((NetworkFragmentGetter) context).getNetworkFragment();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement NetworkFragmentGetter");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNetworkFragment = null;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        mUserId = args.getInt(ARGUMENT_USER_ID);
        mProduct = ProductModel.fromBundle(args.getBundle(ARGUMENT_PRODUCT));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.buy_product_dialog, null);

        mProductPicker = (LinearLayout) v.findViewById(R.id.product_picker);
        mPincodePicker = (RelativeLayout) v.findViewById(R.id.pincode_picker);

        mProductNameView = (TextView) mProductPicker.findViewById(R.id.product);
        mProductPriceView = (TextView) mProductPicker.findViewById(R.id.price);
        mAmountView = (AmountPicker) mProductPicker.findViewById(R.id.amount);

        mPinView = (TextView) mPincodePicker.findViewById(R.id.pincode);

        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mEmptyView = (TextView) v.findViewById(R.id.emptyText);

        mProductNameView.setText(mProduct.getName());
        mProductPriceView.setText(getString(R.string.purchase_price_hint, mProduct.getPrice()));

        mAmountView.setOnValueChangeListener(new AmountPicker.OnValueChangeListener() {
            @Override
            public void onValueChanged(int value) {
                mProductPriceView.setText(
                        getString(R.string.purchase_price_hint, mProduct.getPrice() * value));
            }
        });

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        Log.d(TAG, "initing");
        builder.setView(v)
                // Add action buttons
                .setPositiveButton(R.string.confirm_purchase, null)
                .setNegativeButton(R.string.cancel_purchase, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PurchaseDialogFragment.this.getDialog().cancel();
                    }
                })
                .setIcon(R.drawable.ic_attach_money)
                .setTitle(R.string.confirm_purchase_hint);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(this);

        return alertDialog;
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        Log.d(TAG, "on show");
        Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "On click!");
                final AlertDialog alertDialog =
                        (AlertDialog) PurchaseDialogFragment.this.getDialog();
                alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);

                int pin;
                try {
                    pin = Integer.parseInt(mPinView.getText().toString());
                } catch (Exception ignored) {
                    mPinView.setError(getString(R.string.invalid_pincode));
                    alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                    alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(true);
                    return;
                }

                PurchaseDialogFragment.this.setLoading(true, null);

                Log.d(TAG, "On buying!");
                mNetworkFragment.buyProducts(pin, mUserId, mProduct.getId(),
                        mAmountView.getValue(), new Response.Listener<Boolean>() {
                            @Override
                            public void onResponse(Boolean response) {
                                if (mListener != null) {
                                    mListener.onPurchase(mProduct, mAmountView.getValue());
                                }

                                PurchaseDialogFragment.this.getDialog().dismiss();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "error purchase");

                                PurchaseDialogFragment.this.setLoading(false, null);
                                mPinView.setError(getString(R.string.invalid_pincode));
                                alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                                alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(true);
                            }
                        });
            }
        });
    }

    public void SetOnPurchaseListener(OnPurchaseListener listener) {
        mListener = listener;
    }

    private void setLoading(boolean flag, @Nullable CharSequence error) {
        if (flag) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProductPicker.setVisibility(View.INVISIBLE);
            mPincodePicker.setVisibility(View.INVISIBLE);
            mEmptyView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);

            if (error == null) {
                mProductPicker.setVisibility(View.VISIBLE);
                mPincodePicker.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.INVISIBLE);
            } else {
                mProductPicker.setVisibility(View.INVISIBLE);
                mPincodePicker.setVisibility(View.INVISIBLE);
                mEmptyView.setText(error);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnPurchaseListener {
        void onPurchase(ProductModel product, int amount);
    }
}
