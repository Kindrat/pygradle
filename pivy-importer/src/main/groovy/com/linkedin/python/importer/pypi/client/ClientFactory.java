package com.linkedin.python.importer.pypi.client;

import java.io.File;

public class ClientFactory {
    public static Client create(String localRepo) {
        Client client = new PypiClient();
        if (localRepo != null) {
            return new LocalArtifactClient(client, new File(localRepo));
        }
        return client;
    }
}
