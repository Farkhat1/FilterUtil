import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String outputDir = ".";
        String prefix = "";
        boolean append = false;
        boolean shortStats = false;
        boolean fullStats = false;

        List<String> inputFiles = new ArrayList<>();

        // Разбор аргументов
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                    if (i + 1 < args.length) outputDir = args[++i];
                    break;
                case "-p":
                    if (i + 1 < args.length) prefix = args[++i];
                    break;
                case "-a":
                    append = true;
                    break;
                case "-s":
                    shortStats = true;
                    break;
                case "-f":
                    fullStats = true;
                    break;
                default:
                    inputFiles.add(args[i]);
            }
        }

        List<Integer> integers = new ArrayList<>();
        List<Double> floats = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        for (String filename : inputFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    if (isInteger(line)) {
                        integers.add(Integer.parseInt(line));
                    } else if (isDouble(line)) {
                        floats.add(Double.parseDouble(line));
                    } else {
                        strings.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Ошибка при чтении файла: " + filename + " — " + e.getMessage());
            }
        }

        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (IOException e) {
            System.err.println("Не удалось создать выходную директорию: " + outputDir);
        }

        writeToFile(integers, outputDir, prefix + "integers.txt", append);
        writeToFile(floats, outputDir, prefix + "floats.txt", append);
        writeToFile(strings, outputDir, prefix + "strings.txt", append);

        if (shortStats || fullStats) {
            System.out.println("\n=== Статистика ===");

            if (!integers.isEmpty()) {
                System.out.println("Целые числа: " + integers.size());
                if (fullStats) {
                    IntSummaryStatistics stats = integers.stream().mapToInt(i -> i).summaryStatistics();
                    System.out.printf("  MIN: %d, MAX: %d, SUM: %d, AVG: %.2f%n",
                            stats.getMin(), stats.getMax(), stats.getSum(), stats.getAverage());
                }
            }

            if (!floats.isEmpty()) {
                System.out.println("Вещественные числа: " + floats.size());
                if (fullStats) {
                    DoubleSummaryStatistics stats = floats.stream().mapToDouble(d -> d).summaryStatistics();
                    System.out.printf("  MIN: %.5f, MAX: %.5f, SUM: %.5f, AVG: %.5f%n",
                            stats.getMin(), stats.getMax(), stats.getSum(), stats.getAverage());
                }
            }

            if (!strings.isEmpty()) {
                System.out.println("Строки: " + strings.size());
                if (fullStats) {
                    int minLen = strings.stream().mapToInt(String::length).min().orElse(0);
                    int maxLen = strings.stream().mapToInt(String::length).max().orElse(0);
                    System.out.printf("  MIN длина: %d, MAX длина: %d%n", minLen, maxLen);
                }
            }
        }
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isDouble(String s) {
        try {
            if (s.contains("f") || s.contains("F")) return false;
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static <T> void writeToFile(List<T> data, String outputDir, String fileName, boolean append) {
        if (data.isEmpty()) return;
        File file = Paths.get(outputDir, fileName).toFile();
        try (PrintWriter out = new PrintWriter(new FileWriter(file, append))) {
            for (T item : data) {
                out.println(item.toString());
            }
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл " + file.getPath() + ": " + e.getMessage());
        }
    }
}
