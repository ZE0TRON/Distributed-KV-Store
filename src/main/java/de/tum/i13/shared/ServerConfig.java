package de.tum.i13.shared;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import de.tum.i13.server.storageManagment.CacheDisplacementStrategy;
import picocli.CommandLine;

public class ServerConfig {
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
    
    @CommandLine.Option(names = "-ll", description = "LogLevel", defaultValue = "ALL")
    public Level logLevel;
    
    @CommandLine.Option(names = "-c", description = "Size of the cache,", defaultValue = "100")
    public int cacheSize;
    
    @CommandLine.Option(names = "-s", description = "Cache displacement strategy,", defaultValue = "FIFO")
    public CacheDisplacementStrategy cacheDisplacementStrategy;

    @CommandLine.Option(names = "-h", description = "Displays help", usageHelp = true)
    public boolean usagehelp;

    public static ServerConfig parseCommandlineArgs(String[] args) {
        ServerConfig cfg = new ServerConfig();
        try {
            CommandLine.ParseResult parseResult = new CommandLine(cfg).registerConverter(InetSocketAddress.class, new InetSocketAddressTypeConverter()).registerConverter(Level.class, new LevelTypeConverter()).parseArgs(args);
        } catch (CommandLine.ParameterException e){
            e.printStackTrace();
            CommandLine.usage(new ServerConfig(), System.out);
            System.exit(-1);
        }

        if(!Files.exists(cfg.dataDir)) {
            try {
                Files.createDirectory(cfg.dataDir);
            } catch (IOException e) {
                System.out.println("Could not create directory");
                e.printStackTrace();
                System.exit(-1);
            }
        }
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
                ", logLevel=" + logLevel +
                ", usagehelp=" + usagehelp +
                '}';
    }
}
