import java.io.Serializable;


final class ChatMessage implements Serializable {
    private int type;
    private String message;
    private static final long serialVersionUID = 6898543889087L;
    public ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }
    public int getType() {
        return this.type;
    }

    public String getMessage() {
        return message;
    }
    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
}
