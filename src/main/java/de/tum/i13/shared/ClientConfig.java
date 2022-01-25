package de.tum.i13.shared;

import picocli.CommandLine;

import java.net.InetSocketAddress;
import java.util.logging.Level;

public class ClientConfig {
    @CommandLine.Option(names = "-p", description = "sets the port for notifications", defaultValue = "3000")
    public int port;

    @CommandLine.Option(names = "-a", description = "which address the server should listen to", defaultValue = "127.0.0.1")
    public String listenaddr;

    @CommandLine.Option(names = "-b", description = "bootstrap broker where client will connect to server", defaultValue = "127.0.0.1:3001")
    public InetSocketAddress serverAddr;

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
