package com.mopsa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.slicer.Statement.Kind;
import com.ibm.wala.util.collections.Pair;

import magpiebridge.core.AnalysisConsumer;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;
import magpiebridge.util.SourceCodePositionFinder;



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
                new MessageParams(MessageType.Info, "Found Mopsa: " + result));
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


	public String getJSONAnalyseC(String fichier)
	{
		List<String> command = new ArrayList<String>();
		command.add("mopsa-c");
		command.add("-format=json");
		command.add("hello.c");
		command.add(">analyse.json");
		String jsonanalyse = "";
		try {
			Process mopsaVersionProcess = new ProcessBuilder(command).start();
			int exitCode = mopsaVersionProcess.waitFor();
		
			if (exitCode == 0) {
				InputStream is = mopsaVersionProcess.getInputStream();
				Scanner scanner = new Scanner(is).useDelimiter("\\A");
				jsonanalyse = scanner.hasNext() ? scanner.next() : "";
			} else {
				InputStream err = mopsaVersionProcess.getErrorStream();
				Scanner scanner = new Scanner(err).useDelimiter("\\A");
				String errorMessage = scanner.hasNext() ? scanner.next() : "";
				System.out.println("Il y a une erreur : " + mopsaVersionProcess.toString());
				return 	errorMessage;
			}
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return jsonanalyse;
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
	    Collection<AnalysisResult> res = new ArrayList<AnalysisResult>();
    try {
      JsonParser parser = new JsonParser();
	  JsonObject analyse = parseJsonFile("analyse.json"); 
      JsonArray bugs = analyse.getAsJsonArray("checks");
      for (int i = 0; i < bugs.size(); i++) {
        JsonObject bug = bugs.get(i).getAsJsonObject();
        String bugType = bug.get("title").getAsString();
        String qualifier = bug.get("messages").getAsString();
        int line = bug.get("range").getAsJsonObject().get("start").getAsJsonObject().get("line").getAsInt();
        String file = "hello.c"; 
        Position pos = SourceCodePositionFinder.findCode(new File(file), line).toPosition();
        String msg = bugType + ": " + qualifier;
        JsonArray trace = bug.get("callstacks").getAsJsonArray();
        ArrayList<Pair<Position, String>> traceList = new ArrayList<Pair<Position, String>>();
        if (showTrace) {
          for (int j = 0; j < trace.size(); j++) {
            JsonObject step = trace.get(j).getAsJsonObject();
            String stepFile = this.rootPath + File.separator + step.get("filename").getAsString();
            if (new File(stepFile).exists()) {
              int stepLine = step.get("line_number").getAsInt();
              String stepDescription = step.get("description").getAsString();
              Position stepPos =
                  SourceCodePositionFinder.findCode(new File(stepFile), stepLine).toPosition();
              Pair<Position, String> pair = Pair.make(stepPos, stepDescription);
              traceList.add(pair);
            }
          }
        }
        AnalysisResult rbug =
            new MopsaResult(
                magpiebridge.core.Kind.Diagnostic, pos, msg, traceList, DiagnosticSeverity.Error, null, null);
        res.add(rbug);
      }
    } catch (JsonIOException e) {
      e.printStackTrace();
    }
	catch ( JsonSyntaxException e) {
		e.printStackTrace();
	  }
	catch (FileNotFoundException e) {
		e.printStackTrace();
	  }
	  catch (IOException e) {
		e.printStackTrace();
	  }
    return res;
	}


	public static JsonObject parseJsonFile(String filePath) throws IOException {

        // Création d'un objet Gson
        Gson gson = new Gson();

        // Ouverture du fichier JSON
        BufferedReader br = new BufferedReader(new FileReader(filePath));

        // Conversion du contenu du fichier JSON en objet JSON
        JsonObject jsonObject = gson.fromJson(br, JsonObject.class);

        // Fermeture du fichier JSON
        br.close();

        return jsonObject;
    }

}



