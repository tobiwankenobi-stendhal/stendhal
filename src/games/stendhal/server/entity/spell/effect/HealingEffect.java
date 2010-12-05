/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.spell.effect;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Nature;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;

/**
 * Effect for healing a player
 * 
 * @author madmetzger
 */
public class HealingEffect extends AbstractEffect {

	private static final Logger LOGGER = Logger.getLogger(HealingEffect.class);

	/**
	 * Creates a new {@link HealingEffect}
	 * 
	 * @param nature
	 * @param amount
	 * @param atk
	 * @param def
	 * @param lifesteal
	 * @param rate
	 * @param regen
	 */
	public HealingEffect(Nature nature, int amount, int atk, int def, double lifesteal, int rate,
			int regen) {
		super(nature, amount, atk, def, lifesteal, rate, regen);
	}

	public void act(Player caster, Entity target) {
		if (target instanceof Player) {
			actInternal(caster, (Player) target);
		} else {
			LOGGER.error("target is no instance of Player but: " + target, new Throwable());
		}
	}

	private void actInternal(Player caster, Player target) {
		target.heal(getAmount(), true);
	}

}
