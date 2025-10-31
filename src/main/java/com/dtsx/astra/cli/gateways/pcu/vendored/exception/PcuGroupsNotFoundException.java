package com.dtsx.astra.cli.gateways.pcu.vendored.exception;

public class PcuGroupsNotFoundException extends RuntimeException {
    public PcuGroupsNotFoundException(String message) {
        super(message); // unfortunately the devops api doesn't return a very workable error so will just use the message directly
    }
}
