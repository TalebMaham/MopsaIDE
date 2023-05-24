package com.mopsa;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.util.collections.Pair;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.Kind;
import org.eclipse.lsp4j.DiagnosticSeverity;

public class MopsaResult implements AnalysisResult {

private final Kind type;
  private final Position position;
  private final String message;
  private final Iterable<Pair<Position, String>> informationsConnexes;
  private final DiagnosticSeverity gravite;
  private final Pair<Position, String> reparation;
  private final String code;

  public MopsaResult(
      Kind type,
      Position position,
      String message,
      Iterable<Pair<Position, String>> informationsConnexes,
      DiagnosticSeverity gravite,
      Pair<Position, String> reparation,
      String code) {
    this.type = type;
    this.position = position;
    this.message = message;
    this.informationsConnexes = informationsConnexes;
    this.gravite = gravite;
    this.reparation = reparation;
    this.code = code;
  }

  public Kind kind() {
    return this.type;
  }

  public Position position() {
    return position;
  }

  public Iterable<Pair<Position, String>> related() {
    return informationsConnexes;
  }

  public DiagnosticSeverity severity() {
    return gravite;
  }

  public Pair<Position, String> repair() {
    return reparation;
  }

  public String toString(boolean useMarkdown) {
    return message;
  }

  @Override
  public String toString() {
    return "DataFlowResult [kind="
        + type
        + ", position="
        + position
        + ", code="
        + code
        + ", message="
        + message
        + ", related="
        + informationsConnexes
        + ", severity="
        + gravite
        + ", repair="
        + reparation
        + "]";
  }

  public String code() {
    return code;
  }

}
