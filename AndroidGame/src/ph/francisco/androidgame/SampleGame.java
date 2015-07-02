package ph.francisco.androidgame;

import ph.francisco.framework.Screen;
import ph.francisco.framework.implementation.AndroidGame;

public class SampleGame extends AndroidGame {
    @Override
    public Screen getInitScreen() {
        return new LoadingScreen(this); 
    }

}
