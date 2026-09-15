package fr.campus.grog.SG.dto;

import java.util.UUID;

public record GameCreationParams(String gameFactoryId, Integer nbPlayers, Integer boardSize) {
}
