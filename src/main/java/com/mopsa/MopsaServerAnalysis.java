package com.mopsa;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.util.collections.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;
import magpiebridge.core.AnalysisConsumer;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.core.analysis.configuration.ConfigurationAction;
import magpiebridge.core.analysis.configuration.ConfigurationOption;
import magpiebridge.core.analysis.configuration.OptionType;
import magpiebridge.projectservice.java.JavaProjectService;
import magpiebridge.projectservice.java.JavaProjectType;
import magpiebridge.util.SourceCodePositionFinder;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

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

  /** */
  public void checkMopsaInstallation(MagpieServer server) {
    String result = getMopsaVersion();
    printMopsaVersion(result);
    server.forwardMessageToClient(new MessageParams(MessageType.Info, "Found Mopsa: " + result));
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
    } catch (IOException e) {
      e.printStackTrace();
    }
    return version;
  }

  public String getJSONAnalyseC(String fichier) {
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
        return errorMessage;
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
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

  @Override
  public void analyze(
      Collection<? extends Module> files, AnalysisConsumer consumer, boolean rerun) {
    if (consumer instanceof MagpieServer) {
      this.server = (MagpieServer) consumer;
      if (this.rootPath == null) {
        JavaProjectService ps = (JavaProjectService) server.getProjectService("java").get();
        if (ps.getRootPath().isPresent()) {
          this.rootPath = ps.getRootPath().get().toString();
          this.reportPath = Paths.get(this.rootPath, "mopsa-out", "report.json").toString();
          this.projectType = ps.getProjectType();
          this.defaultCommand = getDefaultCommand();
          checkMopsaInstallation(server);
          // show results of previous run
          File file = new File(MopsaServerAnalysis.this.reportPath);

          if (true) {
            Collection<AnalysisResult> results = convertToolOutput();
            server.forwardMessageToClient(
                new MessageParams(MessageType.Info, "le contenu des results: " + results));
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
                  File file = new File(MopsaServerAnalysis.this.reportPath);
                  if (true) {
                    Collection<AnalysisResult> results = convertToolOutput();
                    server.consume(results, source());
                  }
                } else {
                  server.forwardMessageToClient(
                      new MessageParams(MessageType.Error, String.join("\n", stdErr.getOutput())));
                }
              } catch (InterruptedException e) {
                handleError(server, e);
              } catch (IOException e) {
                handleError(server, e);
              }
            });
      }
    }
  }
  /*
  	public String[] getCommand() {
  		// TODO Auto-generated method stub
  		return null;
  	}

  */

  private String getDefaultCommand() {
    return "./commande.sh";
  }

  @Override
  public String[] getCommand() {

    String mopsaCommand = useDefaultCommand ? defaultCommand : userDefinedCommand;

    String buildCmd = null;
    if (firstTime) {
      firstTime = false;
      buildCmd = getToolBuildCmdWithClean();
    } else {
      buildCmd = getToolBuildCmd();
    }
    if (buildCmd == null) mopsaCommand = "mopsa-c";
    else {
      mopsaCommand = MessageFormat.format(mopsaCommand, buildCmd);
    }

    server.forwardMessageToClient(
        new MessageParams(MessageType.Info, "Running command: " + mopsaCommand));
    return mopsaCommand.split(" ");
  }

  public Collection<AnalysisResult> convertToolOutput() {
    Collection<AnalysisResult> res = new ArrayList<AnalysisResult>();
    try {
      JsonParser parser = new JsonParser();
      String jsonString =
          "{ \"success\": true, \"time\": 0.10651000000000003, \"mopsa_version\": \"1.0~pre2\", \"mopsa_dev_version\": \"release\", \"files\": [ \"hello.c\" ], \"checks\": [ { \"kind\": \"warning\", \"title\": \"Invalid memory access\", \"messages\": \"accessing 4 bytes at offsets [4,400] of variable 'a' of size 400 bytes\", \"range\": { \"start\": { \"file\": \"hello.c\", \"line\": 5, \"column\": 4 }, \"end\": { \"file\": \"hello.c\", \"line\": 5, \"column\": 8 } }, \"callstacks\": [ [ { \"function\": \"main\", \"range\": { \"start\": { \"file\": \"hello.c\", \"line\": 2, \"column\": 4 }, \"end\": { \"file\": \"hello.c\", \"line\": 2, \"column\": 8 } } } ] ] } ], \"assumptions\": [] }";
      Gson gson = new Gson();
      JsonObject analyse = gson.fromJson(jsonString, JsonObject.class);
      JsonArray bugs = analyse.getAsJsonArray("checks");
      for (int i = 0; i < bugs.size(); i++) {
        JsonObject bug = bugs.get(i).getAsJsonObject();
        String bugType = bug.get("title").getAsString();
        String qualifier = bug.get("messages").getAsString();
        int line =
            bug.get("range")
                .getAsJsonObject()
                .get("start")
                .getAsJsonObject()
                .get("line")
                .getAsInt();
        String file = "hello.c";
        // Position pos = SourceCodePositionFinder.findCode(new File(file), line).toPosition();
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
            server.forwardMessageToClient(
                new MessageParams(MessageType.Info, "Lancement convertToolOutPut ..."));
          }
        }
        AnalysisResult rbug =
            new MopsaResult(
                magpiebridge.core.Kind.Diagnostic,
                null,
                msg,
                traceList,
                DiagnosticSeverity.Error,
                null,
                null);
        res.add(rbug);
        /*  server.forwardMessageToClient(
            new MessageParams(MessageType.Info, "La taille du res : " + res.size()));
        server.forwardMessageToClient(
            new MessageParams(MessageType.Info, "Le mes qui doit etre affiché   : " + msg));*/
      }

    } catch (JsonIOException e) {
      e.printStackTrace();
    } catch (JsonSyntaxException e) {
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

  private void handleError(MagpieServer server, Exception e) {
    handleError(server, e.getLocalizedMessage());
  }

  private void handleError(MagpieServer server, String message) {
    server.forwardMessageToClient(new MessageParams(MessageType.Error, message));
  }

  private String getToolBuildCmdWithClean() {
    if (JavaProjectType.Maven.toString().equals(this.projectType)) return "mvn clean compile";
    if (JavaProjectType.Gradle.toString().equals(this.projectType)) return "./gradlew clean build";
    return null;
  }

  private String getToolBuildCmd() {
    if (this.projectType.equals(JavaProjectType.Maven.toString())) return "mvn compile";
    if (this.projectType.equals(JavaProjectType.Gradle.toString())) return "./gradlew build";
    return null;
  }

  @Override
  public List<ConfigurationOption> getConfigurationOptions() {
    List<ConfigurationOption> commands = new ArrayList<>();
    ConfigurationOption defaultCommand =
        new ConfigurationOption("run default command", OptionType.checkbox, "true");
    ConfigurationOption command = new ConfigurationOption("run command: ", OptionType.text);
    commands.add(defaultCommand);
    commands.add(command);
    return commands;
  }

  @Override
  public List<ConfigurationAction> getConfiguredActions() {
    return Collections.emptyList();
  }

  @Override
  public void configure(List<ConfigurationOption> configuration) {
    for (ConfigurationOption o : configuration) {
      if (o.getType().equals(OptionType.checkbox)) {
        this.useDefaultCommand = o.getValueAsBoolean();
      } else if (o.getType().equals(OptionType.text) && o.getValue() != null) {
        this.userDefinedCommand = o.getValue();
      }
    }
  }
}
