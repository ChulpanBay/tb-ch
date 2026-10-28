package academy.backend.market_pulse.history;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PriceHistoryStoreContractTest;
import academy.backend.market_pulse.history.impl.PriceHistoryStoreSkeleton;

class PriceHistoryStoreSkeletonTest extends PriceHistoryStoreContractTest {

    @Override
    protected PriceHistoryStore createStore() {
        return new PriceHistoryStoreSkeleton();
    }
}