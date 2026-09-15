package fr.campus.grog.SG.dto;

import fr.le_campus_numerique.square_games.engine.CellPosition;

public record MoveParams(CellPosition target, CellPosition source) {
}
