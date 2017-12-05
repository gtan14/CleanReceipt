
package com.example.cleanreceipt;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.cleanreceipt.R;

import java.util.ArrayList;
import java.util.List;


public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.Myholder> {
    List<ReceiptModel> dataModelArrayList;
    ReceiptLookUp receiptLookUp;

    public RecycleAdapter(List<ReceiptModel> dataModelArrayList, ReceiptLookUp receiptLookUp) {
        this.dataModelArrayList = dataModelArrayList;
        this.receiptLookUp = receiptLookUp;
    }

    class Myholder extends RecyclerView.ViewHolder{
        TextView name,date,price, cate, location;
        ImageView image;

        public Myholder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            price = (TextView) itemView.findViewById(R.id.price);
            cate = (TextView) itemView.findViewById(R.id.category);
            location = (TextView) itemView.findViewById(R.id.location);
            image = (ImageView) itemView.findViewById(R.id.image);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bitmap bm = ((BitmapDrawable) image.getDrawable()).getBitmap();
                    final Dialog nagDialog = new Dialog(receiptLookUp.ctx, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                    nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    nagDialog.setCancelable(false);
                    nagDialog.setContentView(R.layout.fullscreen_image);
                    ImageView fullscreenImagePortrait = (ImageView) nagDialog.findViewById(R.id.fullscreenImagePortrait);
                    ImageView fullscreenImageLandscape = (ImageView) nagDialog.findViewById(R.id.fullscreenImageLandscape);

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    receiptLookUp.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;


                    fullscreenImagePortrait.setImageBitmap(bm);
                    //bm.recycle();
                    fullscreenImagePortrait.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            nagDialog.dismiss();
                        }
                    });

                    nagDialog.show();
                }
            });
        }
    }

    @Override
    public Myholder onCreateViewHolder(ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview,null);
         return new Myholder(view);

    }

    @Override
    public void onBindViewHolder(Myholder holder, int position) {
        ReceiptModel dataModel=dataModelArrayList.get(position);
        holder.name.setText(dataModel.getName());
        holder.date.setText(dataModel.getDate());
        holder.price.setText(dataModel.getPrice());
        holder.cate.setText(dataModel.getCategory());
        holder.location.setText(dataModel.getLocation());
        holder.image.setImageBitmap(dataModel.getImage());
    }

    @Override
    public int getItemCount() {
        return dataModelArrayList.size();
    }
}
