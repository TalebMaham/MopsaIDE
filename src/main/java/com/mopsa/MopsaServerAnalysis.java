package com.mopsa;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.util.collections.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;
import magpiebridge.core.AnalysisConsumer;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.Kind;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;
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
          this.reportPath = Paths.get(this.rootPath, "analyse.json").toString();
          this.projectType = ps.getProjectType();
          // checkMopsaInstallation(server);
          // show results of previous run
          File file = new File(MopsaServerAnalysis.this.reportPath);

          if (file.exists()) {
            Collection<AnalysisResult> results = convertToolOutput();
            System.out.println("Resultat reçu : " + results);
            if (!results.isEmpty()) server.consume(results, source());
          }
        }
      }

      if (rerun && this.rootPath != null) {
        server.submittNewTask(
            () -> {
              File report = new File(MopsaServerAnalysis.this.reportPath);
              // if (report.exists()) report.delete();
              System.out.println(
                  "Avant de lancer la commande reportpath == : "
                      + MopsaServerAnalysis.this.reportPath);
              System.out.println(
                  "Avant de lancer la commande rootpath == : " + MopsaServerAnalysis.this.rootPath);

              // Process runMopsa = this.runCommand(new File(MopsaServerAnalysis.this.rootPath));

              if (true) {
                File file = new File(MopsaServerAnalysis.this.reportPath);
                if (file.exists()) {
                  Collection<AnalysisResult> results = convertToolOutput();
                  System.out.println("envoie du results" + results);
                  server.consume(results, source());
                } else {
                  System.out.println("Erreur le file n'existe pas : ");
                }
              } else {
                // System.out.println("Erreur : " + runMopsa.waitFor());

              }
            });
      }
    }
  }

  @Override
  public String[] getCommand() {
    return new String[] {"mopsa-c mopsa.db -format=json >analyse.json "};
  }

  public Collection<AnalysisResult> convertToolOutput() {
    Collection<AnalysisResult> res = new ArrayList<AnalysisResult>();
    try {
      Gson gson = new Gson();
      JsonObject jsonObject =
          gson.fromJson(new FileReader(new File(this.reportPath)), JsonObject.class);

      JsonArray checks = jsonObject.getAsJsonArray("checks");
      for (JsonElement checkElement : checks) {
        JsonObject check = checkElement.getAsJsonObject();
        String kind = check.get("kind").getAsString();
        System.out.println("Kind: " + kind);
        String title = check.get("title").getAsString();
        System.out.println("Title: " + title);
        String messages = check.get("messages").getAsString();
        System.out.println("Messages: " + messages);

        JsonObject range = check.getAsJsonObject("range");
        JsonObject start = range.getAsJsonObject("start");
        int line = start.get("line").getAsInt();
        System.out.println("Line: " + line);
        String file = start.get("file").getAsString();
        System.out.println("File: " + file);

        Position pos =
            SourceCodePositionFinder.findCode(
                    new File(MopsaServerAnalysis.this.rootPath + "/" + file), line)
                .toPosition();
        String msg = title + ": " + messages;
        System.out.println("Valeurs de callstacks :");

        System.out.println("Valeurs de callstacks :" + check.getAsJsonArray("callstacks"));
        JsonArray callstacks = check.getAsJsonArray("callstacks");
        ArrayList<Pair<Position, String>> traceList = new ArrayList<Pair<Position, String>>();
        if (showTrace) {
          for (JsonElement callstackElement : callstacks) {
            JsonArray callstack = callstackElement.getAsJsonArray();
            for (JsonElement stepElement : callstack) {
              JsonObject step = stepElement.getAsJsonObject();
              JsonObject stepRange = step.getAsJsonObject("range");
              JsonObject stepStart = stepRange.getAsJsonObject("start");
              int stepLine = stepStart.get("line").getAsInt();
              String stepFile = stepStart.get("file").getAsString();
              String stepFunction = step.get("function").getAsString();

              if (new File(stepFile).exists()) {
                Position stepPos =
                    SourceCodePositionFinder.findCode(new File(stepFile), stepLine).toPosition();
                Pair<Position, String> pair = Pair.make(stepPos, stepFunction);
                traceList.add(pair);
              }
            }
          }
        }

        AnalysisResult rbug =
            new MopsaResult(
                Kind.Diagnostic, pos, msg, traceList, DiagnosticSeverity.Error, null, null);
        res.add(rbug);
      }
    } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
      System.out.println("Wow3 : " + e);
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
