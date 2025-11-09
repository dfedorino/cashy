package com.dfedorino.cashy.ui.cli.command.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Exports the given object to a JSON file in the current directory.
     *
     * @param data     the object to export (can be a single entity, list, etc.)
     * @param fileName the name of the JSON file (without extension)
     */
    public static void exportToJson(Object data, String fileName) {
        Path filePath = Path.of(fileName + ".json");

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            GSON.toJson(data, writer);
            log.info(">> JSON exported successfully to: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error(">> Failed to export JSON: {}", e.getMessage());
        }
    }

    /**
     * Reads an object from a JSON file in the current directory.
     *
     * @param fileName the file name (without .json extension)
     * @param type     the target type to deserialize
     * @return the deserialized object
     */
    public static <T> Optional<T> importFromJson(String fileName, Class<T> type) {
        Path filePath = Path.of(fileName + ".json");

        try (FileReader reader = new FileReader(filePath.toFile())) {
            return Optional.of(GSON.fromJson(reader, type));
        } catch (IOException e) {
            log.error(">> Failed to read object from JSON: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
