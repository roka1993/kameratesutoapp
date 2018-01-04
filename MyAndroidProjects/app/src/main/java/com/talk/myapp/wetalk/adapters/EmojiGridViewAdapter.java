package com.talk.myapp.wetalk.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.talk.myapp.wetalk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 407973884 on 2018/1/2.
 */

public class EmojiGridViewAdapter extends BaseAdapter {
    private Context context;
    private List itemList = new ArrayList();
    private int selectedItemIndex = -1;

    public EmojiGridViewAdapter (Context context,List iconList) {
        this.context = context;
        this.itemList = iconList;
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        this.selectedItemIndex = selectedItemIndex;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EmojiListHolder holder = null;
        if (convertView == null) {
            holder = new EmojiListHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.emoji_list_item, null);
            holder.itemimage = convertView.findViewById(R.id.emoji_image);
           // holder.itemimage.setAlpha(0.3f);
            holder.itemimage.setImageResource((int)itemList.get(position));
            convertView.setTag(holder);
        } else {
            holder = (EmojiListHolder) convertView.getTag();
            holder.itemimage.setImageResource((int)itemList.get(position));
        }
        if (selectedItemIndex == position) {
            convertView.setAlpha(1);
//            ViewGroup.LayoutParams params = convertView.getLayoutParams();
//            params.height=60 * (int)context.getResources().getDisplayMetrics().density;
//            params.width=60 *(int)context.getResources().getDisplayMetrics().density;
//            convertView.setLayoutParams(params);
        } else {
            convertView.setAlpha(0.5f);
//            ViewGroup.LayoutParams params = convertView.getLayoutParams();
//            params.height=50*(int)context.getResources().getDisplayMetrics().density;
//            params.width=50*(int)context.getResources().getDisplayMetrics().density;
//            convertView.setLayoutParams(params);
        }
        return convertView;
    }

    public final class EmojiListHolder {
        //列表图片
        ImageView itemimage;
    }
}
