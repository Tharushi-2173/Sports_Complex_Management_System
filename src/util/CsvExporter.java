package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {
    public static void export(File file, List<String> header, List<List<String>> rows) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            if (header != null && !header.isEmpty()) {
                bw.write(String.join(",", header));
                bw.newLine();
            }
            for (List<String> r : rows) {
                bw.write(escapeRow(r));
                bw.newLine();
            }
        }
    }

    private static String escapeRow(List<String> row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.size(); i++) {
            String cell = row.get(i);
            if (cell == null) cell = "";
            boolean needsQuote = cell.contains(",") || cell.contains("\n") || cell.contains("\"");
            if (needsQuote) {
                cell = cell.replace("\"", "\"\"");
                sb.append('"').append(cell).append('"');
            } else {
                sb.append(cell);
            }
            if (i < row.size() - 1) sb.append(',');
        }
        return sb.toString();
    }
}


