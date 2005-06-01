/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity;

import games.stendhal.server.*;
import games.stendhal.common.*;
import marauroa.common.*;
import marauroa.common.game.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class Player extends RPEntity
  {
  private int devel;
  private int leave;
  private boolean hasLeave;

  public static void generateRPClass()
    {
    try
      {
      RPClass player=new RPClass("player");
      player.isA("rpentity");
      player.add("text",RPClass.LONG_STRING);
      player.add("private_text",RPClass.LONG_STRING,RPClass.HIDDEN);
      player.add("sheep",RPClass.INT);
      player.add("devel",RPClass.INT,RPClass.HIDDEN);
      player.add("dead",RPClass.FLAG,RPClass.HIDDEN);
      
      player.add("outfit",RPClass.INT);
      }
    catch(RPClass.SyntaxException e)
      {
      Logger.thrown("Player::generateRPClass","X",e);
      }
    }

  public Player(RPObject object) throws AttributeNotFoundException
    {
    super(object);
    put("type","player");
    hasLeave=false;

    update();
    }

  public void update() throws AttributeNotFoundException
    {
    super.update();
    if(has("devel")) devel=getInt("devel");
    }

  public void addXP(int newxp)
    {
    super.addXP(newxp);
    
    int newLevel=Level.getLevel(getXP());
    int levels=newLevel-getLevel();
    
    if(levels>0)
      {
      addDevel(levels);
      setLevel(newLevel);
      }
    }

  public void addDevel(int n)
    {
    devel+=n;
    put("devel",devel);
    }

  public void improveATK()
    {
    if(devel>0)
      {
      addDevel(-1);
      setATK(getATK()+1);
      }
    }

  public void improveDEF()
    {
    if(devel>0)
      {
      addDevel(-1);
      setDEF(getDEF()+1);
      }
    }

  public void improveHP()
    {
    if(devel>0)
      {
      addDevel(-1);
      setbaseHP(getbaseHP()+10);
      }
    }

  public void setPrivateText(String text)
    {
    put("private_text", text);
    }

  public void getArea(Rectangle2D rect, double x, double y)
    {
    rect.setRect(x,y+1,1,1);
    }

  public void onDead(RPEntity who)
    {
    put("dead","");

    if(hasSheep())
      {
      Sheep sheep=(Sheep)world.remove(getSheep());
      removeSheep(sheep);
      }

    super.onDead(who, false);
   
    // Penalize: Respawn on afterlive zone and 10% less experience
    setXP((int)(getXP()*0.9));
    setHP(getbaseHP());

    world.modify(who);

    StendhalRPAction.changeZone(this,"afterlive");
    StendhalRPAction.transferContent(this);
    }

  public void removeSheep(Sheep sheep)
    {
    Logger.trace("Player::removeSheep",">");
    remove("sheep");

    rp.removeNPC(sheep);

    Logger.trace("Player::removeSheep","<");
    }

  public boolean hasSheep()
    {
    return has("sheep");
    }

  public void setSheep(Sheep sheep)
    {
    Logger.trace("Player::setSheep",">");
    put("sheep",sheep.getID().getObjectID());

    rp.addNPC(sheep);

    Logger.trace("Player::setSheep","<");
    }

  public static class NoSheepException extends RuntimeException
    {
    public NoSheepException()
      {
      super();
      }
    }

  public RPObject.ID getSheep() throws NoSheepException
    {
    return new RPObject.ID(getInt("sheep"),get("zoneid"));
    }

  public void storeSheep(Sheep sheep)
    {
    Logger.trace("Player::storeSheep",">");
    if(!hasSlot("#flock"))
      {
      addSlot(new RPSlot("#flock"));
      }

    RPSlot slot=getSlot("#flock");
    slot.clear();
    slot.add(sheep);
    put("sheep",sheep.getID().getObjectID());
    Logger.trace("Player::storeSheep","<");
    }

  public Sheep retrieveSheep() throws NoSheepException
    {
    Logger.trace("Player::retrieveSheep",">");
    try
      {
      if(hasSlot("#flock"))
        {
        RPSlot slot=getSlot("#flock");
        if(slot.size()>0)
          {
          Iterator<RPObject> it=slot.iterator();

          Sheep sheep=new Sheep(it.next(),this);

          removeSlot("#flock");
          return sheep;
          }
        }

      throw new NoSheepException();
      }
    finally
      {
      Logger.trace("Player::retrieveSheep","<");
      }
    }
  }
