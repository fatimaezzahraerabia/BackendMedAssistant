package com.rabia.backendmedassistant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class SpaController {

    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect(HttpServletRequest request) throws IOException {
        // Chemin vers le fichier main.js généré par Angular Universal
        String ssrOutput = Files.readString(Paths.get("src/main/resources/ssr/main.js"));
        // Ici, vous devriez appeler le rendu SSR (par exemple, via un processus Node.js)
        // Pour simplifier, redirigez vers index.html si SSR n'est pas encore configuré
        return "forward:/index.html";
    }
}