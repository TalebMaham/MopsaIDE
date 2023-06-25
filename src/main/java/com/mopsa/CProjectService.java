package com.mopsa;

import java.nio.file.Path;
import java.util.Optional;
import magpiebridge.core.IProjectService;

public class CProjectService implements IProjectService {

  private Optional<Path> rootPath;

  public CProjectService() {
    System.out.println("Cprojet : Constructeur ! ");
    this.rootPath = Optional.empty();
  }

  public Optional<Path> getRootPath() {
    System.out.println("Cprojet : Getter! ");
    return rootPath;
  }

  @Override
  public void setRootPath(Path rootPath) {
    System.out.println("Cprojet : Setter! ");
    this.rootPath = Optional.ofNullable(rootPath);
  }

  @Override
  public String getProjectType() {
    System.out.println("Cprojet : ProjectType ");
    return "c";
  }
}
