package de.upb.dss.basilisk.bll;
import java.lang.*;
import java.io.*;
import java.util.*;


public class Benchmark {
    public static int runBenchmark(String benchmarkpath, String port ,String rootpwd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("sh",benchmarkpath, port, rootpwd);
        int exitCode = -1;
        Process p = pb.start();
        System.out.println("Iguana started.. = ");
        InputStream stderr = p.getErrorStream();

        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line = null;

        while (p.isAlive())
        {
            if((line = br.readLine()) != null)
                System.out.println(line);
        }

        exitCode= p.waitFor();
        System.out.println("Iguana completed with " + exitCode);
        return exitCode;
    }
}
