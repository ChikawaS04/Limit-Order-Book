public class Main {
    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();

        long alice = IDGenerator.nextParticipantID();
        long bob = IDGenerator.nextParticipantID();
        long charlie = IDGenerator.nextParticipantID();

        // Seed the book with resting orders (no crosses)
        Order bid1 = new Order(IDGenerator.nextOrderID(), alice, Side.BUY, 9900, 100, System.nanoTime());
        Order bid2 = new Order(IDGenerator.nextOrderID(), bob, Side.BUY, 9800, 50, System.nanoTime());
        Order ask1 = new Order(IDGenerator.nextOrderID(), alice, Side.SELL, 10100, 80, System.nanoTime());
        Order ask2 = new Order(IDGenerator.nextOrderID(), charlie, Side.SELL, 10200, 60, System.nanoTime());

        engine.addOrder(bid1);
        engine.addOrder(bid2);
        engine.addOrder(ask1);
        engine.addOrder(ask2);

        System.out.println("=== Initial Book ===");
        engine.printBook();

        // Partial match: Charlie buys 50 at 10100, fills 50 of ask1's 80
        Order crossingBuy = new Order(IDGenerator.nextOrderID(), charlie, Side.BUY, 10100, 50, System.nanoTime());
        engine.addOrder(crossingBuy);

        System.out.println("\n=== After Partial Fill (buy 50 @ 10100) ===");
        engine.printBook();

        // Full match: Bob sells 100 at 9900, fills all of bid1
        Order crossingSell = new Order(IDGenerator.nextOrderID(), bob, Side.SELL, 9900, 100, System.nanoTime());
        engine.addOrder(crossingSell);

        System.out.println("\n=== After Full Fill (sell 100 @ 9900) ===");
        engine.printBook();

        // Multi-level match: Alice buys 100 at 10200, sweeps remaining ask1 (30) and into ask2 (60)
        Order sweepBuy = new Order(IDGenerator.nextOrderID(), alice, Side.BUY, 10200, 100, System.nanoTime());
        engine.addOrder(sweepBuy);

        System.out.println("\n=== After Multi-Level Sweep (buy 100 @ 10200) ===");
        engine.printBook();

        // Cancellation
        engine.cancelOrder(bid2.getOrderID());

        System.out.println("\n=== After Cancelling Bid @ 9800 ===");
        engine.printBook();

        // Trade history
        System.out.println("\n=== Trade History ===");
        for (Trade trade : engine.getTrades()) {
            System.out.println(trade);
        }
    }
}