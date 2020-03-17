package co.giftree.maskeyes.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

import co.giftree.maskeyes.R;
import co.giftree.maskeyes.model.MaskInfo;
import co.giftree.maskeyes.util.DataUtil;
import co.giftree.maskeyes.util.DistanceCalculator;

public class MaskInfoAdapter extends RecyclerView.Adapter<MaskInfoAdapter.SimpleViewHolder> {

    public interface MapClickListener {
        void onMoveToPosition(MaskInfo maskInfo);
    }

    private final Context mContext;
    private final List<MaskInfo> mItems;
    private int mCurrentItemId = 0;
    private MapClickListener mapClickListener;

    private double currentLat;
    private double currentLng;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {

        public final ImageView type_icon;

        public final TextView name_txt;
        public final TextView remain_stat_txt;
        public final TextView stock_at_txt;
        public final TextView distance_txt;
        public final TextView address_txt;

        public final ImageButton move_to_map_btn;
        public final View address_view;

        private final View root_view;


        public SimpleViewHolder(View view) {
            super(view);
            type_icon = view.findViewById(R.id.type_icon);

            name_txt = view.findViewById(R.id.name_txt);
            remain_stat_txt = view.findViewById(R.id.remain_stat_txt);
            stock_at_txt = view.findViewById(R.id.stock_at_txt);
            address_txt = view.findViewById(R.id.address_txt);
            distance_txt = view.findViewById(R.id.distance_txt);
            address_view = view.findViewById(R.id.address_view);
            move_to_map_btn = view.findViewById(R.id.move_to_map_btn);
            root_view = view.findViewById(R.id.root_view);

        }
    }
    public void setClickListener(MapClickListener  mapClickListener) {
        this.mapClickListener = mapClickListener;
    }

    public void setCurrPos(double lat, double lng){
        this.currentLat = lat;
        this.currentLng = lng;
    }

    public MaskInfoAdapter(Context context, List<MaskInfo> list) {
        mContext = context;
        mItems = list;
    }

    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.item_info, parent, false);
        return new SimpleViewHolder(view);
    }



    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {

        final MaskInfo maskInfo = mItems.get(position);




        holder.type_icon.setImageDrawable(DataUtil.findTypeDrawable(mContext, maskInfo.getType()));

        holder.name_txt.setText(maskInfo.getName());

        String remain_stat = String.format(mContext.getString(R.string.mask_stat_format), maskInfo.getRemain_stat());
        holder.remain_stat_txt.setText(remain_stat);

        holder.remain_stat_txt.setTextColor(DataUtil.convertColorRemainStat(mContext, maskInfo.getRemain_stat()));
        String stock_at = !maskInfo.getStock_at().equals("") ? maskInfo.getStock_at() : mContext.getString(R.string.unknown);
        String stock_at_with_format = String.format(mContext.getString(R.string.stock_at_format), stock_at);
        holder.stock_at_txt.setText(stock_at_with_format);
        holder.address_txt.setText(maskInfo.getAddr());

        try{

            double cal_dis = DistanceCalculator.distance(currentLat, currentLng, maskInfo.getLat(), maskInfo.getLng(), "K");
//                        double result = Math.round(Math.abs(cal_dis)*100d);

            DecimalFormat format = new DecimalFormat("0.#");
            String dis_str = format.format(cal_dis) + " km";

            if(cal_dis>10000){
                holder.distance_txt.setVisibility(View.GONE);
            }else{
                holder.distance_txt.setVisibility(View.VISIBLE);
                holder.distance_txt.setText(String.format(mContext.getString(R.string.distance_format), dis_str));
            }



        }catch (Exception ex){
            holder.distance_txt.setVisibility(View.GONE);
        }

        holder.address_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sdk_Version = android.os.Build.VERSION.SDK_INT;
                if(sdk_Version < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(maskInfo.getAddr());   // Assuming that you are copying the text from a TextView
                    Toast.makeText(mContext, mContext.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
                }
                else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText(mContext.getString(R.string.address), maskInfo.getAddr());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mContext, mContext.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
                }
            }
        });



        holder.root_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mapClickListener!=null){
                    mapClickListener.onMoveToPosition(maskInfo);
                }
            }
        });
        holder.move_to_map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mapClickListener!=null){
                    mapClickListener.onMoveToPosition(maskInfo);
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
