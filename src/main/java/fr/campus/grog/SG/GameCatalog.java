package fr.campus.grog.SG;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public interface GameCatalog {
    public Collection<String> getAvailableItemIds();

    public List<String> getAvailableItemNames(Locale locale);
}
