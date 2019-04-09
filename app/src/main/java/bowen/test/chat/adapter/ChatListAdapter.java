package bowen.test.chat.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bowen.test.chat.R;
import bowen.test.chat.bean.MessageBean;

public class ChatListAdapter extends BaseAdapter {
    List<MessageBean> mDatas = new ArrayList<>();
    Context mContext;
    public ChatListAdapter(Context context,List<MessageBean> list){
        mContext = context;
        mDatas = list;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public MessageBean getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.chat_item,null);
            holder.content = convertView.findViewById(R.id.tv_chat_item_message);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        MessageBean item = getItem(position);
        String showStr = item.getAddress() + "\n" + item.getTime() + "\n" + item.getMessage();
        holder.content.setText(showStr);
        int gravity = item.isSelf() ? Gravity.RIGHT : Gravity.LEFT;
        holder.content.setGravity(gravity);
        return convertView;
    }

    class ViewHolder{
        TextView content;
    }
}
