import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;

public class MatchingEngine {

    private final TreeMap<Long, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());

    private final TreeMap<Long, Deque<Order>> asks = new TreeMap<>();

    private void matchBuy(Order buyOrder) {
        while (buyOrder.getQuantity() > 0 && !asks.isEmpty()) {
            Map.Entry<Long, Deque<Order>> bestAskEntry  = asks.firstEntry();
            long askPrice = bestAskEntry.getKey();

            if (askPrice > buyOrder.getPrice()) break;

            Deque<Order> bestAskQueue = bestAskEntry.getValue();
            Order askOrder = bestAskQueue.peek();

            int quantityMatched = Math.min(buyOrder.getQuantity(), askOrder.getQuantity());
            buyOrder.fill(quantityMatched);
            askOrder.fill(quantityMatched);

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

            if (bidPrice > sellOrder.getPrice()) break;

            Deque<Order> bestBidQueue = bestBidEntry.getValue();
            Order askOrder = bestBidQueue.peek();

            int quantityMatched = Math.min(sellOrder.getQuantity(), askOrder.getQuantity());
            sellOrder.fill(quantityMatched);
            askOrder.fill(quantityMatched);

            if (askOrder.getQuantity() == 0) {
                bestBidQueue.removeFirst();
                if (bestBidQueue.isEmpty()) {
                    asks.pollFirstEntry();
                }
            }
        }
    }
}
