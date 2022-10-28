package core.basesyntax.service.impl;

import static org.junit.Assert.assertEquals;

import core.basesyntax.db.FruitStorage;
import core.basesyntax.model.Operation;
import core.basesyntax.service.DataProcessingService;
import core.basesyntax.strategy.OperationHandler;
import core.basesyntax.strategy.OperationStrategy;
import core.basesyntax.strategy.impl.OperationHandlerBalance;
import core.basesyntax.strategy.impl.OperationHandlerPurchase;
import core.basesyntax.strategy.impl.OperationHandlerReturn;
import core.basesyntax.strategy.impl.OperationHandlerSupply;
import core.basesyntax.strategy.impl.OperationStrategyImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataProcessingServiceImplTest {
    private static final String FILE_HEADER = "type,fruit,quantity";
    private static final String VALID_DATA = "b,banana,50";
    private static final String BANANA_KEY = "banana";
    private static final Integer BANANA_VALUE = 50;
    private static DataProcessingService dataProcessingService;
    private static OperationStrategy strategyPicker;
    private List<String> data;

    @BeforeClass
    public static void beforeClass() {
        dataProcessingService = new DataProcessingServiceImpl();
        Map<Operation, OperationHandler> strategyMap = new HashMap<>();
        strategyMap.put(Operation.BALANCE, new OperationHandlerBalance());
        strategyMap.put(Operation.PURCHASE, new OperationHandlerPurchase());
        strategyMap.put(Operation.SUPPLY, new OperationHandlerSupply());
        strategyMap.put(Operation.RETURN, new OperationHandlerReturn());
        strategyPicker = new OperationStrategyImpl();
        strategyPicker.provideStrategyList(strategyMap);
    }

    @Before
    public void setUp() {
        data = new ArrayList<>();
    }

    @Test(expected = RuntimeException.class)
    public void emptyList_notOk() {
        dataProcessingService.processData(data, strategyPicker);
    }

    @Test(expected = RuntimeException.class)
    public void dataWithWrongOperation_notOk() {
        data.add(FILE_HEADER);
        data.add("k,banana,50");
        dataProcessingService.processData(data, strategyPicker);
    }

    @Test
    public void validDataProcessing_ok() {
        data.add(FILE_HEADER);
        data.add(VALID_DATA);
        dataProcessingService.processData(data, strategyPicker);
        int actual = FruitStorage.fruitStorage.get(BANANA_KEY);
        int expected = BANANA_VALUE;
        assertEquals(actual, expected);
    }

    @Test
    public void dataWithReturnOperation_ok() {
        data.add(FILE_HEADER);
        data.add(VALID_DATA);
        data.add("r,banana,25");
        dataProcessingService.processData(data, strategyPicker);
        int expected = 75;
        int actual = FruitStorage.fruitStorage.get(BANANA_KEY);
        assertEquals(expected, actual);
    }

    @Test
    public void dataWithSupplyOperator_ok() {
        data.add(FILE_HEADER);
        data.add(VALID_DATA);
        data.add("s,banana,50");
        dataProcessingService.processData(data, strategyPicker);
        int expected = 100;
        int actual = FruitStorage.fruitStorage.get(BANANA_KEY);
        assertEquals(expected, actual);
    }

    @Test
    public void dataWithPurchaseOperator_ok() {
        data.add(FILE_HEADER);
        data.add(VALID_DATA);
        data.add("p,banana,30");
        dataProcessingService.processData(data, strategyPicker);
        int expected = 20;
        int actual = FruitStorage.fruitStorage.get(BANANA_KEY);
        assertEquals(expected, actual);
    }
}