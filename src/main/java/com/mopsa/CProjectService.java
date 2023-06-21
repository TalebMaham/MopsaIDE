
package com.mopsa;

// import magpiebridge.util.FileUtils;

public class CProjectService /*implements IProjectService*/ {

  // private Optional<Path> rootPath;
  // private Set<Path> sourcePath;
  // private Set<Path> includePath;
  // private Set<Path> libraryPath;
  // private Set<String> externalDependencies;

  // public CProjectService() {
  //   this.sourcePath = Collections.emptySet();
  //   this.includePath = Collections.emptySet();
  //   this.libraryPath = Collections.emptySet();
  //   this.externalDependencies = Collections.emptySet();
  //   this.rootPath = Optional.empty();
  // }

  // public CProjectService(
  //     Set<Path> sourcePath,
  //     Set<Path> includePath,
  //     Set<Path> libraryPath,
  //     Set<String> externalDependencies) {
  //   this();
  //   this.sourcePath = sourcePath;
  //   this.includePath = includePath;
  //   this.libraryPath = libraryPath;
  //   this.externalDependencies = externalDependencies;
  // }

  // public Set<Path> getSourcePath() {
  //   return sourcePath;
  // }

  // public Set<Path> getIncludePath() {
  //   return includePath;
  // }

  // public Set<Path> getLibraryPath() {
  //   return libraryPath;
  // }

  // public Set<String> getExternalDependencies() {
  //   return externalDependencies;
  // }

  // public Optional<Path> getRootPath() {
  //   return rootPath;
  // }

  // @Override
  // public void setRootPath(Path rootPath) {
  //   this.rootPath = Optional.ofNullable(rootPath);
  // }

  // public Map<String, Path> getSources() {
  //   Map<String, Path> sources = new HashMap<>();
  //   /* for (Path source : sourcePath) {
  //     if (rootPath.isPresent()) {
  //       Path absoluteSource = rootPath.get().resolve(source);
  //       FileUtils.collectFiles(absoluteSource, ".c", true)
  //           .forEach(file -> sources.put(file.getFileName().toString(), file));
  //     }
  //   }*/
  //   return sources;
  // }

  // public Map<String, Path> getHeaders() {
  //   Map<String, Path> headers = new HashMap<>();
  //   for (Path include : includePath) {
  //     // if (rootPath.isPresent()) {
  //     //   Path absoluteInclude = rootPath.get().resolve(include);
  //     //   FileUtil.collectFiles(absoluteInclude, ".h", true)
  //     //       .forEach(file -> headers.put(file.getFileName().toString(), file));
  //     // }
  //   }
  //   return headers;
  // }

  // public Process executeCommand(String command) throws IOException {
  //   ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
  //   processBuilder.directory(rootPath.orElse(null));
  //   return processBuilder.start();
  // }

  // @Override
  // public String getProjectType() {
  //   // TODO Auto-generated method stub
  //   return "c";
  // }
}
