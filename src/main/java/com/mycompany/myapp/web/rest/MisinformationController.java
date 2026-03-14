package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.DetectMisinformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MisinformationController {

    @Autowired
    private DetectMisinformation detectMissinformation;

    @GetMapping("/analyse")
    public String analyse(@RequestParam String repoUrl) {
        return detectMissinformation.analyse(repoUrl);
    }
}
