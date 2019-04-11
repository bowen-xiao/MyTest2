package bowen.test.chat.bean;

import java.io.Serializable;

public class MessageBean implements Serializable {
    String address;
    String message;
    String time;
    String name;
    boolean isSelf;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "MessageBean{" +
                "address='" + address + '\'' +
                ", message='" + message + '\'' +
                ", time='" + time + '\'' +
                ", name='" + name + '\'' +
                ", isSelf=" + isSelf +
                '}';
    }
}
