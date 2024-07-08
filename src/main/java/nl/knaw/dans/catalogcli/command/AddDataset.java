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

    @Parameters(index = "0", paramLabel = "json-file", description = "The JSON or YAML file containing the dataset to be added. Use '-' to read from standard input.")
    private Path jsonFile;

    @Override
    public Integer call() {
        try {
            var json = readJson();
            var objectMapper = getObjectMapper(jsonFile);
            DatasetDto datasetDto = readDatasetDto(json, objectMapper);
            addDataset(nbn, datasetDto);
        }
        catch (CommandException e) {
            System.err.println(e.getMessage());
            return 1;
        }
        return 0;
    }

    private String readJson() throws CommandException {
        var source = "-".equals(jsonFile.toString()) ? "standard input" : jsonFile;
        var json = "";
        try {
            if ("-".equals(jsonFile.toString())) {
                json = new String(System.in.readAllBytes());
            }
            else {
                json = Files.readString(jsonFile);
            }
            return json;
        }
        catch (IOException e) {
            throw new CommandException("Error reading JSON file " + source + ": " + e.getMessage(), e);
        }
    }

    private ObjectMapper getObjectMapper(Path jsonFile) {
        ObjectMapper objectMapper;
        if (jsonFile.getFileName().toString().endsWith(".yaml") || jsonFile.getFileName().toString().endsWith(".yml")) {
            objectMapper = new YAMLMapper();
        }
        else {
            objectMapper = new ObjectMapper();
        }
        // Add module for OffsetDateTime
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    private DatasetDto readDatasetDto(String json, ObjectMapper objectMapper) throws CommandException {
        try {
            var datasetDto = objectMapper.readValue(json, DatasetDto.class);
            if (datasetDto == null) {
                throw new CommandException("Error reading JSON file: no dataset found");
            }
            else if (nbn != null) {
                datasetDto.setNbn(nbn);
                if (datasetDto.getVersionExports() != null) {
                    for (var dve : datasetDto.getVersionExports()) {
                        dve.setDatasetNbn(nbn);
                    }
                }
            }
            return datasetDto;
        }
        catch (IOException e) {
            throw new CommandException("Error reading JSON file: " + e.getMessage(), e);
        }
    }

    private void addDataset(String nbn, DatasetDto datasetDto) throws CommandException {
        try {
            api.addDataset(nbn, datasetDto);
        }
        catch (ApiException e) {
            throw new CommandException("Error adding dataset: " + e.getMessage(), e);
        }
    }
}
