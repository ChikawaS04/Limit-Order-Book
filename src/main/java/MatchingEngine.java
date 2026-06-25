import java.util.Comparator;
import java.util.Deque;
import java.util.TreeMap;

public class MatchingEngine {

    private final TreeMap<Long, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());

    private final TreeMap<Long, Deque<Order>> asks = new TreeMap<>();
}
