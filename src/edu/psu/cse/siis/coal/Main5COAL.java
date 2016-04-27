package edu.psu.cse.siis.coal;

import lu.uni.snt.droidra.AndroidMethodReturnValueAnalyses;

public class Main5COAL {
  public static void main(String[] args) {
    DefaultAnalysis<DefaultCommandLineArguments> analysis = new DefaultAnalysis<>();
    DefaultCommandLineParser parser = new DefaultCommandLineParser();
    DefaultCommandLineArguments commandLineArguments =
        parser.parseCommandLine(args, DefaultCommandLineArguments.class);
    if (commandLineArguments == null) {
      return;
    }
    
    AndroidMethodReturnValueAnalyses.registerAndroidMethodReturnValueAnalyses("");
    analysis.performAnalysis(commandLineArguments);
  }
}
