package asistentepa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Especie {

    public String nombreCientifico;
    public String nombreComun;
    public String categoria;
    public int cicloProduccion;
    public double humedadRequerida;
    public double luzRequerida;
    public double temperaturaOptima;
    public double precioVenta;

    private static final String dirData = "localDB";
    private static final String fileName = "especies.csv";
    private static final Path csvPath = Paths.get(dirData, fileName);

    private static final String[] header = new String[]{
        "nombreCientifico", "nombreComun", "categoria", "cicloProduccion",
        "humedadRequerida", "luzRequerida", "temperaturaOptima", "precioVenta"
    };

    public Especie() {
    }

    public Especie(String nombreCientifico, String nombreComun, String categoria,
            int cicloProduccion, double humedadRequerida, double luzRequerida,
            double temperaturaOptima, double precioVenta) {
        this.nombreCientifico = nombreCientifico;
        this.nombreComun = nombreComun;
        this.categoria = categoria;
        this.cicloProduccion = cicloProduccion;
        this.humedadRequerida = humedadRequerida;
        this.luzRequerida = luzRequerida;
        this.temperaturaOptima = temperaturaOptima;
        this.precioVenta = precioVenta;
    }

    public void registrar() {
        validarClave(this);
        ensureFileExists();
        if (buscar(this.nombreCientifico) != null) {
            throw new IllegalStateException("Ya existe Especie con nombreCientifico=" + this.nombreCientifico);
        }
        try (BufferedWriter bw = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            bw.write(toCsvLine(this));
            bw.newLine();
        } catch (IOException ex) {
            throw new RuntimeException("Error escribiendo CSV: " + csvPath, ex);
        }
    }

    public static Especie[] leerTodos() {
        ensureFileExists();
        int total = contarFilasDatos();
        Especie[] arr = new Especie[total];
        int idx = 0;
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] cols = parseCsvLine(line);
                arr[idx++] = fromColumns(cols);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error leyendo CSV: " + csvPath, ex);
        }
        return arr;
    }

    public static Especie buscar(String nombreCientifico) {
        if (nombreCientifico == null || nombreCientifico.trim().isEmpty()) {
            return null;
        }
        String clave = nombreCientifico.trim();
        Especie[] arr = leerTodos();
        for (int i = 0; i < arr.length; i++) {
            Especie e = arr[i];
            if (e != null && e.nombreCientifico != null && e.nombreCientifico.equalsIgnoreCase(clave)) {
                return e;
            }
        }
        return null;
    }

    public static void actualizar(Especie especieActualizada) {
        validarClave(especieActualizada);
        Especie[] arr = leerTodos();
        boolean found = false;
        for (int i = 0; i < arr.length; i++) {
            Especie e = arr[i];
            if (e != null && e.nombreCientifico.equalsIgnoreCase(especieActualizada.nombreCientifico)) {
                arr[i] = especieActualizada;
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("No existe Especie con nombreCientifico=" + especieActualizada.nombreCientifico);
        }
        escribirTodo(arr);
    }

    public static void eliminar(String nombreCientifico) {
        if (nombreCientifico == null || nombreCientifico.trim().isEmpty()) {
            return;
        }
        Especie[] arr = leerTodos();
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            Especie e = arr[i];
            if (e != null && e.nombreCientifico != null && !e.nombreCientifico.equalsIgnoreCase(nombreCientifico.trim())) {
                count++;
            }
        }
        Especie[] nuevo = new Especie[count];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            Especie e = arr[i];
            if (e != null && e.nombreCientifico != null && !e.nombreCientifico.equalsIgnoreCase(nombreCientifico.trim())) {
                nuevo[j++] = e;
            }
        }
        escribirTodo(nuevo);
    }

    private static void validarClave(Especie e) {
        if (e == null) {
            throw new IllegalArgumentException("Especie nula.");
        }
        if (e.nombreCientifico == null || e.nombreCientifico.trim().isEmpty()) {
            throw new IllegalArgumentException("nombreCientifico es obligatorio (clave).");
        }
    }

    private static void ensureFileExists() {
        try {
            Path parent = csvPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(csvPath)) {
                try (BufferedWriter bw = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8)) {
                    bw.write(String.join(",", header));
                    bw.newLine();
                }
            } else if (Files.size(csvPath) == 0) {
                try (BufferedWriter bw = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                    bw.write(String.join(",", header));
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("No fue posible preparar el archivo CSV: " + csvPath, ex);
        }
    }

    private static int contarFilasDatos() {
        int count = 0;
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                count++;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error contando filas CSV: " + csvPath, ex);
        }
        return count;
    }

    private static void escribirTodo(Especie[] especies) {
        ensureFileExists();
        Path tmp = Paths.get(dirData, fileName + ".tmp");
        try (BufferedWriter bw = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write(String.join(",", header));
            bw.newLine();
            for (int i = 0; i < especies.length; i++) {
                Especie e = especies[i];
                if (e != null) {
                    bw.write(toCsvLine(e));
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error escribiendo temporal CSV: " + tmp, ex);
        }
        try {
            Files.move(tmp, csvPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            try {
                Files.move(tmp, csvPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex2) {
                throw new RuntimeException("No fue posible reemplazar el CSV final: " + csvPath, ex2);
            }
        }
    }

    private static String toCsvLine(Especie e) {
        return String.join(",",
                q(e.nombreCientifico),
                q(e.nombreComun),
                q(e.categoria),
                String.valueOf(e.cicloProduccion),
                String.valueOf(e.humedadRequerida),
                String.valueOf(e.luzRequerida),
                String.valueOf(e.temperaturaOptima),
                String.valueOf(e.precioVenta)
        );
    }

    private static String q(String s) {
        if (s == null) {
            s = "";
        }
        String esc = s.replace("\"", "\"\"");
        return "\"" + esc + "\"";
    }

    private static Especie fromColumns(String[] c) {
        if (c.length < 8) {
            throw new IllegalArgumentException("Fila CSV inválida. Esperadas 8 columnas, recibidas: " + c.length);
        }
        Especie e = new Especie();
        e.nombreCientifico = c[0];
        e.nombreComun = c[1];
        e.categoria = c[2];
        e.cicloProduccion = parseInt(c[3]);
        e.humedadRequerida = parseDouble(c[4]);
        e.luzRequerida = parseDouble(c[5]);
        e.temperaturaOptima = parseDouble(c[6]);
        e.precioVenta = parseDouble(c[7]);
        return e;
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception ex) {
            return 0.0;
        }
    }
    private static String[] parseCsvLine(String line) {
        String[] cols = new String[16];
        int count = 0;
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                        sb.append('\"');
                        i += 2;
                        continue;
                    } else {
                        inQuotes = false;
                        i++;
                        continue;
                    }
                } else {
                    sb.append(ch);
                    i++;
                    continue;
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true;
                    i++;
                    continue;
                } else if (ch == ',') {
                    if (count == cols.length) {
                        String[] nuevo = new String[cols.length * 2];
                        for (int k = 0; k < cols.length; k++) {
                            nuevo[k] = cols[k];
                        }
                        cols = nuevo;
                    }
                    cols[count++] = sb.toString();
                    sb.setLength(0);
                    i++;
                    continue;
                } else {
                    sb.append(ch);
                    i++;
                    continue;
                }
            }
        }
        if (count == cols.length) {
            String[] nuevo = new String[cols.length * 2];
            for (int k = 0; k < cols.length; k++) {
                nuevo[k] = cols[k];
            }
            cols = nuevo;
        }
        cols[count++] = sb.toString();
        String[] out = new String[count];
        for (int k = 0; k < count; k++) {
            out[k] = cols[k];
        }
        return out;
    }

    @Override
    public String toString() {
        return "Especie{"
                + "nombreCientifico='" + nombreCientifico + '\''
                + ", nombreComun='" + nombreComun + '\''
                + ", categoria='" + categoria + '\''
                + ", cicloProduccion=" + cicloProduccion
                + ", humedadRequerida=" + humedadRequerida
                + ", luzRequerida=" + luzRequerida
                + ", temperaturaOptima=" + temperaturaOptima
                + ", precioVenta=" + precioVenta
                + '}';
    }

    public static void main(String[] args) {
        Especie e = new Especie("Sedum_morganianum", "Cola de burro", "Suculenta",
                90, 50.0, 6.0, 22.0, 25000.0);
        e.registrar();
        Especie[] todas = Especie.leerTodos();
        for (int i = 0; i < todas.length; i++) {
            System.out.println(todas[i]);
        }
        System.out.println("Buscar: " + Especie.buscar("Sedum_morganianum"));
        e.precioVenta = 28000.0;
        Especie.actualizar(e);
    }
}
