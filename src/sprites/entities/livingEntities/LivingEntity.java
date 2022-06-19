package src.sprites.entities.livingEntities;

import src.Game;
import src.sprites.entities.Entity;
import src.sprites.entities.EntityHandler;
import src.sprites.entities.EntityType;
import src.tools.Rotation;
import src.tools.WindowFocus;
import src.player.PlayerTeam;
import src.tools.aStar.PathFinder;
import src.tools.aStar.PathMap;
import src.tools.image.ImageLoader;
import src.tools.Vector2D;
import src.tools.aStar.Path;
import src.tools.time.DeltaTime;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class LivingEntity extends Entity {
    protected Character.CharacterEnum character;
    protected Animation animation;
    protected boolean alive = true;
    protected PlayerTeam team;
    protected EntityHandler entityHandler;
    protected BufferedImage flag;
    protected Path path;
    protected int movement;
    protected double timeUntilMove = 0;
    protected double timeBetweenMoves = 0.3;
    protected int tileSize = 0;

    /**
     * A unit on the GameMap that can move, belongs to a team and has an army
     * @param position Entity's position
     * @param character Entity's character, i.e list of animations
     * @param team Entity's team
     * @param entityHandler EntityHandler which keeps track of Entities on the GameMap
     */
    public LivingEntity(Vector2D position, Character.CharacterEnum character, PlayerTeam team, EntityHandler entityHandler) {
        super(position, new Vector2D(1, 1), 0, null);
        this.entityHandler = entityHandler;
        this.team = team;
        this.character = character;
        this.animation = new Animation(character);
        this.texture = animation.getAnimationFrame(rotation);
        movement = 5;
        setEntityType(EntityType.LIVING);

        switch (team.getTeamColor()){
            case RED -> flag = Game.imageLoader.getImage(ImageLoader.ImageName.RED_FLAG);
            case BLUE -> flag = Game.imageLoader.getImage(ImageLoader.ImageName.BLUE_FLAG);
            default -> flag = Game.imageLoader.getImage(ImageLoader.ImageName.ERROR);
        }
    }

    @Override
    public void update(DeltaTime deltaTime, WindowFocus focus) {
        super.update(deltaTime, focus);
        if (alive) move(deltaTime, focus);
        animation.update(deltaTime);
        this.texture = animation.getAnimationFrame(rotation);
        this.tileSize = focus.getTileSize();
    }

    protected void move(DeltaTime deltaTime, WindowFocus focus){
        if (isStationary() || getMovement() < 0) {
            animation.setAnimation(LivingEntityState.IDLE);
            path = null;
            return;
        }

        timeUntilMove -= deltaTime.getSeconds();
        if (timeUntilMove <= 0){
            Path.Step nextStep = path.popStep();
            this.position.setX(nextStep.getX());
            this.position.setY(nextStep.getY());
            updateRelativePos(focus);
            timeUntilMove = timeBetweenMoves;
            setMovement(getMovement() - 1);
        }

        if (!isStationary()) {
            double directionLength = (1 - timeUntilMove / timeBetweenMoves) * tileSize;
            Vector2D direction = new Vector2D(
                    path.getX(0) - position.getX(),
                    path.getY(0) - position.getY());
            updateRotation(direction);
            Vector2D directionAdjusted = Vector2D.getProduct(direction, directionLength);
            drawPosition = Vector2D.getSum(directionAdjusted, drawPosition);

        }
    }

    protected boolean interact(Vector2D InteractPosition){
        boolean performedAction = false;
        for (Entity entity : entityHandler.getIterator()){
            if (interactConditions(entity, InteractPosition)){
                interactAction(entity);
                performedAction = true;
            }
        }
        return performedAction;
    }

    /**
     * Conditions that need to be met in order for an interaction to take place and be considered legal
     * @param entity Entity to check conditions against
     * @return true: the interaction is legal and takes place, false: do nothing since it's an illegal interaction
     */
    protected boolean interactConditions(Entity entity, Vector2D InteractPosition){
        return entity != this && entity.isOverlap(InteractPosition) ;
    }

    protected void interactAction(Entity entity){
    }

    /**
     * Function called on right click button
     * @param map map to be used when creating a path for movement
     * @param finder PathFinder object
     * @param mouseMapPos Position of mouse
     * @return if the move was legal or not (if the move wasn't legal, maybe don't "use" up the Entity's turn)
     */
    @Override
    public boolean onMouseClick3(PathMap map, PathFinder finder, Vector2D mouseMapPos) {
        if (!alive) return false;

        Vector2D mouseRounded = new Vector2D((int)mouseMapPos.getX(), (int)mouseMapPos.getY());
        Vector2D diff = Vector2D.getDifference(position, mouseRounded);
        boolean interactBool = false;
        if (diff.getLength() <= 1.42){
            interactBool = interact(mouseMapPos);
        }

        finder.setMap(map);
        path = finder.findPath(this, (int)position.getX(), (int)position.getY(),
                (int)mouseRounded.getX(), (int)mouseRounded.getY());

        if (path == null) {
            return interactBool;
        } else {
            animation.setAnimation(LivingEntityState.RUN);
            return true;
        }
    }

    @Override
    protected void updateRelativePos(WindowFocus focus) {
        super.updateRelativePos(focus);
    }

    protected void updateRotation(Vector2D direction){
        Rotation newRotation = new Rotation(Math.PI / 2);
        newRotation.addRadians(direction.getAngle().getRadians());
        this.rotation = newRotation;
    }

    protected void drawBanner(Graphics g, JComponent gc){
        if(flag != null){
            g.drawImage(flag, (int) relativePosition.getX(), (int) relativePosition.getY(), gc);
        }
    }

    protected void drawSizeRect(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(4));
        g2.setColor(Color.CYAN);
        g2.drawRect((int)(relativePosition.getX()), (int)(relativePosition.getY()),
                (int)(size.getX() * tileSize), (int)(size.getY() * tileSize));
        g2.setStroke(oldStroke);
    }

    @Override
    public void draw(Graphics g, JComponent gc) {
        Vector2D offsetPosition = Vector2D.getSum(drawPosition, Character.CHARACTER_OFFSET);
        g.drawImage(getTexture(), (int) offsetPosition.getX(), (int) offsetPosition.getY(), gc);
        drawBanner(g, gc);
    }

    public int getMovement() {
        return movement;
    }

    public void setMovement(int movement) {
        this.movement = movement;
    }

    public PlayerTeam getPlayerTeam(){
        return team;
    }

    /**
     * is the entity stationary, true or false.
     * @return true: the entity is stationary, false: the entity is moving
     */
    private boolean isStationary(){
        return path == null || path.getLength() == 0;
    }

    public boolean isInactive(){
        return isStationary() && animation.getEntityState() == LivingEntityState.IDLE;
    }
}
