package com.mycompany.myapp.service;

import com.mycompany.myapp.service.DetectMisinformation;
import org.junit.jupiter.api.Test;

public class DetectMisinformationTest {

    @Test
    void testAnalyse() {
        DetectMisinformation service = new DetectMisinformation();
        String result = service.analyse("https://github.com/crytic/not-so-smart-contracts/tree/master/bad_randomness");
        System.out.println(result);
    }
}
