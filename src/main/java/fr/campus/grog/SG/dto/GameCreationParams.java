package fr.campus.grog.SG.dto;

import java.util.List;
import java.util.UUID;

public record GameCreationParams(String gameFactoryId, Integer nbPlayers, Integer boardSize, List<UUID> opponentIds) {

    //Second constructor (for retro compatibility)
    public GameCreationParams(String gameFactoryId, Integer nbPlayers, Integer boardSize) {
        this(gameFactoryId, nbPlayers, boardSize, null); // opponentIds vaut null par défaut
    }

}
