package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class FtpSftpRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        // FTP file upload route
        from("file:ftp-upload?noop=false&move=ftp-processed")
            .routeId("ftp-upload-route")
            .log("Uploading file to FTP: ${header.CamelFileName}")
            .to("ftp://ftpuser@localhost:21/upload?password=ftppass123&binary=true")
            .log("File uploaded to FTP successfully: ${header.CamelFileName}");
        
        // FTP file download route
        from("ftp://ftpuser@localhost:21/download?password=ftppass123&binary=true&delete=true")
            .routeId("ftp-download-route")
            .log("Downloaded file from FTP: ${header.CamelFileName}")
            .to("file:ftp-downloaded")
            .log("File saved locally: ${header.CamelFileName}");
        
        // SFTP file upload route
        from("file:sftp-upload?noop=false&move=sftp-processed")
            .routeId("sftp-upload-route")
            .log("Uploading file to SFTP: ${header.CamelFileName}")
            .to("sftp://sftpuser@localhost:22/upload?password=sftppass123&binary=true")
            .log("File uploaded to SFTP successfully: ${header.CamelFileName}");
        
        // SFTP file download route
        from("sftp://sftpuser@localhost:22/download?password=sftppass123&binary=true&delete=true")
            .routeId("sftp-download-route")
            .log("Downloaded file from SFTP: ${header.CamelFileName}")
            .to("file:sftp-downloaded")
            .log("File saved locally: ${header.CamelFileName}");
        
        // File synchronization route (Local to FTP to SFTP)
        from("file:sync-source?noop=false&move=sync-processed")
            .routeId("file-sync-route")
            .log("Synchronizing file: ${header.CamelFileName}")
            .multicast()
            .to("ftp://ftpuser@localhost:21/upload?password=ftppass123&binary=true")
            .to("sftp://sftpuser@localhost:22/upload?password=sftppass123&binary=true")
            .to("file:sync-completed")
            .log("File synchronized across all destinations: ${header.CamelFileName}");
        
        // Error handling for FTP/SFTP operations
        onException(Exception.class)
            .handled(true)
            .log("Error in FTP/SFTP operation: ${exception.message}")
            .to("file:ftp-sftp-errors");
    }
}
