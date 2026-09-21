package fr.campus.grog.SG.controller;

import fr.campus.grog.SG.service.GameCatalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@RestController
@Tag(name = "Catalogue de jeux", description = "Consultation des jeux disponibles et traduction multilingue")
public class GameCatalogController {

    private final GameCatalog gameCatalog;

    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @Operation(summary = "Lister les identifiants techniques des jeux", description = "Retourne la liste des identifiants disponibles (ex: tictactoe, connect4, taquin).")
    @GetMapping("/gamesIds")
    public Collection<String> getGameListIds() {
        return this.gameCatalog.getAvailableItemIds();
    }

    @Operation(summary = "Consulter le catalogue traduit", description = "Retourne les noms traduits des jeux selon l'en-tête standard HTTP Accept-Language (ex: fr, en).")
    @GetMapping("/catalog")
    public List<String> getGameNames(
            @Parameter(description = "Locale déduite automatiquement par Spring depuis l'en-tête Accept-Language", hidden = true)
            Locale locale) {
        // Spring automatically populates 'locale' from the HTTP "Accept-Language" header!
        return this.gameCatalog.getAvailableItemNames(locale);
    }

}
