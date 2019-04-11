package bowen.test.chat.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bowen.test.chat.Constants;
import bowen.test.chat.R;
import bowen.test.chat.bean.MessageBean;
import bowen.test.chat.tools.DateDistance;

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
        ViewHolder holder = new ViewHolder();
        int viewType = getItemViewType(position);

        switch (viewType){
            case VIEW_TYPE_SELF:
                convertView = View.inflate(mContext, R.layout.chat_item_right,null);
                break;
            case VIEW_TYPE_OTHER:
                convertView = View.inflate(mContext, R.layout.chat_item,null);
                break;
        }


        holder.title = convertView.findViewById(R.id.tv_chat_item_name);
        holder.content = convertView.findViewById(R.id.tv_chat_item_message);
        holder.name = convertView.findViewById(R.id.tv_chat_item_name_info);

       /* if(convertView == null){
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.chat_item,null);
            holder.title = convertView.findViewById(R.id.tv_chat_item_name);
            holder.content = convertView.findViewById(R.id.tv_chat_item_message);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }*/

        MessageBean item = getItem(position);
        String showStr = item.getAddress() + "\n" + item.getTime();
        boolean neesShowTime = true;
        if(position != 0){
            MessageBean upItem = getItem(position - 1);
            neesShowTime = showTime(upItem.getTime(),item.getTime());
        }
        holder.title.setVisibility(neesShowTime ? View.VISIBLE : View.GONE);
        holder.content.setText(item.getMessage());
        holder.title.setText(item.getTime());
        holder.name.setText(item.getAddress());
        holder.name.setGravity(Gravity.CENTER);
//        int gravity = item.isSelf() ? Gravity.RIGHT : Gravity.LEFT;
//        holder.content.setGravity(gravity);
//        holder.title.setGravity(gravity);

        return convertView;
    }

    boolean showTime(String date1,String date2){
        boolean result = true;
        try {
            long[] times = DateDistance.getDistanceTimes(date1, date2);
            result = times[0] > 0 || times[1] > 0 || times[2] > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private final int VIEW_TYPE_SELF =  1;
    private final int VIEW_TYPE_OTHER =  2;
    @Override
    public int getItemViewType(int position) {
        MessageBean item = getItem(position);
        return item.isSelf() ? VIEW_TYPE_SELF : VIEW_TYPE_OTHER;
    }

    class ViewHolder{
        TextView title;
        TextView content;
        TextView name;
    }
}
