package com.example.camel;

import org.apache.camel.main.Main;

public class FileHandlingApplication {
    
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new FileProcessingRoute());
        main.configure().addRoutesBuilder(new FtpSftpRoute());
        main.configure().addRoutesBuilder(new AdvancedFileRoute());
        main.configure().addRoutesBuilder(new MonitoringRoute());
        main.run(args);
    }
}
