package studentservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Saves and loads service requests to/from a plain text file so that data
 * is not lost when the application is closed.
 * <p>
 * Format: one request per line, fields separated by TAB characters.
 * Tabs, new lines and backslashes inside fields are escaped so that a
 * description containing them cannot break the file.
 */
public final class RequestFileHandler {

    private static final String SEPARATOR = "\t";
    private static final String HEADER =
            "requestId\tstudentId\tname\temail\tcontact\tserviceType\tstatus\tcreatedAt\tlastUpdated\tdescription";
    private static final int FIELD_COUNT = 10;

    private RequestFileHandler() {
    }

    /** Result of loading a file: the requests read plus how many bad lines were skipped. */
    public static final class LoadResult {
        private final List<ServiceRequest> requests;
        private final int skippedLines;

        LoadResult(List<ServiceRequest> requests, int skippedLines) {
            this.requests = Collections.unmodifiableList(requests);
            this.skippedLines = skippedLines;
        }

        public List<ServiceRequest> getRequests() {
            return requests;
        }

        public int getSkippedLines() {
            return skippedLines;
        }
    }

    /** Writes all requests to the file, replacing any existing content. */
    public static void save(List<ServiceRequest> requests, Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (ServiceRequest request : requests) {
                writer.write(toLine(request));
                writer.newLine();
            }
        }
    }

    /**
     * Reads requests from the file. A missing file returns an empty result.
     * Lines that are corrupt are skipped (and counted) rather than crashing the program.
     */
    public static LoadResult load(Path file) throws IOException {
        List<ServiceRequest> loaded = new ArrayList<>();
        if (!Files.exists(file)) {
            return new LoadResult(loaded, 0);
        }
        int skipped = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.equals(HEADER)) {
                        continue;
                    }
                }
                if (line.isBlank()) {
                    continue;
                }
                try {
                    loaded.add(fromLine(line));
                } catch (IllegalArgumentException | NullPointerException | DateTimeParseException e) {
                    skipped++;
                }
            }
        }
        return new LoadResult(loaded, skipped);
    }

    private static String toLine(ServiceRequest request) {
        Student student = request.getStudent();
        return String.join(SEPARATOR,
                escape(request.getRequestId()),
                escape(student.getStudentId()),
                escape(student.getName()),
                escape(student.getEmail()),
                escape(student.getContactNumber()),
                request.getServiceType().name(),
                request.getStatus().name(),
                request.getCreatedAt().toString(),
                request.getLastUpdated().toString(),
                escape(request.getDescription()));
    }

    private static ServiceRequest fromLine(String line) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException("Expected " + FIELD_COUNT + " fields but found " + fields.length);
        }
        Student student = new Student(unescape(fields[1]), unescape(fields[2]),
                unescape(fields[3]), unescape(fields[4]));
        return new ServiceRequest(
                unescape(fields[0]),
                student,
                ServiceType.valueOf(fields[5]),
                unescape(fields[9]),
                RequestStatus.valueOf(fields[6]),
                LocalDateTime.parse(fields[7]),
                LocalDateTime.parse(fields[8]));
    }

    static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    static String unescape(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 't' -> result.append('\t');
                    case 'n' -> result.append('\n');
                    default -> result.append(next);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
