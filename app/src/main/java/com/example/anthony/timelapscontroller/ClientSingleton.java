package com.example.anthony.timelapscontroller;

import com.app4.project.timelapse.api.client.TimelapseAsyncClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.api.client.TimelapseFakeClient;

public class ClientSingleton {
    public static final String API_URL = "https://timelapse-server.herokuapp.com/";
    private static TimelapseClient client;

    public static TimelapseClient getClient() {
        if (client == null) {
            client = new TimelapseAsyncClient(API_URL);
        }
        return client;
    }
}
