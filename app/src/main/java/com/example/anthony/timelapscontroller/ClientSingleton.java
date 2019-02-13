package com.example.anthony.timelapscontroller;

import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.api.client.TimelapseFakeClient;

public class ClientSingleton {
    private static TimelapseClient client;

    public static TimelapseClient getClient() {
        if (client == null) {
            client = new TimelapseFakeClient();
        }
        return client;
    }
}
