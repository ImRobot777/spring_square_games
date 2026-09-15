package fr.campus.grog.SG.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class GameTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    public Long id;

    public String ownerId;
    public String name;
    public boolean removed;
    public Integer x;
    public Integer y;

    public GameTokenEntity() {}
}