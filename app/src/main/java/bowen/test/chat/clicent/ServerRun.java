package bowen.test.chat.clicent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ServerRun implements Runnable {
    @Override
    public void run() {
        service();
    }

    //客户端列表
     static List<String> mReqList = new ArrayList<>();
     static LinkedList<byte[]> mMessageQue = new LinkedList<>();

    private final static int PORT = 6986;

    public  void service() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            while (true) {
                try {
                    //1)接受信息
                    DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(request);
                    byte[] requestData = request.getData();
                    InetAddress address = request.getAddress();
                    int length = request.getLength();
                    SocketAddress socketAddress = request.getSocketAddress();
                    int port = request.getPort();
                    //将物理地址添加进去
                    mReqList.add(socketAddress.toString() + ":" + port);
                    mMessageQue.add(requestData);
                    System.out.println(" :: request ::: " +request.toString());

                    //2)回复信息
                    String daytime = new Date().toString();
                    byte[] data = daytime.getBytes("ASCII");
                    DatagramPacket response = new DatagramPacket(data, data.length, request.getAddress(), request.getPort());
                    socket.send(response);
                    System.out.println(daytime + " " + request.getAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
