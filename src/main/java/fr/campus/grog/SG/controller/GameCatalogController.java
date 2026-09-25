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
@Tag(name = "Game Catalog", description = "Available games catalog and multilingual translation")
public class GameCatalogController {

    private final GameCatalog gameCatalog;

    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @Operation(summary = "List technical game identifiers", description = "Returns the list of available game identifiers (e.g., tictactoe, connect4, taquin).")
    @GetMapping("/gamesIds")
    public Collection<String> getGameListIds() {
        return this.gameCatalog.getAvailableItemIds();
    }

    @Operation(summary = "Get translated game catalog", description = "Returns translated game names based on the standard HTTP Accept-Language header (e.g., fr, en).")
    @GetMapping("/catalog")
    public List<String> getGameNames(
            @Parameter(description = "Locale automatically resolved by Spring from the Accept-Language header", hidden = true)
            Locale locale) {
        // Spring automatically populates 'locale' from the HTTP "Accept-Language" header!
        return this.gameCatalog.getAvailableItemNames(locale);
    }

}
