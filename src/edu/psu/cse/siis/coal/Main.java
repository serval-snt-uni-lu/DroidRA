package edu.psu.cse.siis.coal;

public class Main {
  public static void main(String[] args) {
    DefaultAnalysis<DefaultCommandLineArguments> analysis = new DefaultAnalysis<>();
    DefaultCommandLineParser parser = new DefaultCommandLineParser();
    DefaultCommandLineArguments commandLineArguments =
        parser.parseCommandLine(args, DefaultCommandLineArguments.class);
    if (commandLineArguments == null) {
      return;
    }
    analysis.performAnalysis(commandLineArguments);
  }
}
