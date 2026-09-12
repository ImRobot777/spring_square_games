package fr.campus.grog.SG;

import com.sun.source.util.Plugin;
import fr.le_campus_numerique.square_games.engine.connectfour.ConnectFourGameFactory;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class GameCatalogImpl implements GameCatalog  {

    private final Map<String, GamePlugin> plugins = new HashMap<>();

    // Spring automatically collects and injects(new()) all beans implementing GamePlugin
    public GameCatalogImpl(List<GamePlugin> pluginList) {
        for(GamePlugin plugin : pluginList){
            this.plugins.put(plugin.getId(), plugin);
        }
    }

    @Override
    public Collection<String> getAvailableItemIds() {
        // keySet() directly provides all registered game identifiers
        return this.plugins.keySet();
    }

    @Override
    public List<String> getAvailableItemNames(Locale locale) {
        // 1. Initialize an empty list to accumulate translated names
        List<String> names = new ArrayList<>();

        // 2. Loop through all plugins and retrieve the localized name
        for (GamePlugin plugin : this.plugins.values()) {
            names.add(plugin.getName(locale));
        }

        // 3. Return the populated list
        return names;
    }

}
