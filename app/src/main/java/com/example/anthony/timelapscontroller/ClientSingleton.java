package com.example.anthony.timelapscontroller;

import com.app4.project.timelapse.api.client.TimelapseBasicClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.User;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ClientSingleton {
    public static final String API_URL = "https://timelapse-server.herokuapp.com/";
    public static final User USER = new User("android", "fdshsdfmhlhdfs");
    private static TimelapseBasicClient client;

    public static TimelapseBasicClient getClient() {
        if (client == null) {
            client = new TimelapseBasicClient(API_URL);
        }
        return client;
    }

    public static Executor getExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
