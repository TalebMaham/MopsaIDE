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
import java.util.stream.Stream;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
              File report = new File(MopsaServerAnalysis.this.reportPath);
              // if (report.exists()) report.delete();
              System.out.println(
                  "Avant de lancer la commande reportpath == : "
                      + MopsaServerAnalysis.this.reportPath);
              System.out.println(
                  "Avant de lancer la commande rootpath == : " + MopsaServerAnalysis.this.rootPath);

              try {
                Process runMopsa = this.runCommand(new File(MopsaServerAnalysis.this.rootPath));

                if (runMopsa.waitFor() != 2) {
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
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            });
      }
    }
  }

  @Override
  public String[] getCommand() {
    // Poser les questions avec des cases à cocher
    JPanel panel = new JPanel();
    JCheckBox cCheckbox = new JCheckBox("C");
    JCheckBox pythonCheckbox = new JCheckBox("Python");
    panel.add(cCheckbox);
    panel.add(pythonCheckbox);
    JOptionPane.showOptionDialog(
        null,
        panel,
        "Question 1: C ou Python?",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        null,
        null);

    // Récupérer les réponses de l'utilisateur
    boolean isCSelected = cCheckbox.isSelected();
    boolean isPythonSelected = pythonCheckbox.isSelected();

    if (isCSelected) {
      return new String[] {"./commande.sh"};
    } else {
      return new String[] {"./commande2.sh"};
    }
  }

  public Collection<AnalysisResult> convertToolOutput() {
    Collection<AnalysisResult> res = new ArrayList<AnalysisResult>();
    try {
      Gson gson = new Gson();
      JsonObject jsonObject =
          gson.fromJson(new FileReader(new File(this.reportPath)), JsonObject.class);

      JPanel panel2 = new JPanel();
      JCheckBox singleFileCheckbox = new JCheckBox("Un fichier");
      JCheckBox multipleFilesCheckbox = new JCheckBox("Plusieurs");
      panel2.add(singleFileCheckbox);
      panel2.add(multipleFilesCheckbox);
      JOptionPane.showOptionDialog(
          null,
          panel2,
          "Question 2: Un fichier ou plusieurs?",
          JOptionPane.DEFAULT_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null,
          null,
          null);

      boolean isSingleFileSelected = singleFileCheckbox.isSelected();
      boolean isMultipleFilesSelected = multipleFilesCheckbox.isSelected();
      JsonArray checks = jsonObject.getAsJsonArray("checks");
      for (JsonElement checkElement : checks) {
        JsonObject check = checkElement.getAsJsonObject();
        String kind = check.get("kind").getAsString();
        String title = check.get("title").getAsString();
        String messages = check.get("messages").getAsString();

        // Traiter les réponses ici...

        JsonObject range = check.getAsJsonObject("range");
        JsonObject start = range.getAsJsonObject("start");
        int line = start.get("line").getAsInt();
        String file = start.get("file").getAsString();
        if (file.contains("usr/")) {
          continue;
        }
        Position pos;
        if (isSingleFileSelected) {

          pos =
              SourceCodePositionFinder.findCode(
                      new File(MopsaServerAnalysis.this.rootPath + "/" + file), line)
                  .toPosition();
        } else {
          pos = SourceCodePositionFinder.findCode(new File(file), line).toPosition();
        }
        String msg = title + ": " + messages;
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
}
