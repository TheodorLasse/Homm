package src.player;

public class PlayerTeam {
    PlayerResources playerResources;

    public PlayerTeam(PlayerTeamColors color){
        playerResources = new PlayerResources();
    }

    public PlayerResources getPlayerResources() {
        return playerResources;
    }
}
