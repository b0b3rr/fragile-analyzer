package org.example;

import org.example.TestMethodMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Записывает результаты анализа в JSON-файл с помощью Jackson.
 */
public class JsonReportWriter {

    private final ObjectMapper mapper;

    public JsonReportWriter(ObjectMapper mapper) {
        this.mapper = mapper;
        // Включаем красивый вывод (pretty print)
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Записывает список метрик в JSON-файл по указанному пути.
     *
     * @param outputPath путь к выходному файлу
     * @param metrics    список метрик тестовых методов
     * @throws IOException если возникла ошибка ввода-вывода
     */
    public void write(String outputPath, List<TestMethodMetrics> metrics) throws IOException {
        mapper.writeValue(new File(outputPath), metrics);
    }
}