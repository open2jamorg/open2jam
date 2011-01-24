
package org.open2jam.render.entities;


import org.open2jam.render.SpriteList;

/**
 *
 * @author fox
 */
public class BarEntity extends AnimatedEntity
{
    private int LIMIT = 1;
    private int number = 0;

    public enum fillDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        UP_TO_DOWN,
        DOWN_TO_UP
    };

    private fillDirection direction = fillDirection.LEFT_TO_RIGHT;

    public BarEntity(SpriteList list, double x, double y)
    {
        super(list,x,y);
    }

    public void setNumber(Integer i){
        this.number = i;
    }

    public int getNumber(){
        return number;
    }

    public void incNumber()
    {
        number++;
    }

    public void addNumber(int add)
    {
        number += add;
    }

    public void setLimit(int limit){ LIMIT = limit; }
    public int getLimit() {return LIMIT; }

    public void setFillDirection(fillDirection value) { direction = value; }
    public fillDirection getFillDirection() { return direction; }

    @Override
    public void draw()
    {
        float perc = ((float)number / LIMIT);
        double px = x;
        double py = y;
         // TODO: ideally, this should be called only when the number changes
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
