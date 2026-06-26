import java.util.*;

public class MatchingEngine {

    private final TreeMap<Long, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());

    private final TreeMap<Long, Deque<Order>> asks = new TreeMap<>();

    private final List<Trade> trades = new ArrayList<>();

    private final Map<Long, Order> openOrders = new HashMap<>();

    private void matchBuy(Order buyOrder) {
        while (buyOrder.getQuantity() > 0 && !asks.isEmpty()) {
            Map.Entry<Long, Deque<Order>> bestAskEntry  = asks.firstEntry();
            long askPrice = bestAskEntry.getKey();

            if (askPrice > buyOrder.getPrice()) break;

            Deque<Order> bestAskQueue = bestAskEntry.getValue();
            Order askOrder = bestAskQueue.getFirst();

            int quantityMatched = Math.min(buyOrder.getQuantity(), askOrder.getQuantity());
            buyOrder.fill(quantityMatched);
            askOrder.fill(quantityMatched);
            trades.add(new Trade(
                    IDGenerator.nextTradeID(),
                    buyOrder.getOrderID(),
                    askOrder.getOrderID(),
                    buyOrder.getParticipantID(),
                    askOrder.getParticipantID(),
                    askPrice,
                    quantityMatched,
                    System.nanoTime()
            ));

            if (askOrder.getQuantity() == 0) {
                bestAskQueue.removeFirst();
                if (bestAskQueue.isEmpty()) {
                    asks.pollFirstEntry();
                }
            }
        }
    }

    private void matchSell(Order sellOrder) {
        while (sellOrder.getQuantity() > 0 && !bids.isEmpty()) {
            Map.Entry<Long, Deque<Order>> bestBidEntry = bids.firstEntry();
            long bidPrice = bestBidEntry.getKey();

            if (bidPrice < sellOrder.getPrice()) break;

            Deque<Order> bestBidQueue = bestBidEntry.getValue();
            Order bidOrder = bestBidQueue.getFirst();

            int quantityMatched = Math.min(sellOrder.getQuantity(), bidOrder.getQuantity());
            sellOrder.fill(quantityMatched);
            bidOrder.fill(quantityMatched);
            trades.add(new Trade(
                    IDGenerator.nextTradeID(),
                    bidOrder.getOrderID(),
                    sellOrder.getOrderID(),
                    bidOrder.getParticipantID(),
                    sellOrder.getParticipantID(),
                    bidPrice,
                    quantityMatched,
                    System.nanoTime()
            ));

            if (bidOrder.getQuantity() == 0) {
                bestBidQueue.removeFirst();
                if (bestBidQueue.isEmpty()) {
                    bids.pollFirstEntry();
                }
            }
        }
    }

    public void addOrder(Order order) {
        if (order.getSide() == Side.BUY) {
            matchBuy(order);
            if (order.getQuantity() > 0) addToBook(bids, order);
        } else {
            matchSell(order);
            if (order.getQuantity() > 0) addToBook(asks, order);
        }
    }

    private void addToBook(TreeMap<Long, Deque<Order>> book, Order order) {
        book.computeIfAbsent(order.getPrice(), k -> new ArrayDeque<>()).addLast(order);
    }

    public void cancelOrder(long orderID) {
        Order order = openOrders.remove(orderID);
        if (order == null || order.getQuantity() == 0) return;

        order.cancel();

        TreeMap<Long, Deque<Order>> book = (order.getSide() == Side.BUY) ? bids : asks;
        Deque<Order> queue = book.get(order.getPrice());
        if (queue != null) {
            queue.remove(order);
            if (queue.isEmpty()) book.remove(order.getPrice());
        }
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public void printBook() {
        System.out.println("Bids:");
        for (Map.Entry<Long, Deque<Order>> entry : bids.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Asks:");
        for (Map.Entry<Long, Deque<Order>> entry : asks.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}