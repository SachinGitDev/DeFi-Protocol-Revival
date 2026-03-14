package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SmartContractTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static SmartContract getSmartContractSample1() {
        return new SmartContract().id(1L).name("name1").githubUrl("githubUrl1");
    }

    public static SmartContract getSmartContractSample2() {
        return new SmartContract().id(2L).name("name2").githubUrl("githubUrl2");
    }

    public static SmartContract getSmartContractRandomSampleGenerator() {
        return new SmartContract()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .githubUrl(UUID.randomUUID().toString());
    }
}
