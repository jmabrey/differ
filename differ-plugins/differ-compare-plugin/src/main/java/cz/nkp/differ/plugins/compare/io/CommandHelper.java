package cz.nkp.differ.plugins.compare.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

import cz.nkp.differ.plugins.ComparePluginInterface;

public class CommandHelper extends Thread{

	private static Logger LOGGER = ComparePluginInterface.LOGGER;
	
	private static long MAX_WAIT_TIME = 1000 * 5;
	
	private ProcessBuilder pb;
	StreamGobbler stdGobbler = null ,errorGobbler = null;
	private boolean errorFlag = false;
	
	public CommandHelper(String command){
		pb = new ProcessBuilder();
		pb.command(command);
	}
	
	public void run(){
		try {
			Process proc = pb.start();
			
			stdGobbler = new StreamGobbler(proc.getInputStream());
			stdGobbler.start();
			
			errorGobbler = new StreamGobbler(proc.getErrorStream());
			errorGobbler.start();
		} catch (IOException e) {
			LOGGER.error("Unable to run process",e);
			errorFlag = true;
			return;
		}
	}
	
	public String getMessage() throws IOException{
		long timeStarted = System.currentTimeMillis();
		if(errorFlag || errorGobbler == null || stdGobbler == null){
			throw new IOException("The command was invalid and no message could be generated");
		}
		while(true){
			if(errorGobbler.isReady() && stdGobbler.isReady()){
				String errorMsg = errorGobbler.getMessage();
				if(errorMsg != null){
					LOGGER.error(errorMsg);
				}
				return stdGobbler.getMessage();
			}
			else {
				if((System.currentTimeMillis() - timeStarted) > MAX_WAIT_TIME){
					throw new IOException("Command took too long to execute");
				}
			}
		}
		
	}
}

class StreamGobbler extends Thread
{
	private static Logger LOGGER = ComparePluginInterface.LOGGER;
    InputStream is;
    private boolean isReady = false;
    private String msg = "";
    
    StreamGobbler(InputStream is)
    {
        this.is = is;
    }
    
    public void run()
    {
    	InputStreamReader stream = null;
        try{
            stream = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(stream);
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
					LOGGER.error("Unable to close stream!",e);
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
