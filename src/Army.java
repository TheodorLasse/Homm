package src;

import src.player.PlayerTeam;
import src.sprites.Entities.LivingEntities.CombatLivingEntity;
import src.sprites.Entities.EntityHandler;
import src.tools.Vector2D;
import src.tools.image.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Army {
    private final PlayerTeam team;
    private final ArrayList<CombatLivingEntity> combatEntities;
    private final List<Vector2D> startingPositions;

    public Army(PlayerTeam team){
        this.team = team;
        combatEntities = new ArrayList<>();
        startingPositions = Arrays.asList(new Vector2D(),
                new Vector2D(0, 1), new Vector2D(0, 2), new Vector2D(0, 3),
                new Vector2D(0, 4), new Vector2D(0, 5), new Vector2D(0, 6));

        combatEntities.add(new CombatLivingEntity(startingPositions.get(0), ImageLoader.Character.NINJA_4, team));
        combatEntities.add(new CombatLivingEntity(startingPositions.get(0), ImageLoader.Character.NINJA_5, team));
        combatEntities.add(new CombatLivingEntity(startingPositions.get(0), ImageLoader.Character.NINJA_6, team));
    }

    public void setCombatEntities(EntityHandler combatEntityHandler) {
        for (CombatLivingEntity entity : combatEntities){
            entity.setCombatEntityHandler(combatEntityHandler);
        }
    }

    public ArrayList<CombatLivingEntity> getCombatEntities() {
        return combatEntities;
    }

    public List<Vector2D> getStartingPositions() {
        return startingPositions;
    }
}
