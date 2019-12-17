package de.upb.dss.basilisk.bll;
import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;


public class Benchmark {
	static Logger logger;
	static File dockerFile = new File("../../continuousBM/bmWorkSpace/Dockerfile");
	static File bmWorkSpace = new File("../../continuousBM/bmWorkSpace/");
	static File iguanaPath = new File("../../continuousBM/iguana/");
	static String serverName, port, testDataset, queryFile;
	
    public static int runBenchmark(String argPort, String argServerName, String argTestDataSet, String argQueryFile) throws IOException, InterruptedException 
    {
    	//Set all the required info for running the benchmark.
    	serverName = argServerName;
        port = argPort;
        testDataset = argTestDataSet;
        queryFile = argQueryFile;
        
        
        //Run the triple stores
        runTripleStores();
        
        
        
    	
        return 0;
    }
    
    
    protected static int runTripleStores()
    {
    	String cmd = "";
    	
    	
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
                        System.out.println(s);
                }

                logger.info(log);

                System.out.println("Here is the standard error of the command (if any):\n");

                while ((s = stdError.readLine()) != null)
                {
                        err = err + "\n" + s;
                        System.err.println(s);
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

                String command = "nohup docker run -p "
                                    + port + ":" + port
                                    + " -v /home/dss/continuousBM/testDataSet:/datasets --name "
                                    + serverName + "_server cbm:" + serverName
                                    + " -f /datasets/"
                                    + testDataset + " -p "
                                    + port + " &";

                System.out.println(command);
                p = Runtime.getRuntime().exec(command, null, bmWorkSpace);

                TimeUnit.SECONDS.sleep(10);
                if(p.isAlive())
                {
                        runIguana();
                }
                else
                {
                	System.out.println("P is not alive");
                }

                logger.info("Successfully built docker image\n");

                logger.info("Running the tentris server\n");

                System.out.println("closing std out file");
                stdInput.close();
                System.out.println("closed std out file");
                System.out.println("closing std err file");
                stdError.close();
                System.out.println("closed std err file");
                
                
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
    
    public static int runIguana() throws Exception
    {
    	String s = "", log = "", err = "";
    	String dockerId = "";
    	String cmd = "docker images -q cbm:"
    			+ serverName;
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
        	return -1;
        }
        else
        {
        	stdInput.close();
        	stdError.close();
        	
        	//Set the configuration file of Iguana before running it.
        	setIguanaConfigFile();
        	cmd = "./start-iguana.sh benchmark.config";
        	
        	p = Runtime.getRuntime().exec(cmd, null, iguanaPath);

            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            while ((s = stdInput.readLine()) != null)
            {
                    log = log + "\n" + s;
                    System.out.println(s);
            }

            logger.info(log);

            System.out.println("Here is the standard error of the command (if any):\n");

            while ((s = stdError.readLine()) != null)
            {
                    err = err + "\n" + s;
                    System.err.println(s);
            }

            System.out.println(err);
            p.waitFor();
            int exitCode = p.exitValue();
            
            cmd = "docker kill "
                    + serverName + "_server";
            p = Runtime.getRuntime().exec(cmd, null, iguanaPath);
            p.waitFor();

            cmd = "docker system prune";
            p = Runtime.getRuntime().exec(cmd, null, iguanaPath);

            OutputStream out = p.getOutputStream();
            out.write("y".getBytes());
            out.close();
            p.waitFor();

            while ((s = stdError.readLine()) != null)
            {
            	err = err + "\n" + s;
                System.err.println(s);
            }

            cmd = "docker image rm cbm:"
                    + serverName;
            p = Runtime.getRuntime().exec(cmd, null, iguanaPath);
            p.waitFor();
            
            if(exitCode != 0)
            {
            	System.out.println("Something went wrong while building the docker");
                System.out.println("Exit code = " + exitCode);
                System.out.println("Error message = \n" + err);
                return 50;
            }
        }
        
        System.out.println(dockerId);

    	return 0;
    }
    
    protected static int setIguanaConfigFile()
    {
    	Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

//        cfg.setClassForTemplateLoading(Benchmark.class, "/");
    	

        try
        {
        	cfg.setClassForTemplateLoading(Benchmark.class, ".");
            cfg.setDefaultEncoding("UTF-8");
        	Template template = cfg.getTemplate("iguanaConfig.ftl");

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("port", port);
            templateData.put("testData", queryFile);

            StringWriter out = new StringWriter();
                template.process(templateData, out);
                System.out.println(out.getBuffer().toString());

                out.flush();
                
                String fileSeparator = System.getProperty("file.separator");
                String configPath = ".."+fileSeparator
                		+".."+fileSeparator
                		+"continuousBM"+fileSeparator
                		+"iguana"+fileSeparator
                		+"benchmark.config";
                
                File configFile = new File(configPath);
                
                if(configFile.exists())
                {
                	if(!configFile.delete())
                	{
                		System.err.println("Could not gnerate config file");
                		return -1;
                	}
                }
                if(configFile.createNewFile())
                {
                	FileOutputStream fos = new FileOutputStream(configPath);
                	fos.write(out.toString().getBytes());
                	fos.flush();
                	fos.close();
                    System.out.println(configPath+" File Created");
                }else 
                	System.out.println("Something went wrong while creating the file "+configPath+"");
        	}
        	catch(Exception e)
        	{
        		System.err.println(e);
        	}
        return 0;
    }
}
