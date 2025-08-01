package com.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.stream.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

@Controller
@RequestMapping("/wasistinderpost")
public class PostkastenController {

    private static final int MAX_ATTEMPTS = 10;

    @Value("${postkasten.folder}")
    private String folder;

    @Value("${postkasten.password}")
    private String password;

    @GetMapping
    public String showPage(HttpSession session, Model model) throws IOException {
        Boolean authed = (Boolean) session.getAttribute("authed");
        Boolean locked = (Boolean) session.getAttribute("locked");
        Integer attempts = (Integer) session.getAttribute("attempts");
        if (locked != null && locked) {
            return "locked";
        }
        if (authed != null && authed) {
            Path dir = Paths.get(folder);
            // alle PNG-Dateien sammeln
            List<Path> pngs = Files.list(dir)
                    .filter(p -> p.toString().toLowerCase().endsWith(".png"))
                    .collect(Collectors.toList());

            // Dateinamen für die Galerie
            List<String> images = pngs.stream()
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
            model.addAttribute("images", images);

            // neuestes Änderungsdatum ermitteln
            Optional<FileTime> latest = pngs.stream()
                    .map(p -> {
                        try {
                            return Files.getLastModifiedTime(p);
                        } catch (IOException e) {
                            return FileTime.fromMillis(0);
                        }
                    })
                    .max(Comparator.naturalOrder());

            String lastUpdated = latest
                    .map(ft -> LocalDateTime.ofInstant(ft.toInstant(), ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                    .orElse("–");
            model.addAttribute("lastUpdated", lastUpdated);

            return "gallery";
        }
        if (attempts == null) attempts = 0;
        model.addAttribute("attemptsLeft", MAX_ATTEMPTS - attempts);
        return "login";
    }

    @GetMapping("<128BIT-HASH>")
    public String showPagePW(HttpSession session, Model model) throws IOException {
        Path dir = Paths.get(folder);
        // alle PNG-Dateien sammeln
        List<Path> pngs = Files.list(dir)
                .filter(p -> p.toString().toLowerCase().endsWith(".png"))
                .collect(Collectors.toList());

        // Dateinamen für die Galerie
        List<String> images = pngs.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
        model.addAttribute("images", images);

        // neuestes Änderungsdatum ermitteln
        Optional<FileTime> latest = pngs.stream()
                .map(p -> {
                    try {
                        return Files.getLastModifiedTime(p);
                    } catch (IOException e) {
                        return FileTime.fromMillis(0);
                    }
                })
                .max(Comparator.naturalOrder());

        String lastUpdated = latest
                .map(ft -> LocalDateTime.ofInstant(ft.toInstant(), ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .orElse("–");
        model.addAttribute("lastUpdated", lastUpdated);

        return "gallery";
    }

    @PostMapping
    public String doLogin(@RequestParam String pass, HttpSession session, Model model) {
        Integer attempts = (Integer) session.getAttribute("attempts");
        if (attempts == null) attempts = 0;
        attempts++;
        session.setAttribute("attempts", attempts);

        if (attempts > MAX_ATTEMPTS) {
            session.setAttribute("locked", true);
            return "locked";
        }
        if (password.equals(pass)) {
            session.setAttribute("authed", true);
            return "redirect:/wasistinderpost";
        }
        model.addAttribute("error", "Falsches Passwort");
        model.addAttribute("attemptsLeft", MAX_ATTEMPTS - attempts);
        return "login";
    }
}