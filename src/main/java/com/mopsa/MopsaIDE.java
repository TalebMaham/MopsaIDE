package com.mopsa;

import com.google.common.base.Supplier;
import java.io.IOException;
import java.text.MessageFormat;
import magpiebridge.core.IProjectService;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.core.ServerConfiguration;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class MopsaIDE {

  private static final int DEFAULT_TIMEOUT = 5;
  private static final String DEFAULT_DOCKER_IMAGE ="mopsadocker";
  private static final String DEFAULT_PORT = "5007";

  private static CommandLine cmd = null;

  public static void main(String... args) throws IOException, InterruptedException {
	    CommandLine cmd = parseCommandLine(args);
	    if (cmd == null) {
	        printHelpAndExit();
	    }

	    Supplier<MagpieServer> createServer = createServerSupplier(cmd);
	    if (cmd.hasOption("socket")) {
	        int port = getSocketPort(cmd);
	        MagpieServer.launchOnSocketPort(port, createServer);
	    } else {
	        createServer.get().launchOnStdio();
	    }
	}

	private static CommandLine parseCommandLine(String[] args) {
	    Options cliOptions = createCLIOptions();
	    CommandLineParser parser = new DefaultParser();
	    try {
	        return parser.parse(cliOptions, args);
	    } catch (ParseException e) {
	        return null;
	    }
	}

	private static void printHelpAndExit() {
	    Options cliOptions = createCLIOptions();
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("MopsaIDE", cliOptions, true);
	    System.exit(1);
	}

	private static Options createCLIOptions() {
    Options cliOptions = new Options();
    cliOptions.addOption("a", "auto", true,
            MessageFormat.format("active le mode automatique et définit le délai d'attente en minutes pour démarrer l'analyse automatique des outils\n le délai d'attente par défaut est {0}", DEFAULT_TIMEOUT));
    cliOptions.addOption("i", "dockerImage", true,
            MessageFormat.format("image Docker à utiliser lors de l'exécution de mopsa en Docker\n par défaut : `{0}`", DEFAULT_DOCKER_IMAGE));
    cliOptions.addOption("s", "socket", false,
            MessageFormat.format("exécute en mode socket, le port standard est {0}", DEFAULT_PORT));
    cliOptions.addOption("p", "port", true,
            MessageFormat.format("définit le port pour le mode socket, le port standard est {0}", DEFAULT_PORT));
    return cliOptions;
}


	private static Supplier<MagpieServer> createServerSupplier(CommandLine cmd) {
	    return () -> {
	        boolean auto = cmd.hasOption("auto");
	        int timeout = auto ? parseTimeout(cmd.getOptionValue("auto")) : 5;
	        String dockerImage = cmd.getOptionValue("dockerImage", DEFAULT_DOCKER_IMAGE);

	        ServerConfiguration config = createServerConfiguration(auto, timeout);
	        MagpieServer server = new MagpieServer(config);
	        String language = "java";
	        IProjectService javaProjectService = new JavaProjectService();
	        server.addProjectService(language, javaProjectService);
	        ToolAnalysis analysis = new MopsaServerAnalysis();
	        Either<ServerAnalysis, ToolAnalysis> either = Either.forRight(analysis);
	        server.addAnalysis(either, language);
	        return server;
	    };
	}

	private static int getSocketPort(CommandLine cmd) {
	    return Integer.parseInt(cmd.getOptionValue("port", DEFAULT_PORT));
	}

	private static ServerConfiguration createServerConfiguration(boolean auto, int timeout) {
	    ServerConfiguration config = new ServerConfiguration();
	    config.setDoAnalysisBySave(false);
	    config.setDoAnalysisByOpen(!auto);
	    config.setShowConfigurationPage(!auto, true);
	    config.setDoAnalysisByIdle(auto, timeout * 60 * 1000);
	    return config;
	}

	private static int parseTimeout(String value) {
	    try {
	        return Integer.parseInt(value);
	    } catch (NumberFormatException e) {
	        System.exit(1);
	        return -1;
	    }
	}

}
