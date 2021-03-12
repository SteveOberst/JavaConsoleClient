package com.github.ninurtax.consoleclient.modules.impl.script;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class ScriptReader extends FileReader {

        private Scanner scanner;

        public ScriptReader(@NotNull String fileName) throws FileNotFoundException {
            super(fileName);
            this.scanner = new Scanner(this);
        }

        public String getNextLine() {
            return scanner.nextLine();
        }

        public boolean hasNextLine() {
            return scanner.hasNextLine();
        }


    }