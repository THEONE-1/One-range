package com.one.range.controller.rmi;

import java.rmi.Remote;

public interface Test extends Remote {
    public Object getTest() throws Exception;
}