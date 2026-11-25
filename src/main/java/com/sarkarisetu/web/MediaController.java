package com.sarkarisetu.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/** Admin > Media: upload images and copy their address into a post. */
@Controller
@RequestMapping("/admin")
public class MediaController {
    private static final Logger log = LoggerFactory.getLogger(MediaController.class);
    private static final Set<String> OK = Set.of("png", "jpg", "jpeg", "gif", "webp");
    private static final Path ROOT = Path.of("uploads");

    @GetMapping("/media")
    public String page(Model m) throws IOException {
        List<String[]> files = new ArrayList<>();
        if (Files.isDirectory(ROOT)) {
            try (Stream<Path> s = Files.list(ROOT)) {
                s.filter(Files::isRegularFile)
                 .sorted(Comparator.comparingLong((Path p) -> p.toFile().lastModified()).reversed())
                 .forEach(p -> files.add(new String[]{p.getFileName().toString(), "/uploads/" + p.getFileName()}));
            }
        }
        m.addAttribute("files", files);
        return "admin/media";
    }

    @PostMapping("/media/upload")
    public String upload(@RequestParam("file") MultipartFile f, Authentication a, RedirectAttributes ra) {
        String orig = f.getOriginalFilename() == null ? "" : f.getOriginalFilename().toLowerCase();
        String ext = orig.contains(".") ? orig.substring(orig.lastIndexOf('.') + 1) : "";
        String type = f.getContentType();
        if (f.isEmpty() || !OK.contains(ext) || type == null || !type.startsWith("image/")) {
            ra.addFlashAttribute("err", "Choose a png, jpg, gif or webp image (up to 5 MB).");
            return "redirect:/admin/media";
        }
        try {
            Files.createDirectories(ROOT);
            String name = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "." + ext;
            f.transferTo(ROOT.resolve(name).toAbsolutePath());
            log.info("image uploaded by {}: {}", a.getName(), name);
            ra.addFlashAttribute("ok", "Uploaded. Use this address in your post: /uploads/" + name);
        } catch (IOException e) {
            log.error("upload failed: {}", e.getMessage());
            ra.addFlashAttribute("err", "Could not save the file.");
        }
        return "redirect:/admin/media";
    }

    @PostMapping("/media/delete")
    public String delete(@RequestParam String name, Authentication a, RedirectAttributes ra) {
        try {
            Path p = ROOT.resolve(Path.of(name).getFileName().toString()).normalize();
            if (p.startsWith(ROOT) && Files.deleteIfExists(p)) { log.info("image deleted by {}: {}", a.getName(), name); ra.addFlashAttribute("ok", "Deleted."); }
        } catch (IOException | RuntimeException e) {
            ra.addFlashAttribute("err", "Could not delete the file.");
        }
        return "redirect:/admin/media";
    }
}
