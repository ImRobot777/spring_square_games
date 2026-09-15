package fr.campus.grog.SG.controller;

import fr.campus.grog.SG.service.GameCatalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@RestController
public class GameCatalogController {

    private final GameCatalog gameCatalog;

    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @GetMapping("/gamesIds")
    public Collection<String> getGameListIds() {
        return this.gameCatalog.getAvailableItemIds();
    }

    @GetMapping("/catalog") // or modify your existing endpoint
    public List<String> getGameNames(Locale locale) {
        // Spring automatically populates 'locale' from the HTTP "Accept-Language" header!
        return this.gameCatalog.getAvailableItemNames(locale);
    }

}
