public class Order {

    private long orderID;
    private long timeStamp;
    private Side side;
    private int quantity;
    private long price;
    private OrderType orderType;
    private Status status;
    private long participantID;

    public Order(long orderID, long timeStamp, Side side, int quantity, long price, OrderType orderType, Status status, long participantID) {
        this.orderID = orderID;
        this.timeStamp = timeStamp;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.orderType = orderType;
        this.status = status;
        this.participantID = participantID;
    }

    public long getOrderID() { return orderID; }
    public long getTimeStamp() { return timeStamp; }
    public Side getSide() { return side; }
    public int getQuantity() { return quantity; }
    public long getPrice() { return price; }
    public OrderType getOrderType() { return orderType; }
    public Status getStatus() { return status; }
    public long getParticipantID() { return participantID; }

    @Override
    public String toString() {
        return "Order{" +
                "orderID=" + orderID +
                ", timeStamp=" + timeStamp +
                ", side='" + side + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", orderType='" + orderType + '\'' +
                ", participantID=" + participantID +
                ", status='" + status + '\'' +
                '}';
    }
}