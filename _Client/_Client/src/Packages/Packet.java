package Packages;

public class Packet {

    private String id;
    private String message;

    public Packet() {
        
    }
    
    public Packet(String id) {
        this.id = id;
    }

    public boolean isId(String id) {
        return this.id.equals(id);
    }

    public void analyzeMessage(String strReceived) {
        // Định dạng id:x###msg:z
        String[] msgSplit = strReceived.split("###");
        if(msgSplit.length != 2) {
            id = message = "";
            return;
        }
        // Lấy ID từ phần thứ nhất của gói
        String idPkg = msgSplit[0].trim();
        if(idPkg.startsWith("id:")) {
            id = idPkg.replaceFirst("id:", "");
        } else {
            id = "";
        }
        // Lấy message từ phần tứ 2 của gói
        String msgPkg = msgSplit[1].trim();
        if(msgPkg.startsWith("msg:")) {
            message = msgPkg.replaceFirst("msg:", "");
        } else {
            message = "";
        }
    }

    @Override
    public String toString() {
        return String.format("id:%s###msg:%s", getId(), getMessage());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

