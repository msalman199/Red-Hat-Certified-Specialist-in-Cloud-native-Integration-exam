package com.example.camel;

import org.apache.camel.builder.RouteBuilder;

public class AdvancedFileRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        // Content-based routing
        from("file:advanced-input?noop=false&move=advanced-processed")
            .routeId("content-based-routing")
            .log("Processing file with content-based routing: ${header.CamelFileName}")
            .choice()
                .when(body().contains("URGENT"))
                    .log("Urgent file detected: ${header.CamelFileName}")
                    .to("file:urgent-output")
                    .to("ftp://ftpuser@localhost:21/urgent?password=ftppass123&binary=true")
                .when(body().contains("ARCHIVE"))
                    .log("Archive file detected: ${header.CamelFileName}")
                    .to("file:archive-output")
                    .to("sftp://sftpuser@localhost:22/archive?password=sftppass123&binary=true")
                .otherwise()
                    .log("Regular file: ${header.CamelFileName}")
                    .to("file:regular-output")
            .end();
        
        // File size-based routing
        from("file:size-input?noop=false&move=size-processed")
            .routeId("size-based-routing")
            .log("File size: ${header.CamelFileLength} bytes")
            .choice()
                .when(header("CamelFileLength").isGreaterThan(1000))
                    .log("Large file: ${header.CamelFileName}")
                    .to("file:large-files")
                .otherwise()
                    .log("Small file: ${header.CamelFileName}")
                    .to("file:small-files")
            .end();
        
        // File extension-based routing
        from("file:extension-input?noop=false&move=extension-processed")
            .routeId("extension-based-routing")
            .choice()
                .when(header("CamelFileNameOnly").endsWith(".txt"))
                    .to("file:text-files")
                .when(header("CamelFileNameOnly").endsWith(".csv"))
                    .to("file:csv-files")
                .when(header("CamelFileNameOnly").endsWith(".xml"))
                    .to("file:xml-files")
                .otherwise()
                    .to("file:other-files")
            .end();
    }
}
