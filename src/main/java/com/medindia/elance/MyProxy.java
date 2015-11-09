package com.medindia.elance;

public class MyProxy {
    private String host;
    private String port;

    public MyProxy(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }
}
