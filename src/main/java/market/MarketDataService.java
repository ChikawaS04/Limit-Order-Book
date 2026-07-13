package market;

import engine.BookView;

public class MarketDataService {

    private final BookView bookView;

    public MarketDataService(BookView bookView) {
        this.bookView = bookView;
    }

    public long getMidpoint() {
        long bestBid = bookView.getBestBid();
        long bestAsk = bookView.getBestAsk();
        if (bestBid == -1L || bestAsk == -1L) return -1L ;
        return (bestBid + bestAsk) / 2 ;
    }
}