package com.fintellix.platformcore.security;

import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;

public class EnvironmentStringPBEConfigIcreate extends EnvironmentStringPBEConfig {

    private static final String password = "!cr3@t3#b@nk!ng!nt3ll!53n53#1601";

    public EnvironmentStringPBEConfigIcreate() {
        setPassword(password);
    }
}