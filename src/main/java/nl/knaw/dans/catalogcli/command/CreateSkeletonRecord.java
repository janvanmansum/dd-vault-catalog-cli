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
package nl.knaw.dans.catalogcli.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.knaw.dans.catalogcli.api.DatasetDto;
import nl.knaw.dans.catalogcli.api.VersionExportDto;
import nl.knaw.dans.catalogcli.client.ApiException;
import nl.knaw.dans.catalogcli.client.DefaultApi;
import nl.knaw.dans.validation.UrnUuid;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "create-skeleton-record",
         mixinStandardHelpOptions = true,
         description = "Create a skeleton record in the Data Vault Catalog.")
@RequiredArgsConstructor
public class CreateSkeletonRecord implements Callable<Integer> {
    @NonNull
    private final DefaultApi api;

    @Option(names = { "-n", "--nbn" },
            description = "The NBN of the dataset.",
            required = true)
    private String nbn;

    @Option(names = { "-d", "--datastation", "--data-station" },
            description = "The datastation from which the dataset was exported. If the NBN refers to a new dataset, this option is required. If the NBN refers to an existing dataset, this option is ignored.")
    private String datastation;

    @Option(names = { "-o", "--ocfl-object-version-number" },
            description = "The OCFL object version number of the dataset version export.",
            required = true)
    private Integer ocflObjectVersionNumber;

    @Option(names = { "-b", "--bag-id" },
            description = "The bag-id of the dataset version export.",
            required = true)
    @UrnUuid
    private String bagId;

    @Option(names = { "-c", "--creation-timestamp" },
            description = "The creation timestamp of the dataset version export. If not provided, the current timestamp is used.")
    private OffsetDateTime creationTimestamp;

    @Override
    public Integer call() {
        try {
            var optDataset = getDataset(nbn);
            if (optDataset.isEmpty()) {
                createDataset(nbn);
            }
            System.err.println("Created skeleton record for dataset with NBN " + nbn);
            return 0;
        }
        catch (ApiException e) {
            System.err.println("Error creating skeleton record: " + e.getMessage());
            return 1;
        }
    }

    private Optional<DatasetDto> getDataset(String nbn) throws ApiException {
        try {
            return Optional.of(api.getDataset(nbn, null)); // Use the default media type, which is application/json
        }
        catch (ApiException e) {
            if (e.getCode() == 404) {
                return Optional.empty();
            }
            else {
                throw e;
            }
        }
    }

    private DatasetDto createDataset(String nbn) throws ApiException {
        var datasetDto = new DatasetDto()
            .nbn(nbn)
            .datastation(datastation)
            .addVersionExportsItem(new VersionExportDto()
                .datasetNbn(nbn)
                .bagId(bagId)
                .ocflObjectVersionNumber(ocflObjectVersionNumber)
                .createdTimestamp(getCreationTimestamp())
                .skeletonRecord(true));

        api.addDataset(nbn, datasetDto);
        return datasetDto;
    }

    private OffsetDateTime getCreationTimestamp() {
        return creationTimestamp != null ? creationTimestamp : OffsetDateTime.now();
    }

}
