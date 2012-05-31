package org.open2jam.render.entities;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author fox
 */
public class NumberEntity extends Entity
{
    int number = 0;
    int show_digits = 1;
    ArrayList<Entity> entity_list;
    
    public NumberEntity(Collection<Entity> list, double x, double y)
    {
        entity_list = new ArrayList<Entity>();
        entity_list.addAll(list);
        this.x = x;
        this.y = y;
        sprite = entity_list.get(0).sprite;
    }

    private NumberEntity(NumberEntity org) {
        super(org);
        entity_list = new ArrayList<Entity>();
        for(Entity e : org.entity_list)entity_list.add(e.copy());
        this.number = org.number;
        this.show_digits = org.show_digits;
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

    public void showDigits(int number)
    {
        show_digits = number;
    }

    @Override
    public void move(double delta)
    {
        super.move(delta);
    }
    
    @Override
    public void draw()
    {
        //draw from right to left
	String numberString = String.valueOf(number);
        if(numberString.length() < show_digits)
        {
            StringBuilder zeros = new StringBuilder(show_digits);
            for(int i = numberString.length(); i<show_digits;i++) 
                zeros.append('0');
            numberString = zeros.append(numberString).toString();
        }
        double tx = x;
        char[] chars = numberString.toCharArray();
        for(int j=chars.length-1;j>=0;j--)
        {
            int i = chars[j] - '0';
            tx -= entity_list.get(i).getWidth();
	    entity_list.get(i).setPos(tx,y);
            entity_list.get(i).draw();
        }
    }

    @Override
    public NumberEntity copy()
    {
        return new NumberEntity(this);
    }
}
