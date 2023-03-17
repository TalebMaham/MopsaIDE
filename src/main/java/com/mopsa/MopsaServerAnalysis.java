package com.mopsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.stream.Stream;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

import com.ibm.wala.classLoader.Module;

import magpiebridge.core.AnalysisConsumer;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;



public class MopsaServerAnalysis implements ToolAnalysis {
 
	  private String rootPath;
	  private String reportPath;
	  private String projectType;
	  private boolean firstTime;
	  private static boolean showTrace = false;
	  private String userDefinedCommand;
	  private boolean useDefaultCommand;
	  private String defaultCommand;
	  private MagpieServer server;
    
    /**
     * 
     */
    public void checkMopsaInstallation(MagpieServer server) {
        String result = getMopsaVersion();
        printMopsaVersion(result);  
        server.forwardMessageToClient(
                new MessageParams(MessageType.Info, "Found infer: " + result));
    }
    
    public String getMopsaVersion() {
        String version = "";
        try {
            Process mopsaVersionProcess = new ProcessBuilder("mopsa", "-v").start();
            if (mopsaVersionProcess.waitFor() == 0) {
                InputStream is = mopsaVersionProcess.getInputStream();
                version = getResult(is).findFirst().orElse("");
                
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return version;
    }
    
    public void printMopsaVersion(String version) {
        System.out.println("Mopsa version: " + version);
        
    }
    
    
    public Stream<String> getResult(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return reader.lines();
    }

	public String source() {
		return "mopsa";
	}
/*
	  @Override
	  public void analyze(
	      Collection<? extends Module> files, AnalysisConsumer consumer, boolean rerun) {
	    if (consumer instanceof MagpieServer) {
	      this.server = (MagpieServer) consumer;
	      if (this.rootPath == null) {
	        JavaProjectService ps = (JavaProjectService) server.getProjectService("java").get();
	        if (ps.getRootPath().isPresent()) {
	          this.rootPath = ps.getRootPath().get().toString();
	          this.reportPath = Paths.get(this.rootPath, "infer-out", "report.json").toString();
	          this.projectType = ps.getProjectType();
	          this.defaultCommand = getDefaultCommand();
	          checkMopsaInstallation(server);
	          // show results of previous run
	          File file = new File(MopsaServerAnalysis.this.reportPath);
	          if (file.exists()) {
	            Collection<AnalysisResult> results = convertToolOutput();
	            if (!results.isEmpty()) server.consume(results, source());
	          }
	        }
	      }

	      if (rerun && this.rootPath != null) {
	        server.submittNewTask(
	            () -> {
	              try {
	                File report = new File(MopsaServerAnalysis.this.reportPath);
	                if (report.exists()) report.delete();
	                Process runInfer = this.runCommand(new File(MopsaServerAnalysis.this.rootPath));
	                StreamGobbler stdOut =
	                    new StreamGobbler(runInfer.getInputStream(), e -> handleError(server, e));
	                StreamGobbler stdErr =
	                    new StreamGobbler(runInfer.getErrorStream(), e -> handleError(server, e));
	                stdOut.start();
	                stdErr.start();
	                if (runInfer.waitFor() == 0) {
	                  File file = new File(InferServerAnalysis.this.reportPath);
	                  if (file.exists()) {
	                    Collection<AnalysisResult> results = convertToolOutput();
	                    server.consume(results, source());
	                  }
	                } else {
	                  server.forwardMessageToClient(
	                      new MessageParams(MessageType.Error, String.join("\n", stdErr.getOutput())));
	                }
	              } catch (InterruptedException e) {
	               
	              }
	              catch (IOException e) {
		              
		              }
	            });
	      }
	    }

	public String[] getCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<AnalysisResult> convertToolOutput() {
		// TODO Auto-generated method stub
		return null;
	}
    
*/

	public void analyze(Collection<? extends Module> files, AnalysisConsumer server, boolean rerun) {
		// TODO Auto-generated method stub
		
	}

	public String[] getCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<AnalysisResult> convertToolOutput() {
		// TODO Auto-generated method stub
		return null;
	}

}



