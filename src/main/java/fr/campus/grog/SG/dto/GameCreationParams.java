package fr.campus.grog.SG.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record GameCreationParams(@NotBlank String gameFactoryId, @Positive Integer nbPlayers, @Positive Integer boardSize, List<UUID> opponentIds) {

    //Second constructor (for retro compatibility)
    public GameCreationParams(String gameFactoryId, Integer nbPlayers, Integer boardSize) {
        this(gameFactoryId, nbPlayers, boardSize, null); // opponentIds vaut null par défaut
    }

}
