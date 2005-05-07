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
package games.stendhal.server;

import marauroa.common.*;
import marauroa.common.game.*;
import marauroa.server.game.*;

import games.stendhal.common.*;
import games.stendhal.server.entity.*;

import java.util.*;

public class Path 
  {
  private static RPServerManager rpman;
  private static RPWorld world;
  
  public static class Node
    {
    public double x;
    public double y;
    
    public Node(double x, double y)
      {
      this.x=x;
      this.y=y;
      }
    }
    
  public static void initialize(RPServerManager rpman, RPWorld world)
    {
    Path.rpman=rpman;
    Path.world=world;
    }

  private static void moveto(ActiveEntity entity, double x, double y, double speed)
    {
    double rndx=x-entity.getx();
    double rndy=y-entity.gety();
    
    if(Math.abs(rndx)>Math.abs(rndy))
      {
      entity.setdx(speed*Math.signum(rndx));
      entity.setdy(0);
      }
    else
      {
      entity.setdx(0);
      entity.setdy(speed*Math.signum(rndy));
      }

    world.modify(entity);
    }
  
  static class NavigableStendhalNode implements Navigable
    {
    private ActiveEntity entity;
    private StendhalRPZone zone;
    
    public NavigableStendhalNode(ActiveEntity entity, StendhalRPZone zone)
      {
      this.entity=entity;
      this.zone=zone;
      }
    
    public boolean isValid(Pathfinder.Node node)
      {
      return !zone.simpleCollides(entity, node.getX(),node.getY());
      }
      
    public double getCost(Pathfinder.Node n1, Pathfinder.Node n2)
      {
      return Math.abs(n1.getX()-n2.getX())+Math.abs(n1.getY()-n2.getY());
      }
      
    public double getDistance(Pathfinder.Node n1, Pathfinder.Node n2)
      {
      return getCost(n1,n2);
      }
   
    public int createNodeID(Pathfinder.Node node)
      {
      return node.getY()*zone.getWidth()+node.getX();
      }
    }
    
  public static List<Node> searchPath(ActiveEntity entity, double x, double y)
    {
    Logger.trace("Path::searchPath",">");
    Pathfinder path=new Pathfinder();
    path.setNavigable(new NavigableStendhalNode(entity, (StendhalRPZone)world.getRPZone(entity.getID())));
    path.setEndpoints((int)entity.getx(),(int)entity.gety(),(int)x,(int)y);

    path.init();
    while(path.getStatus()==Pathfinder.IN_PROGRESS)
      {
      path.doStep();
      }
     
    Logger.trace("Path::searchPath","D","Optimal route to ("+x+","+y+")");
    List<Node> list=new LinkedList<Node>();
    Pathfinder.Node node=path.getBestNode();
    while(node!=null)
      {
      Logger.trace("Path::searchPath","D",node.toString());
      list.add(0,new Node(node.getX(),node.getY()));
      node=node.getParent();      
      }

    
    Logger.trace("Path::searchPath","<");
    return list;
    }
    
  public static boolean followPath(ActiveEntity entity, double speed)
    {
    List<Node> path=entity.getPath();
    
    if(path.size()==0)
      {
      return true;
      }
    
    int pos=entity.getPathPosition();
    
    Node actual=path.get(pos);
    
    if(entity.distance(actual.x, actual.y)<1)
      {
      Logger.trace("Path::followPath","D","Completed waypoint("+pos+")("+actual.x+","+actual.y+") on Path");
      pos++;
      if(pos<path.size())      
        {
        entity.setPathPosition(pos);
        return false;
        }
      else
        {
        if(entity.isPathLoop())
          {
          entity.setPathPosition(0);
          }
          
        return true;
        }
      }
    else
      {
      Logger.trace("Path::followPath","D","Moving to waypoint("+pos+")("+actual.x+","+actual.y+") on Path");
      moveto(entity,actual.x, actual.y,speed);     
      return false;    
      }    
    }
  }
