package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.service.DetectMisinformation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MisinformationController {

    @Autowired
    private DetectMisinformation detectMisinformation;

    @GetMapping("/analyse")
    public String analyse(@RequestParam String repoUrl) {
        return detectMisinformation.analyse(repoUrl);
    }

    @GetMapping("/GETREADME")
    public String getReadme(
        @RequestParam String owner,
        @RequestParam String repo,
        @RequestParam(required = false, defaultValue = "") String folder
    ) {
        return detectMisinformation.getREADMERecursive(owner, repo, folder);
    }

    @GetMapping("/GETSOL")
    public List<String> getSol(
        @RequestParam String owner,
        @RequestParam String repo,
        @RequestParam(required = false, defaultValue = "") String folder
    ) {
        return detectMisinformation.getSolFilesRecursive(owner, repo, folder);
    }
}
