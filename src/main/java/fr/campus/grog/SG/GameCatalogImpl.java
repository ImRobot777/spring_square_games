package fr.campus.grog.SG;

import fr.le_campus_numerique.square_games.engine.connectfour.ConnectFourGameFactory;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;


@Service
public class GameCatalogImpl implements GameCatalog  {

    private TicTacToeGameFactory  tictactoeGameFactory =  new TicTacToeGameFactory();
    private TaquinGameFactory  taquinGameFactory =  new TaquinGameFactory();
    private ConnectFourGameFactory  connectfourGameFactory =  new ConnectFourGameFactory();

    @Override
    public Collection<String> getAvailableItemIds() {

        return List.of(
                tictactoeGameFactory.getGameFactoryId()
                , taquinGameFactory.getGameFactoryId()
                , connectfourGameFactory.getGameFactoryId()
        );
    }

}
