package com.mycompany.myapp.service;

import org.springframework.stereotype.Service;

@Service
public class DetectMisinformation {

    public String analyse(String repoUrl) {
        String[] parts = repoUrl.split("/");
        if (parts.length < 5) {
            return "Invalid GitHub URL";
        }
        String owner = parts[3];
        String repo = parts[4];

        return "Analyzing repository: " + owner + "/" + repo;
    }
}
