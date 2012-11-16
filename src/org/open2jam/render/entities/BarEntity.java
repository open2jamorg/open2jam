
package org.open2jam.render.entities;


import org.open2jam.render.SpriteList;

/**
 *
 * @author fox
 */
public class BarEntity extends AnimatedEntity
{
    private int limit = 1;
    private int number = 0;


    public enum FillDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        UP_TO_DOWN,
        DOWN_TO_UP
    }

    private FillDirection direction = FillDirection.LEFT_TO_RIGHT;

    public BarEntity(SpriteList list, double x, double y)
    {
        super(list,x,y);
    }

    public BarEntity(SpriteList list, double x, double y, FillDirection fill)
    {
        super(list,x,y);
        direction = fill;
    }

    public void setNumber(Integer i){
        if(i <= limit) number = i;
        else number = limit;
    }

    public int getNumber(){
        return number;
    }

    public void addNumber(int add)
    {
        if(number + add <= limit) number += add;
        else number = limit;
    }
    
    public void subtractNumber(int sub) {
        if(number - sub >= 0) number -= sub;
        else number = 0;
    }

    public void setLimit(int limit){ this.limit = limit; }
    public int getLimit() {return limit; }

    public void setFillDirection(FillDirection value) { direction = value; }
    public FillDirection getFillDirection() { return direction; }

    @Override
    public void draw()
    {
        float perc = ((float)number / limit);
        double px = x;
        double py = y;
        switch(direction)
        {
            case LEFT_TO_RIGHT:
            sprite.setSlice(perc, 1);
            break;
            case UP_TO_DOWN:
            py += sprite.getHeight();
            sprite.setSlice(1, -perc);
            break;
            case RIGHT_TO_LEFT:
            px += sprite.getWidth();
            sprite.setSlice(-perc, 1);
            break;
            case DOWN_TO_UP:
            sprite.setSlice(1, perc);
            break;
        }
        sprite.draw(px, py);
    }
}
