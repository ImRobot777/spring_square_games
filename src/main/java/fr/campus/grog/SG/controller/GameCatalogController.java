package fr.campus.grog.SG.controller;

import fr.campus.grog.SG.service.GameCatalog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
public class GameCatalogController {

    @Autowired
    GameCatalog gameCatalog;

    @GetMapping("/gamesIds")
    public Collection<String> getGameListIds(){
        return gameCatalog.getAvailableItemIds();
    }

    @GetMapping("/catalog") // or modify your existing endpoint
    public List<String> getGameNames(Locale locale) {
        // Spring automatically populates 'locale' from the HTTP "Accept-Language" header!
        return this.gameCatalog.getAvailableItemNames(locale);
    }

}
