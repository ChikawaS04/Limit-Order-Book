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
        }
    }
}
