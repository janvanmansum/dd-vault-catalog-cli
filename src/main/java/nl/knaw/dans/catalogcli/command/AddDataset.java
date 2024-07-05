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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.knaw.dans.catalogcli.api.DatasetDto;
import nl.knaw.dans.catalogcli.client.ApiException;
import nl.knaw.dans.catalogcli.client.DefaultApi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "add-dataset",
         mixinStandardHelpOptions = true,
         description = "Create a dataset in the Data Vault Catalog.")
@RequiredArgsConstructor
public class AddDataset implements Callable<Integer> {
    @NonNull
    private final DefaultApi api;

    @Option(names = { "-n", "--nbn" },
            description = "The NBN of the dataset. If also present in the JSON file, the value from the command line overwrites the value from the file.")
    private String nbn;

    @Parameters(index = "0", paramLabel = "json-file", description = "The JSON or YAML file containing the dataset to be added.")
    private String jsonFile;

    @Override
    public Integer call() {
        String json = null;
        try {
            json = Files.readString(Path.of(jsonFile));
        }
        catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }
        ObjectMapper objectMapper;
        if (jsonFile.endsWith(".yaml") || jsonFile.endsWith(".yml")) {
            objectMapper = new YAMLMapper();
        }
        else {
            objectMapper = new ObjectMapper();
        }
        // Add module for OffsetDateTime
        objectMapper.findAndRegisterModules();
        DatasetDto datasetDto = null;
        try {
            datasetDto = objectMapper.readValue(json, DatasetDto.class);
            if (datasetDto == null) {
                System.err.println("Error reading JSON file: no dataset found");
                return 1;
            }
            else if (nbn != null) {
                datasetDto.setNbn(nbn);
                if (datasetDto.getVersionExports() != null) {
                    for (var dve : datasetDto.getVersionExports()) {
                        dve.setDatasetNbn(nbn);
                    }
                }
            }
        }
        catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }

        try {
            api.addDataset(nbn, datasetDto);
        }
        catch (ApiException e) {
            System.err.println("Error adding dataset: " + e.getMessage());
        }
        return 0;
    }

}
