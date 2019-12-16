package de.upb.dss.basilisk.bll;
import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Benchmark {
	static Logger logger;
    public static int runBenchmark(String benchmarkpath, String port ,String rootpwd, String serverName, String testDataset) throws IOException, InterruptedException {
        
    	String cmd = "";
    	File dockerFile = new File("../../../../../../../../../continuousBM/bmWorkSpace/Dockerfile");
    	File bmWorkSpace = new File("../../../../../../../../../continuousBM/bmWorkSpace/");
    	
    	String s = null, log = "", err = "";
        logger = Logger.getLogger("MyLog");
        FileHandler fileHandler;
        
        
        try
        {
          fileHandler = new FileHandler("MyLogFile.log");
          logger.addHandler(fileHandler);
          SimpleFormatter formatter = new SimpleFormatter();
          fileHandler.setFormatter(formatter);

          logger.info("Trying to build the docker image.\n");
          if(dockerFile.exists())
          {
                cmd = "docker build --tag cbm:"
                        + serverName
                        + " .";
                Process p = Runtime.getRuntime().exec(cmd, null, bmWorkSpace);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null)
                {
                        log = log + "\n" + s;
                }

                logger.info(log);

                System.out.println("Here is the standard error of the command (if any):\n");

                while ((s = stdError.readLine()) != null)
                {
                        err = err + "\n" + s;
                }

                System.out.println(err);
                p.waitFor();
                int exitCode = p.exitValue();
                
                
                if(exitCode != 0)
                {
                        System.out.println("Something went wrong while building the docker");
                        System.out.println("Exit code = " + exitCode);
                        System.out.println("Error message = \n" + err);
                        return 50;
                }
                logger.info("Successfully built docker image\n");

                logger.info("Running the tentris server\n");

                stdInput.close();
                stdError.close();

                String command = "docker run -p "
                                    + port + ":" + port
                                    + " -v /home/dss/continuousBM/testDataSet:/datasets --name "
                                    + serverName + "_server cbm:" + serverName
                                    + " -f /datasets/"
                                    + testDataset + " -p "
                                    + port;

                System.out.println(command);
                p = Runtime.getRuntime().exec(command, null, bmWorkSpace);

                TimeUnit.SECONDS.sleep(10);
                if(p.isAlive())
                {
                        runIguana(bmWorkSpace);
                }

                logger.info("Successfully built docker image\n");

                logger.info("Running the tentris server\n");

                stdInput.close();
                stdError.close();
                
                
          }
        }
        catch (Exception e)
        {
                System.out.println("exception happened - here's what I know: ");
                e.printStackTrace();
                System.exit(-1);
        }
        return 0;
    }
    
    public static int runIguana(File bmWorkSpace) throws Exception
    {
    	String s = "", log = "", err = "";
    	String dockerId = "";
    	String cmd = "docker images -q cbm:tentrise";
    	Process p = Runtime.getRuntime().exec(cmd, null, bmWorkSpace);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        System.out.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null)
        {
        	dockerId = s;
        }

        if(dockerId == "")
        {
        	System.out.println("Empty not existed docker container");
        	
        }
        
        System.out.println(dockerId);

    	return 0;
    }
}
