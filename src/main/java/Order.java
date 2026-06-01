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

        if (quantity <= 0) { throw new IllegalArgumentException("Quantity must be positive"); }
        if (orderID <= 0) { throw new IllegalArgumentException("Order ID must be positive"); }
        if (participantID <= 0) { throw new IllegalArgumentException("Participant ID must be positive"); }
        if (timeStamp <= 0) { throw new IllegalArgumentException("Timestamp must be positive"); }
        if (side == null) { throw new IllegalArgumentException("Side cannot be null"); }
        if (orderType == null) { throw new IllegalArgumentException("Order type cannot be null"); }
        if (orderType == OrderType.LIMIT && price <= 0) { throw new IllegalArgumentException("Limit order price must be positive");}

        this.orderID = orderID;
        this.timeStamp = timeStamp;
        this.side = side;
        this.quantity = quantity;
        this.price = (orderType == OrderType.MARKET ? 0 : price);
        this.orderType = orderType;
        this.status = Status.OPEN;
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
                ", status='" + status + '\'' +
                ", participantID=" + participantID +
                '}';
    }
}
