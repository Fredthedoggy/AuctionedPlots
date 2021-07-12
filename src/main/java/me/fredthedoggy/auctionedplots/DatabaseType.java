package me.fredthedoggy.auctionedplots;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseType {
    public List<Plot> plots;

    public class Plot {
        public String plot;
        public UUID owner;
        public long nexttime;
        public Sell selling;
        public Plot(String plot, UUID owner) {
            this.plot = plot;
            this.owner = owner;
            this.nexttime = 0;
            this.selling = null;
        }
    }

    public class Sell {
        public UUID currentBidder;
        public Map<UUID, Integer> bids;
        public Sell() {
            bids = new HashMap<>();
        }
    }
}
