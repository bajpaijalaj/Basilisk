package de.upb.dss.basilisk.bll;
import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import de.upb.dss.basilisk.Basilisk;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;


public class Benchmark {
	static Logger logger;
	static File dockerFile;
	static File bmWorkSpace;
	static File iguanaPath;
	static String logFilePath;
	static String configPath;
	static Properties appProps;
	
	static String serverName, port, testDataset, queryFile, versionNumber;
	
    public static int runBenchmark(String argPort, String argServerName, String argTestDataSet, String argQueryFile, String argVersionNumber) throws IOException, InterruptedException 
    {
    	appProps = Basilisk.applicationProperties;

    	dockerFile = new File(appProps.getProperty("dockerFile"));
    	bmWorkSpace = new File(appProps.getProperty("bmWorkSpace"));
    	iguanaPath = new File(appProps.getProperty("iguanaPath"));
    	logFilePath = appProps.getProperty("logFilePath");
    	configPath = appProps.getProperty("configPath");
    	
    	
    	//Set all the required info for running the benchmark.
    	serverName = argServerName;
        port = argPort;
        testDataset = argTestDataSet;
        queryFile = argQueryFile;
        versionNumber = argVersionNumber;
        
        //Clear the docker, so that next benchmark can be run.
        clearDocker();
        
        
        //Run the triple stores
        runTripleStores();
        
    	//Move the results to results folder and rename it.
        renameResults();
      //Clear the docker, so that next benchmark can be run.
        clearDocker();
        return 0;
    }
    
    protected static void renameResults() throws IOException
    {
    	String result1 = appProps.getProperty("result1");
    	String result2 = appProps.getProperty("result2");
    	String result3 = appProps.getProperty("result3");
    	String result4 = appProps.getProperty("result4");
    	String result5 = appProps.getProperty("result5");
    	String continousBM = appProps.getProperty("continousBM");
    	
    	String cmd = "mv " + result1 + " " + continousBM + serverName + "$" + versionNumber + "$noClient1";
		
        Runtime.getRuntime().exec(cmd, null, bmWorkSpace);
        
        cmd = "mv " + result2 + " " + continousBM + serverName + "$" + versionNumber + "$noClient2";
		
        Runtime.getRuntime().exec(cmd, null, bmWorkSpace);
        
        cmd = "mv " + result3 + " " + continousBM + serverName + "$" + versionNumber + "$noClient3";
		
        Runtime.getRuntime().exec(cmd, null, bmWorkSpace);
        
        cmd = "mv " + result4 + " " + continousBM + serverName + "$" + versionNumber + "$noClient4";
		
        Runtime.getRuntime().exec(cmd, null, bmWorkSpace);
        
        cmd = "mv " + result5 + " " + continousBM + serverName + "$" + versionNumber + "$noClient5";
		
        Runtime.getRuntime().exec(cmd, null, bmWorkSpace);
    }
    
    protected static int runTripleStores()
    {
    	String cmd = "";
    	String s = null;
    	String log = "";
    	String err = "";
    	
    	//Initialize the logger to log
        logger = Logger.getLogger("MyLog");
        FileHandler fileHandler;
        
        try
        {
        	fileHandler = new FileHandler(logFilePath);
        	logger.addHandler(fileHandler);
        	SimpleFormatter formatter = new SimpleFormatter();
        	fileHandler.setFormatter(formatter);

        	logger.info("Trying to build the docker image.\n");
        	if(dockerFile.exists())
        	{
        		//Command to build the docker
        		//docker build --tag cbm:${serverName} .
        		cmd = "docker build --tag cbm:"
                        + serverName
                        + " .";
        		
        		//Run the command through Process.
                Process p = Runtime.getRuntime().exec(cmd, null, bmWorkSpace);

                //track the output and error to log.
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                System.out.println("Output of the command is :\n");
                logger.info("Output of the command is :\n");
                while ((s = stdInput.readLine()) != null)
                {
                        log = log + "\n" + s;
                        System.out.println(s);
                }

                logger.info(log);  //Log the output

                
                System.out.println("Error/Warning of the command :\n");
                logger.info("Error/Warning of the command :\n");
                while ((s = stdError.readLine()) != null)
                {
                        err = err + "\n" + s;
                        System.err.println(s);
                }

                logger.info(log);  //Log the error/warning
                
                p.waitFor();    //Wait for the process to complete.
                int exitCode = p.exitValue();
                
                
                if(exitCode != 0)
                {
                        System.out.println("Something went wrong while building the docker");
                        System.out.println("Exit code = " + exitCode);
                        System.out.println("Error message = \n" + err);
                        return exitCode;
                }
                
                
                logger.info("Successfully built docker image\n");
                logger.info("Running the " + serverName + " server\n");

                //Close all the standard input and error files to reuse it in future.
                stdInput.close();
                stdError.close();

                
                /*
                 * Command to run the docker image.
                 * nohup docker run -p ${port}:${port} -v ../../continuousBM/testDataSet:/datasets --name ${serverName}_server cbm:{serverName} \ 
                 * -f /dataset/ ${testDataset} -p ${port} &
                 * 
                 * Example for tentris.
                 * ${port} = 9080, ${serverName} = tentris, ${testDataset} = sp2b.nt
                 * nohup docker run -p 9080:9080 -v home/dss/continuousBM/testDataSet:/datasets --name tentris_server cbm:tentris \ 
                 * -f /datasets/sp2b.nt -p 9080 &
                */
                
                String command = "";
                
                if(serverName.toLowerCase().equals("tentris"))
                {
                	command = "docker run -p "
                            + port + ":" + port
                            + " -v /home/dss/continuousBM/testDataSet:/datasets --name "
                            + serverName + "_server cbm:" + serverName
                            + " -f /datasets/"
                            + testDataset + " -p "
                            + port;
                }
                else if(serverName.toLowerCase().equals("virtuoso"))
                {
                	command = "docker run -p "
                            + port + ":" + port
                            + " --name "
                            + serverName + "_server cbm:" + serverName;
                }
                

                //Run the command.
                Runtime.getRuntime().exec(command, null, bmWorkSpace);

                //Wait for 10 seconds to docker image to setup and keep running.
                TimeUnit.SECONDS.sleep(10);
                
                //If the process is alive run Iguana benchmarl otherwise could not run the docker image.
                //Command to check whether the respective docker image is running or not, to avoid the infinite loop.
            	//docker images -q cbm:${serverName}
                cmd = "docker images -q cbm:"
            			+ serverName;
            	
            	//Run the command.
            	p = Runtime.getRuntime().exec(cmd, null, bmWorkSpace);

            	//Track the input and error.
                stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String dockerId = "";
                
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
                	runIguana();
                }
                
                
                
//                if(p.isAlive())
//                {
//                	logger.info(serverName + " server is successfully running.\n");
//                    
//                	runIguana();
//                }
//                else
//                {
//                	stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//                	logger.info("Error/Warning of the command :\n");
//                	System.err.println("Error/Warning of the command :\n");
//                    while ((s = stdError.readLine()) != null)
//                    {
//                            err = err + "\n" + s;
//                            System.err.println(s);
//                    }
//                    
//                	System.out.println("Something went wrong while running the docker");
//                    System.out.println("Exit code = " + exitCode);
//                    System.out.println("Error message = \n" + err);
//                    return 50;
//                }

                

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
    
    protected static int runIguana() throws Exception
    {
    	String s = "";
    	String log = "";
    	String err = "";
    	String cmd = "";

       	//Set the Iguana configuration file respective to triple store before running it.
       	setIguanaConfigFile();
       	
       	//Command to run the iguana script.
       	cmd = "./start-iguana.sh benchmark.config";
        	
       	//Run the Iguana script
       	Process p = Runtime.getRuntime().exec(cmd, null, iguanaPath);

        	//Track the output and error
       	BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
       	BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        System.out.println("Output of the command is :\n");
        logger.info("Output of the command is :\n");
        while ((s = stdInput.readLine()) != null)
        {
        	log = log + "\n" + s;
        	System.out.println(s);
        }

        logger.info(log);

        System.out.println("Error/Warning of the command :\n");
        logger.info("Error/Warning of the command :\n");
        while ((s = stdError.readLine()) != null)
        {
        	err = err + "\n" + s;
            System.err.println(s);
        }

        //Wait for process to complete.
        p.waitFor();
        int exitCode = p.exitValue();
           
        if(exitCode != 0)
        {
         	System.out.println("Something went wrong while while running iguana");
            System.out.println("Exit code = " + exitCode);
            System.out.println("Error message = \n" + err);
            return exitCode;
        }
    	return 0;
    }
    
    protected static void clearDocker()
    {
    	try
    	{
    		//Clear the complete docker, so that next benchamrk can be done.
            //First kill the docker container we ran.
            String cmd = "docker kill "
                    + serverName + "_server";
            Process p = Runtime.getRuntime().exec(cmd, null, iguanaPath);
            p.waitFor();

            //Second prune the docker system, so that all the stoped container will be removed.
            cmd = "docker system prune";
            p = Runtime.getRuntime().exec(cmd, null, iguanaPath);

            //For the above command we will have to give a confirmation "y".
            OutputStream out = p.getOutputStream();
            out.write("y".getBytes());
            out.close();
            p.waitFor();

//            while ((s = stdError.readLine()) != null)
//            {
//            	err = err + "\n" + s;
//                System.err.println(s);
//            }

            cmd = "docker image rm cbm:"
                    + serverName;
            p = Runtime.getRuntime().exec(cmd, null, iguanaPath);
            p.waitFor();
    	}
    	catch (Exception e)
        {
                System.out.println("exception happened - here's what I know: ");
                e.printStackTrace();
                System.exit(-1);
        }
    }
    
    protected static int setIguanaConfigFile()
    {
    	//Get the freemarker configuration
    	Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

        try
        {
        	//Set up the free marker configuration, with template loading class and path.
        	cfg.setClassForTemplateLoading(Benchmark.class, "/");
            cfg.setDefaultEncoding("UTF-8");
            
            //Get the Iguana configuration template.
        	Template template = cfg.getTemplate("iguanaConfig.ftl");

        	String connName = serverName + "$" + versionNumber;
        	String datasetName = serverName + "$" + versionNumber + "$DB";
        	//Port number and query file to insert into benchmark template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("port", port);
            templateData.put("testData", queryFile);
            templateData.put("connName", connName);
            templateData.put("datasetName", datasetName);

            //Write port number and query file in to template
            StringWriter out = new StringWriter();
            template.process(templateData, out);
            out.flush();
            
            //Dump that configuration into a configuration file called benchmark.config
            String fileSeparator = System.getProperty("file.separator");
    
    
            System.out.println("Config is : " + configPath);
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
                logger.info(configPath+" File Created");
            }else 
            {
              	System.out.println("Something went wrong while creating the file "+configPath);
            	logger.info("Something went wrong while creating the file "+configPath);
            	return -1;
            }
        }
        catch(Exception e)
        {
        	System.err.println(e);
        }
        return 0;
    }
}
