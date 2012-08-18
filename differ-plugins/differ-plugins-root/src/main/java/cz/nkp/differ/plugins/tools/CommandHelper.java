package cz.nkp.differ.plugins.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class CommandHelper extends Thread{

	public static final class CommandInfo{
		public String workingDir;
		public String[] commands;
	};
	
	public static abstract class CommandMessageCallback{
		public abstract void messageGenerated(String message);
	};
	
	private static Logger LOGGER = Logger.getRootLogger();
	
	private long maximumWaitTime = 1000 * 5;
	
	private CommandMessageCallback callback;
	
	private ProcessBuilder pb;
	private StreamGobbler stdGobbler = null ,errorGobbler = null;
	private boolean errorFlag = false;
	
	public CommandHelper(CommandInfo info,CommandMessageCallback callback,Logger logger){
		if(info == null || callback == null || logger == null){
			throw new IllegalArgumentException("Cannot pass null parameters to CommandHelper");
		}
		
		LOGGER = logger;	
		this.callback = callback;
		
		pb = new ProcessBuilder();
		pb.directory(new File(info.workingDir));
		pb.command(info.commands);
	}
	
	public void setMaxCompletionTime(long milliseconds){
		if(milliseconds > 0){
			maximumWaitTime = milliseconds;
		}
		else{
			LOGGER.warn("Invalid maximum wait time for CommandHelper");
		}
		
	}
	
	public void run(){
		try {
			Process proc = pb.start();
			
			stdGobbler = new StreamGobbler(proc.getInputStream(),LOGGER);
			stdGobbler.start();
			
			errorGobbler = new StreamGobbler(proc.getErrorStream(),LOGGER);
			errorGobbler.start();
			
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				LOGGER.error("Command process interrupted",e);
			}
			
			callback.messageGenerated(getMessage());
			
		} catch (IOException e) {
			LOGGER.error("Unable to run process",e);
			errorFlag = true;
			return;
		}
	}
	
	private String getMessage() throws IOException{
		long timeStarted = System.currentTimeMillis();
		if(errorFlag){
			throw new IOException("The command was invalid and no message could be generated");
		}
		if(stdGobbler == null || errorGobbler == null){
			throw new IOException("The command streams were not correctly created"); 
		}
		
		while(true){
			if(errorGobbler.isReady() && stdGobbler.isReady()){
				String errorMsg = errorGobbler.getMessage();
				if(errorMsg != null){
					LOGGER.error("Process Error Stream: " + errorMsg);
				}
				return stdGobbler.getMessage();
			}
			else {
				if((System.currentTimeMillis() - timeStarted) > maximumWaitTime){
					throw new IOException("Command took too long to execute");
				}
			}
		}
		
	}
}

class StreamGobbler extends Thread
{
	private static Logger LOGGER = Logger.getRootLogger();
    InputStream is;
    private boolean isReady = false;
    private String msg = "";
    
    StreamGobbler(InputStream is, Logger logger)
    {
        this.is = is;
        LOGGER = logger;
    }
    
    public void run()
    {
    	InputStreamReader stream = null;
    	BufferedReader br = null;
        try{
            stream = new InputStreamReader(is);
            br = new BufferedReader(stream);
            String line = null;
            while ( (line = br.readLine()) != null){
                msg+=line;  
            }  
            isReady = true;
        } catch (IOException e){
        	isReady = false;
            LOGGER.error(e);
        } finally{
        	if(stream != null){
        		try {
					stream.close();
				} catch (IOException e) {
					LOGGER.warn("Unable to close stream reader",e);
				}
        	}
        	if(br != null){
        		try {
					br.close();
				} catch (IOException e) {
					LOGGER.warn("Unable to close stream reader",e);
				}
        	}
        }
    }
    
    public boolean isReady(){
    	return isReady;
    }
    
    public String getMessage(){
    	if(!isReady()){
    		return null;
    	}
    	else return msg;
    }
}
