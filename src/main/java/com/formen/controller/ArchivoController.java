package com.formen.controller;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ArchivoController {

    @GetMapping({"/pagos/{filename:.+}", "/uploads/pagos/{filename:.+}"})
    public ResponseEntity<?> verPagoAntiguo(@PathVariable String filename) throws Exception {
        String nombre = Path.of(filename.replace("\\", "/")).getFileName().toString();

        Optional<Path> archivo = buscarArchivoPago(nombre);
        if (archivo.isEmpty()) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<h3>No se encontró la captura de pago.</h3>"
                            + "<p>El registro de la venta existe, pero el archivo físico no está en esta carpeta del proyecto.</p>"
                            + "<p>Esto suele pasar cuando se cambia de ZIP/carpeta y la imagen quedó guardada en el proyecto anterior.</p>"
                            + "<p>Realiza una nueva compra de prueba o copia la carpeta <b>uploads/pagos</b> del proyecto anterior al proyecto actual.</p>"
                            + "<a href='/admin/ventas'>Volver a ventas</a>");
        }

        Path path = archivo.get();
        Resource recurso = new PathResource(path);
        String contentType = Files.probeContentType(path);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName().toString() + "\"")
                .body(recurso);
    }

    private Optional<Path> buscarArchivoPago(String nombreArchivo) throws Exception {
        List<Path> candidatos = new ArrayList<>();
        candidatos.add(Path.of("uploads", "pagos", nombreArchivo));
        candidatos.add(Path.of("pagos", nombreArchivo));
        candidatos.add(Path.of("src", "main", "resources", "static", "pagos", nombreArchivo));
        candidatos.add(Path.of("src", "main", "resources", "static", "uploads", "pagos", nombreArchivo));
        candidatos.add(Path.of("target", "classes", "static", "pagos", nombreArchivo));
        candidatos.add(Path.of("target", "classes", "static", "uploads", "pagos", nombreArchivo));

        for (Path candidato : candidatos) {
            Path normalizado = candidato.toAbsolutePath().normalize();
            if (Files.exists(normalizado) && Files.isRegularFile(normalizado)) {
                return Optional.of(normalizado);
            }
        }

        Path base = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        List<Path> raices = new ArrayList<>();
        raices.add(base);
        if (base.getParent() != null) raices.add(base.getParent());
        if (base.getParent() != null && base.getParent().getParent() != null) raices.add(base.getParent().getParent());

        for (Path raiz : raices) {
            if (!Files.exists(raiz)) continue;
            try (var stream = Files.find(raiz, 6,
                    (p, attrs) -> attrs.isRegularFile() && p.getFileName().toString().equals(nombreArchivo))) {
                Optional<Path> encontrado = stream.findFirst();
                if (encontrado.isPresent()) return encontrado.map(p -> p.toAbsolutePath().normalize());
            } catch (Exception ignored) {
                // Si una carpeta no se puede leer, seguimos con las demás.
            }
        }

        return Optional.empty();
    }
}
