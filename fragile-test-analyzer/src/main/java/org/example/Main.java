package org.example;

import org.example.FragilityVisitor;
import org.example.TestMethodMetrics;
import org.example.JsonReportWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Точка входа программы для анализа хрупкости тестов.
 * Принимает --input (путь к папке с исходниками) и --output (путь к JSON-отчёту).
 */
public class Main {

    public static void main(String[] args) {
        String inputPath = null;
        String outputPath = "report.json";

        // Простая обработка аргументов командной строки
        for (int i = 0; i < args.length; i++) {
            if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputPath = args[++i];
            } else if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputPath = args[++i];
            }
        }

        if (inputPath == null) {
            System.err.println("Укажите --input <директория с тестами> [--output <файл отчёта>]");
            System.exit(1);
        }

        Path sourcesDir = Paths.get(inputPath);
        if (!Files.isDirectory(sourcesDir)) {
            System.err.println("Указанный путь не является директорией: " + inputPath);
            System.exit(1);
        }

        try {
            // 1. Собрать все .java файлы
            List<Path> javaFiles = collectJavaFiles(sourcesDir);
            System.out.println("Найдено Java-файлов: " + javaFiles.size());

            // 2. Проанализировать каждый файл и собрать метрики
            List<TestMethodMetrics> allMetrics = new ArrayList<>();
            FragilityVisitor visitor = new FragilityVisitor();

            for (Path file : javaFiles) {
                try {
                    CompilationUnit cu = StaticJavaParser.parse(file);
                    // Передаём имя файла для указания класса
                    String className = extractClassName(file.getFileName().toString());
                    List<TestMethodMetrics> fileMetrics = visitor.analyze(cu, className);
                    allMetrics.addAll(fileMetrics);
                } catch (IOException e) {
                    System.err.println("Ошибка парсинга файла: " + file + " - " + e.getMessage());
                }
            }

            System.out.println("Всего проанализировано тестовых методов: " + allMetrics.size());

            // 3. Записать отчёт в JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonReportWriter writer = new JsonReportWriter(mapper);
            writer.write(outputPath, allMetrics);
            System.out.println("Отчёт записан в: " + outputPath);

        } catch (IOException e) {
            System.err.println("Ошибка записи отчёта: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Рекурсивно собирает все файлы с расширением .java в заданной директории.
     */
    private static List<Path> collectJavaFiles(Path rootDir) throws IOException {
        try (Stream<Path> stream = Files.walk(rootDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Извлекает простое имя класса из имени файла (без расширения).
     */
    private static String extractClassName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    }
}
