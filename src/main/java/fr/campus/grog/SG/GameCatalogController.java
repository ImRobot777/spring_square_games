package fr.campus.grog.SG;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class GameCatalogController {

    @Autowired
    GameCatalog gameCatalog;

    @GetMapping("/getGameListIds")
    public Collection<String> getGameListIds(){
        return gameCatalog.getAvailableItemIds();
    }
}
