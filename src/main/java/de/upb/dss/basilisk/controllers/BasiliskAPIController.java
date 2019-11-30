package de.upb.dss.basilisk.controllers;

import de.upb.dss.basilisk.Basilisk;
import de.upb.dss.basilisk.bll.Benchmark;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Properties;

@RestController
public class BasiliskAPIController {
    @RequestMapping("/")
    public String index() {
        return "Basilisk is running...";
    }

    @RequestMapping("/runbenchmark")
    public String runBenchmark() {
        Properties appProps = Basilisk.applicationProperties;

        String condigpath = appProps.getProperty("benchmarkpath");
        String tentrisport = appProps.getProperty("tentrisport");
        String rootpwd = appProps.getProperty("rootpassword");

        int exitcode = -1;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        try {
            exitcode = Benchmark.runBenchmark(condigpath,tentrisport, rootpwd);
        } catch (Exception ex) {
            ex.printStackTrace(pw);
            return sw.toString();
        }

        if(exitcode == 0) {
            return "Successfully ran the benchmark";
        } else {
            return "We ran into problems";
        }
    }
}
