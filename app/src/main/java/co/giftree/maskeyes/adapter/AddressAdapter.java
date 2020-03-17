package co.giftree.maskeyes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.giftree.maskeyes.R;
import co.giftree.maskeyes.model.AddressInfo;
import co.giftree.maskeyes.model.MaskInfo;
import co.giftree.maskeyes.util.DataUtil;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    public interface OnClickListener {
        void onMoveToPosition(AddressInfo addressInfo);
    }

    private final Context mContext;
    private final List<AddressInfo> mItems;
    private int mCurrentItemId = 0;
    private OnClickListener mapClickListener;

    public static class AddressViewHolder extends RecyclerView.ViewHolder {


        public final TextView address_txt;

        public final View root_view;


        public AddressViewHolder(View view) {
            super(view);

            address_txt = view.findViewById(R.id.address_txt);
            root_view = view.findViewById(R.id.root_view);

        }
    }
    public void setClickListener(OnClickListener  mapClickListener) {
        this.mapClickListener = mapClickListener;
    }


    public AddressAdapter(Context context, List<AddressInfo> list) {
        mContext = context;
        mItems = list;
    }

    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, final int position) {

        final AddressInfo addressInfo = mItems.get(position);




        holder.address_txt.setText(addressInfo.getAddress());




        holder.root_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mapClickListener!=null){
                    mapClickListener.onMoveToPosition(addressInfo);
                }
            }
        });

    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
