package com.mycompany.myapp.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DetectMisinformationTest {

    @Autowired
    private DetectMisinformation detectMisinformation;

    @Test
    void testAnalyse() {
        String result = detectMisinformation.analyse("https://github.com/crytic/not-so-smart-contracts/tree/master/bad_randomness");
        System.out.println(result);
    }
}
