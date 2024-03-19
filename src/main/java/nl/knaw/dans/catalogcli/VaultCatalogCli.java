/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.catalogcli;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.core.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.catalogcli.client.ApiClient;
import nl.knaw.dans.catalogcli.config.VaultCatalogConfig;
import nl.knaw.dans.lib.util.AbstractCommandLineApp;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "vault-catalog",
         mixinStandardHelpOptions = true,
         versionProvider = VersionProvider.class,
         description = "Manage the Data Vault Catalog.")
@Slf4j
public class VaultCatalogCli extends AbstractCommandLineApp<VaultCatalogConfig> {
    public static void main(String[] args) throws Exception {
        new VaultCatalogCli().run(args);
    }

    public String getName() {
        return "Vault Catalog CLI";
    }

    @Override
    public void configureCommandLine(CommandLine commandLine, VaultCatalogConfig config) {

    }

    private void createDefaultApi(VaultCatalogConfig config) {
        log.debug("Creating DefaultApi");
        var client = new JerseyClientBuilder(new Environment(VaultCatalogCli.class.getSimpleName()))
            .using(config.getVaultCatalogService().getHttpClient())
            .build("vault-catalog-client");

        var apiClient = new ApiClient();
        apiClient.setHttpClient(client);
        apiClient.setBasePath(config.getVaultCatalogService().getUrl().toString());
    }

}
