package de.tum.i13.shared;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import de.tum.i13.server.storageManagment.CacheDisplacementStrategy;
import picocli.CommandLine;

public class Config {
    @CommandLine.Option(names = "-p", description = "sets the port of the server", defaultValue = "5153")
    public int port;

    @CommandLine.Option(names = "-a", description = "which address the server should listen to", defaultValue = "127.0.0.1")
    public String listenaddr;

    @CommandLine.Option(names = "-b", description = "bootstrap broker where clients and other brokers connect first to retrieve configuration, port and ip, e.g., 192.168.1.1:5153", defaultValue = "clouddatabases.i13.in.tum.de:5153")
    public InetSocketAddress bootstrap;

    @CommandLine.Option(names = "-d", description = "Directory for files", defaultValue = "data/")
    public Path dataDir;

    @CommandLine.Option(names = "-l", description = "Logfile", defaultValue = "echo.log")
    public Path logfile;
    
//    @CommandLine.Option(names = "-ll", description = "LogLevel", defaultValue = "ALL")
//    public Level logLevel;
    
    @CommandLine.Option(names = "-c", description = "Size of the cache,", defaultValue = "100")
    public int cacheSize;
    
    @CommandLine.Option(names = "-s", description = "Cache displacement strategy,", defaultValue = "FIFO")
    public CacheDisplacementStrategy cacheDisplacementStrategy;

    @CommandLine.Option(names = "-h", description = "Displays help", usageHelp = true)
    public boolean usagehelp;

    public static Config parseCommandlineArgs(String[] args) {
        Config cfg = new Config();
        // TODO fix loglevel issue
        CommandLine.ParseResult addressParseResult = new CommandLine(cfg).registerConverter(InetSocketAddress.class, new InetSocketAddressTypeConverter()).parseArgs(args);
        //CommandLine.ParseResult logLevelParseResult = new CommandLine(cfg).registerConverter(Level.class, new LevelTypeConverter()).parseArgs(args);

        if(!Files.exists(cfg.dataDir)) {
            try {
                Files.createDirectory(cfg.dataDir);
            } catch (IOException e) {
                System.out.println("Could not create directory");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        if(!addressParseResult.errors().isEmpty()) {
            for(Exception ex : addressParseResult.errors()) {
                ex.printStackTrace();
            }

            CommandLine.usage(new Config(), System.out);
            System.exit(-1);
        }
//        if(!logLevelParseResult.errors().isEmpty()) {
//            for(Exception ex : logLevelParseResult.errors()) {
//                ex.printStackTrace();
//            }
//
//            CommandLine.usage(new Config(), System.out);
//            System.exit(-1);
//        }

        return cfg;
    }

    @Override
    public String toString() {
        return "Config{" +
                "port=" + port +
                ", listenaddr='" + listenaddr + '\'' +
                ", bootstrap=" + bootstrap +
                ", dataDir=" + dataDir +
                ", logfile=" + logfile +
                ", usagehelp=" + usagehelp +
                '}';
    }
}

