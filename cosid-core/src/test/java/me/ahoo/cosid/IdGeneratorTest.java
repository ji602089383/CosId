package me.ahoo.cosid;

import lombok.var;
import me.ahoo.cosid.jvm.JdkId;
import me.ahoo.cosid.snowflake.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ahoo wang
 * Creation time: 2019/11/21 20:56
 */
public class IdGeneratorTest {
    @Test
    public void jdkTest() {
        var id = JdkId.INSTANCE.generate();
        var id1 = JdkId.INSTANCE.generate();
        Assertions.assertTrue(id1 > id);
    }

    @Test
    public void snowflakeTest() {
        var idGen = new MillisecondSnowflakeId(1);
        Assertions.assertEquals(1, idGen.getMachineId());
        var id = idGen.generate();
        var id1 = idGen.generate();

        Assertions.assertTrue(id1 > id);
        var snowflakeIdStateParser = MillisecondSnowflakeIdStateParser.of(idGen);
        var idState = snowflakeIdStateParser.parse(id);
        Assertions.assertNotNull(idState);
    }

    @Test
    public void snowflakeFriendlyIdTest() {
        MillisecondSnowflakeId idGen = new MillisecondSnowflakeId(1);
        DefaultSnowflakeFriendlyId snowflakeFriendlyId = new DefaultSnowflakeFriendlyId(idGen);
        long id = snowflakeFriendlyId.generate();
        SnowflakeIdState snowflakeIdState = snowflakeFriendlyId.friendlyId(id);
        Assertions.assertNotNull(snowflakeIdState);
        Assertions.assertEquals(1, snowflakeIdState.getMachineId());
        Assertions.assertEquals(id, snowflakeIdState.getId());
        SnowflakeIdState snowflakeIdState2 = snowflakeFriendlyId.ofFriendlyId(snowflakeIdState.getFriendlyId());
        Assertions.assertEquals(snowflakeIdState2, snowflakeIdState);
    }

    /***
     *
     */
    @Test
    public void test() {
        var id = 99191438008389632L;
        var snowflakeIdStateParser = new MillisecondSnowflakeIdStateParser(CosId.COSID_EPOCH, MillisecondSnowflakeId.DEFAULT_TIMESTAMP_BIT, MillisecondSnowflakeId.DEFAULT_MACHINE_BIT, MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT);
        var idState = snowflakeIdStateParser.parse(id);
        Assertions.assertNotNull(idState);
        var idStateOfFriendlyId = snowflakeIdStateParser.parse(idState.getFriendlyId());
        Assertions.assertEquals(idState, idStateOfFriendlyId);
    }

    @Test
    public void customize_SnowflakeTest() {
        var idGen = new MillisecondSnowflakeId(CosId.COSID_EPOCH, 41, 5, 10, 1);
        var id = idGen.generate();

        var snowflakeIdStateParser = MillisecondSnowflakeIdStateParser.of(idGen);
        var idState = snowflakeIdStateParser.parse(id);

        var idStateOfFriendlyId = snowflakeIdStateParser.parse(idState.getFriendlyId());
        Assertions.assertEquals(idState, idStateOfFriendlyId);
    }


    @Test
    public void secondSnowflakeIdTest() {

        var idGen = new SecondSnowflakeId(1);
        var snowflakeIdStateParser = SecondSnowflakeIdStateParser.of(idGen);
        var id = idGen.generate();
        var id1 = idGen.generate();

        Assertions.assertTrue(id1 > id);

        var idState = snowflakeIdStateParser.parse(id);
        var idStateOfFriendlyId = snowflakeIdStateParser.parse(idState.getFriendlyId());
        Assertions.assertEquals(idState, idStateOfFriendlyId);
    }


    @Test
    public void safe_ofSecond() {
        var snowflakeId = SafeJavaScriptSnowflakeId.ofSecond(1);
        var snowflakeIdStateParser = SecondSnowflakeIdStateParser.of(snowflakeId);
        var id = snowflakeId.generate();
        var idState = snowflakeIdStateParser.parse(id);
        var idStateOfFriendlyId = snowflakeIdStateParser.parse(idState.getFriendlyId());
        Assertions.assertEquals(idState, idStateOfFriendlyId);
    }

    @Test
    public void safe_ofMillisecond() {
        var snowflakeId = SafeJavaScriptSnowflakeId.ofMillisecond(1);
        var snowflakeIdStateParser = MillisecondSnowflakeIdStateParser.of(snowflakeId);
        var id = snowflakeId.generate();
        var idState = snowflakeIdStateParser.parse(id);
        var idStateOfFriendlyId = snowflakeIdStateParser.parse(idState.getFriendlyId());
        Assertions.assertEquals(idState, idStateOfFriendlyId);
    }

    static final int CONCURRENT_THREADS = 30;
    static final int THREAD_REQUEST_NUM = 50000;

    @Test
    public void concurrent_generate_step_10() {
        final SnowflakeId idGen = new ClockSyncSnowflakeId(new MillisecondSnowflakeId(1), ClockBackwardsSynchronizer.DEFAULT);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CompletableFuture<List<Long>>[] completableFutures = new CompletableFuture[CONCURRENT_THREADS];
        int threads = 0;
        while (threads < CONCURRENT_THREADS) {
            completableFutures[threads] = CompletableFuture.supplyAsync(() -> {
                List<Long> ids = new ArrayList<>(THREAD_REQUEST_NUM);
                int requestNum = 0;
                while (requestNum < THREAD_REQUEST_NUM) {
                    requestNum++;
                    long id = idGen.generate();
                    ids.add(id);
                }
                return ids;
            });

            threads++;
        }
        CompletableFuture.allOf(completableFutures).thenAccept(nil -> {
            List<Long> totalIds = new ArrayList<>();
            for (CompletableFuture<List<Long>> completableFuture : completableFutures) {
                List<Long> ids = completableFuture.join();
                totalIds.addAll(ids);
            }
            totalIds.sort(Long::compareTo);
            Long lastId = null;
            for (Long currentId : totalIds) {
                if (lastId == null) {
                    lastId = currentId;
                    continue;
                }

                Assertions.assertTrue(currentId > lastId);
                lastId = currentId;
            }

        }).join();
        executorService.shutdown();
    }
}
