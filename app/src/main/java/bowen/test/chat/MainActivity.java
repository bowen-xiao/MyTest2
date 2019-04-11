package bowen.test.chat;

import androidx.appcompat.app.AppCompatActivity;
import bowen.test.chat.adapter.ChatListAdapter;
import bowen.test.chat.bean.MessageBean;

import android.content.Context;
import android.database.DataSetObserver;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity {

    private static final String TAG =  "MainActivity";
//    TextView mListMsg;
//    ScrollView mScrollView;
    Button mBtnSend;
    EditText mInputText;
    ListView mList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mListMsg = findViewById(R.id.tv_list_clients);
//        mScrollView = findViewById(R.id.main_scrollview);
        mBtnSend = findViewById(R.id.btn_send);
        mInputText = findViewById(R.id.et_input);
        mList = findViewById(R.id.lv_chat_list);
        //消息处理
        initHandler();
        initListAdapter();
        //UI数据展示进程
        showThread();
        //心跳轮询
        sendAsk();
        //开始接受数据
        startReceived();
        //点击事件处理
        initBtnListener();
    }

    ChatListAdapter mAdapter;
    List<MessageBean> mDatas = new ArrayList<>();
    void initListAdapter(){
        mAdapter = new ChatListAdapter(this,mDatas);
        mList.setAdapter(mAdapter);
    }


    private void initBtnListener() {
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = mInputText.getText().toString();
                if(TextUtils.isEmpty(messageText)){return;}
                sendMessageToFriends(messageText);
                mInputText.setText("");
            }
        });
    }

    void sendMessageToFriends(final String message){
        try {
            final byte[] asciis = message.getBytes();

            new Thread(){
                @Override
                public void run() {
                    Iterator<String> iterator = mReqList.iterator();
                    while (iterator.hasNext()){
                        String next = iterator.next();
                        sendMsg(next,asciis);
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发出一个问候的信息
    void sendAsk(){
        if(mSendSocket == null){
            try {
                mSendSocket = new DatagramSocket(PORT + 13);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        new Thread(){
            @Override
            public void run() {
                //while (true){
                    seekClients();
                //}
            }
        }.start();
    }

    String askCode = "111111";
    String askCodeReplay = "111222";
    String askCodeExit = "222222";
    String mIpAddress;
    //扫描主机
    private void seekClients() {
        //1)获取自己的IP地址
        mIpAddress = getLocalIpAddress(this);
        Log.e(TAG," this is my ip :: " + mIpAddress);
        int lastIndexOf = mIpAddress.lastIndexOf(".");
        String firstName = mIpAddress.substring(0,lastIndexOf+1);
        String myName = mIpAddress.substring(lastIndexOf+1);
        //2)开始发消息
        try {
            logE("开始找朋友");
            for (int i = 0; i < 250; i++) {
                String hostAddress = firstName + i;
                sendMsg(hostAddress,askCode.getBytes());
            }
            //Thread.sleep(8000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //3)过一段时间再发送心跳数据
    }

    DatagramSocket mSendSocket;
    private void sendMsg (String address,byte[] data){

        try {
            InetAddress host = InetAddress.getByName(address);
            DatagramPacket response = new DatagramPacket(data, data.length, host, PORT);
            mSendSocket.send(response);
//            logE("发送消息 address:: " + address + " ::port: " + port + " :data:" + new String(data,"ASCII") );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    public  String getLocalIpAddress(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int i = wifiInfo.getIpAddress();
            return int2ip(i);
        } catch (Exception ex) {
            return " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
        }
        // return null;
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    public  String int2ip(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    final static int MSG_RECIEVER = 128233;
    final static int MSG_UPDATE_TITLE = 892365;
    Handler mHandler;
    //初始化
    private void initHandler() {
        mHandler= new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_RECIEVER:
                        updateUi((MessageBean) msg.obj);
                        break;
                    case MSG_UPDATE_TITLE:
                        setTitle((String)msg.obj);
                        break;
                }
            }
        };
    }

    private void updateUi(MessageBean bean){
//        StringBuilder sb = new StringBuilder();
        mDatas.add(bean);
        mAdapter.notifyDataSetChanged();
        mList.smoothScrollToPosition(mDatas.size());
//        mListMsg.setText(sb.toString());
//        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private static ReentrantLock mMessageLock = new ReentrantLock();
    private static Condition consumeCondition = mMessageLock.newCondition();
    private void showThread() {
        new Thread(){
            @Override
            public void run() {
                while (true){
                    showUI();
                }
            }
        }.start();
    }

    void showUI(){
        try {
            mMessageLock.lock();
            if(mMessageQue != null && mMessageQue.size() != 0){
//                showData();
                for (int i = 0; i < mMessageQue.size(); i++) {
                    MessageBean bytes = mMessageQue.get(i);
                    Message message = mHandler.obtainMessage();
                    message.what =  MSG_RECIEVER;
                    message.obj = bytes;
                    mHandler.sendMessage(message);
                    logE("显示内容 ：：： " + bytes);
                }
                mMessageQue.clear();
            }
            mMessageLock.unlock();
            Thread.sleep(200L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final static int PORT = 6986;

    static Set<String> mReqList = new HashSet<>();
    static LinkedList<MessageBean> mMessageQue = new LinkedList<>();
    void startReceived(){
        new Thread(){
            @Override
            public void run() {
                beginReceived();
            }
        }.start();
    }

    private void beginReceived() {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            while (true) {
                    //1)接受信息
                    DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(request);
                    byte[] requestData = request.getData();
                    InetAddress address = request.getAddress();
                    int length = request.getLength();
                    SocketAddress socketAddress = request.getSocketAddress();
                    int port = request.getPort();
                    mMessageLock.lock();

                    String reqData = new String(requestData,0,length);

                     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                     String time = format.format(Calendar.getInstance().getTime());
                     System.out.println("reqData： " + reqData);
                     String showData = " \n " +  address.getHostName() + ":" + port + "   :: " +time+" \n 说: " + reqData;
                    if(reqData.equals(askCode) || reqData.equals(askCodeReplay)){
                        //将物理地址添加进去
                        mReqList.add(address.getHostName());
                        updateTitle();
                    }
                     if(!reqData.equals(askCode)
                            && !reqData.equals(askCodeReplay)
                            //退出程序
                            && !reqData.equals(askCodeExit)
                    ){

                        MessageBean bean = new MessageBean();
                        bean.setMessage(reqData);
                        bean.setTime(time);
                        bean.setAddress(address.getHostName());
                        bean.setSelf(mIpAddress.equals(address.getHostName()));
                        mMessageQue.add(bean);

                    }
                    if(reqData.equals(askCodeExit)){
                        //为退出项
                        mReqList.remove(address.getHostName());
                        updateTitle();
                    }
                    logE("------收到消息 : " + showData);
                    logE("------mReqList size : " + mReqList.size());
//                    consumeCondition.signal();
                    mMessageLock.unlock();
                    //2)回复信息
                        logE("------getHostName : " + address.getHostName());
                        logE("------mIpAddress : " + mIpAddress);
                        logE("------reqData : " + reqData);
                    if(!mIpAddress.equals(address.getHostName()) && askCode.equals(reqData)){
                        byte[] data = askCodeReplay.getBytes();
                        logE("------回复消息222 : " + new String(data));
                        sendMsg(address.getHostName(),data);
                    }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void updateTitle(){
        Message message = mHandler.obtainMessage();
        message.what = MSG_UPDATE_TITLE;
        message.obj = "当前聊天室人数 : " + mReqList.size() + "人";
        mHandler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        sendExitCmd();
        super.onDestroy();
    }

    //退出程序时发出的消息
    private void sendExitCmd() {
        new Thread(){
            @Override
            public void run() {
                sendMessageToFriends(askCodeExit);
            }
        }.start();
    }

    void logE(String msg){
        Log.e(TAG,msg);
    }
}
