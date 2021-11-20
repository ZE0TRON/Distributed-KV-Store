package de.tum.i13.shared;

import picocli.CommandLine;

import java.util.logging.Level;

public class LevelTypeConverter implements CommandLine.ITypeConverter<Level> {
    @Override
    public Level convert(final String value) throws Exception {
        return Level.parse(value);
    }
}