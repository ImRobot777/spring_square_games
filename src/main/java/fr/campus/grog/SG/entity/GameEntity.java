package fr.campus.grog.SG.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class GameEntity {
    @Id
    public String id;
    public String factoryId;
    public int boardSize;
    public String playerIds;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "game_id") // Foreign key in game_token_entity table
    public List<GameTokenEntity> tokens = new ArrayList<>();

    // Constructor without argument : Mandatory for Hibernate !
    public GameEntity() {}
}
