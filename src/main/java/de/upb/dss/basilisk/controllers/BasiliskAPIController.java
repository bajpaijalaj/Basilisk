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
            exitcode = Benchmark.runBenchmark("9080", "tentris", "sp2b.nt", "sp2b.txt", "version1");
        } catch (Exception ex) {
            ex.printStackTrace(pw);
            return sw.toString();
        }

	try
	{
		File file = new File("/home/dss/continuousBM/log/start-benchmarking.err");
		BufferedReader br = new BufferedReader(new FileReader(file));

		String message = " ";
		String msg;
		while ((msg = br.readLine()) != null)
		{
			message += msg + "\n";
		}

            	return message;
	}catch (Exception e)
	{
		if(exitcode == 0) {
			return "Successfully ran";
		}else{
			return "Something went wrong.";
		}
	}
    }
}
