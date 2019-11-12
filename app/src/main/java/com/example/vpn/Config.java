package com.example.vpn;

public class Config {
    private String IpAddress;
    private String DnsAddress;

    public String getIpAddress() {
        return IpAddress;
    }

    public void setIpAddress(String ipAddress) {
        IpAddress = ipAddress;
    }

    public String getDnsAddress() {
        return DnsAddress;
    }

    public void setDnsAddress(String dnsAddress) {
        DnsAddress = dnsAddress;
    }
}
