package de.tum.i13.shared;

import picocli.CommandLine;

import java.net.InetSocketAddress;
import java.util.logging.Level;

public class ClientConfig {
    @CommandLine.Option(names = "-p", description = "listen port for notifications", defaultValue = "0")
    public int port;

    @CommandLine.Option(names = "-a", description = "which address the server should listen to", defaultValue = "0.0.0.0")
    public String listenaddr;

    @CommandLine.Option(names = "-bh", description = "address of the bootstrap server", defaultValue = "127.0.0.1")
    public String serverAddr;

    @CommandLine.Option(names = "-bp", description = "port of the bootstrap server", defaultValue = "55430")
    public int serverPort;

    @CommandLine.Option(names = "-d", description = "run inside docker container", defaultValue = "false")
    public boolean isContainer;

    @CommandLine.Option(names = "-h", description = "Displays help", usageHelp = true)
    public boolean usagehelp;

    public static ClientConfig parseCommandlineArgs(String[] args) {
        ClientConfig cfg = new ClientConfig();
        try {
            CommandLine.ParseResult parseResult = new CommandLine(cfg).registerConverter(InetSocketAddress.class, new InetSocketAddressTypeConverter()).registerConverter(Level.class, new LevelTypeConverter()).parseArgs(args);
        } catch (CommandLine.ParameterException e){
            e.printStackTrace();
            CommandLine.usage(new ClientConfig(), System.out);
            System.exit(-1);
        }
        return cfg;
    }

}
