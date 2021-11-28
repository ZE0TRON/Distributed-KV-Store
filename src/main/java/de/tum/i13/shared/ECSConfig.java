package de.tum.i13.shared;

import picocli.CommandLine;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.logging.Level;

public class ECSConfig {
    @CommandLine.Option(names = "-p", description = "sets the port of the server", defaultValue = "5153")
    public int port;

    @CommandLine.Option(names = "-a", description = "which address the server should listen to", defaultValue = "127.0.0.1")
    public String listenaddr;

    @CommandLine.Option(names = "-l", description = "Logfile", defaultValue = "echo.log")
    public Path logfile;
    
    @CommandLine.Option(names = "-ll", description = "LogLevel", defaultValue = "ALL")
    public Level logLevel;
    
    @CommandLine.Option(names = "-h", description = "Displays help", usageHelp = true)
    public boolean usagehelp;

    public static ECSConfig parseCommandlineArgs(String[] args) {
        ECSConfig cfg = new ECSConfig();
        try {
            CommandLine.ParseResult parseResult = new CommandLine(cfg).registerConverter(InetSocketAddress.class, new InetSocketAddressTypeConverter()).registerConverter(Level.class, new LevelTypeConverter()).parseArgs(args);
        } catch (CommandLine.ParameterException e){
            e.printStackTrace();
            CommandLine.usage(new ECSConfig(), System.out);
            System.exit(-1);
        }

        return cfg;
    }

    @Override
    public String toString() {
        return "Config{" +
                "port=" + port +
                ", listenaddr='" + listenaddr + '\'' +
                ", logfile=" + logfile +
                ", logLevel=" + logLevel +
                ", usagehelp=" + usagehelp +
                '}';
    }
}

