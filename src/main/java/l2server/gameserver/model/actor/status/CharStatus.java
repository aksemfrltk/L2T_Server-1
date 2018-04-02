/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.actor.status;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.stat.CharStat;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.stats.BaseStats;
import l2server.gameserver.stats.Formulas;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;

public class CharStatus {
	private static Logger log = LoggerFactory.getLogger(CharStatus.class.getName());



	private Creature activeChar;

	private double currentHp = 0; //Current HP of the Creature
	private double currentMp = 0; //Current MP of the Creature

	/**
	 * Array containing all clients that need to be notified about hp/mp updates of the Creature
	 */
	private Set<Creature> statusListener;

	private Future<?> regTask;

	protected byte flagsRegenActive = 0;

	protected static final byte REGEN_FLAG_CP = 4;
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;

	public CharStatus(Creature activeChar) {
		this.activeChar = activeChar;
	}

	/**
	 * Add the object to the list of Creature that must be informed of HP/MP updates of this Creature.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each Creature owns a list called <B>statusListener</B> that contains all Player to inform of HP/MP updates.
	 * Players who must be informed are players that target this Creature.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Target a PC or NPC</li><BR><BR>
	 *
	 * @param object Creature to add to the listener
	 */
	public final void addStatusListener(Creature object) {
		if (object == getActiveChar()) {
			return;
		}

		getStatusListener().add(object);
	}

	/**
	 * Remove the object from the list of Creature that must be informed of HP/MP updates of this Creature.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each Creature owns a list called <B>statusListener</B> that contains all Player to inform of HP/MP updates.
	 * Players who must be informed are players that target this Creature.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Untarget a PC or NPC</li><BR><BR>
	 *
	 * @param object Creature to add to the listener
	 */
	public final void removeStatusListener(Creature object) {
		getStatusListener().remove(object);
	}

	/**
	 * Return the list of Creature that must be informed of HP/MP updates of this Creature.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each Creature owns a list called <B>statusListener</B> that contains all Player to inform of HP/MP updates.
	 * Players who must be informed are players that target this Creature.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 *
	 * @return The list of Creature to inform or null if empty
	 */
	public final Set<Creature> getStatusListener() {
		if (statusListener == null) {
			statusListener = new CopyOnWriteArraySet<>();
		}
		return statusListener;
	}

	// place holder, only PcStatus has CP
	public void reduceCp(int value) {
	}

	/**
	 * Reduce the current HP of the Creature and launch the doDie Task if necessary.<BR><BR>
	 * <p>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> Attackable : Set overhit values</li><BR>
	 * <li> Npc : Update the attacker AggroInfo of the Attackable aggroList and clear duel status of the attacking players</li><BR><BR>
	 *
	 * @param attacker The Creature who attacks
	 */
	public void reduceHp(double value, Creature attacker) {
		reduceHp(value, attacker, true, false, false);
	}

	public void reduceHp(double value, Creature attacker, boolean isHpConsumption) {
		reduceHp(value, attacker, true, false, isHpConsumption);
	}

	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption) {
		if (getActiveChar().isDead()) {
			return;
		}

		boolean isHide = attacker instanceof Player && ((Player) attacker).getAppearance().getInvisible();
		if (!isDOT && !isHPConsumption || isHide) {
			getActiveChar().stopEffectsOnDamage(awake, (int) value);

			if (getActiveChar().isStunned()) {
				int baseBreakChance = attacker.getLevel() > 85 ? 5 : 25; // TODO Recheck this
				double breakChance = baseBreakChance * Math.sqrt(BaseStats.CON.calcBonus(getActiveChar()));

				if (value > 2000) {
					breakChance *= 4;
				} else if (value > 1000) {
					breakChance *= 2;
				} else if (value > 500) {
					breakChance *= 1.5;
				}

				if (value > 100 && Rnd.get(100) < breakChance) {
					getActiveChar().stopStunning(true);
				}
			}
		}

		// invul handling
		if (getActiveChar().isInvul(attacker)) {
			// other chars can't damage
			//if (attacker != getActiveChar())
			//	return;

			// only DOT and HP consumption allowed for damage self
			if (!isDOT && !isHPConsumption) {
				return;
			}
		}

		if (attacker instanceof Playable) {
			final Player attackerPlayer = attacker.getActingPlayer();

			if (attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage()) {
				return;
			}
		}

		if (attacker instanceof Player) {
			Player player = (Player) attacker;

			if (player.isGM() && !player.getAccessLevel().canGiveDamage()) {
				return;
			}
		}

		StatusUpdateDisplay display = StatusUpdateDisplay.NONE;
		if (isDOT) {
			display = StatusUpdateDisplay.DOT;
		}
		if (value > 0) // Reduce Hp if any, and Hp can't be negative
		{
			setCurrentHp(Math.max(getCurrentHp() - value, 0), true, attacker, display);
		}

		if (getActiveChar().getCurrentHp() < 0.5 && getActiveChar().isMortal()) // Die
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();

			if (Config.DEBUG) {
				log.debug("char is dead.");
			}

			// Check for onKill skill trigger
			if (attacker.getChanceSkills() != null) {
				attacker.getChanceSkills().onKill(getActiveChar());
			}

			getActiveChar().doDie(attacker);
		}
	}

	public void reduceMp(double value) {
		setCurrentMp(Math.max(getCurrentMp() - value, 0));
	}

	/**
	 * Start the HP/MP/CP Regeneration task.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the regen task period </li>
	 * <li>Launch the HP/MP/CP Regeneration task with Medium priority </li><BR><BR>
	 */
	public final synchronized void startHpMpRegeneration() {
		if (regTask == null && !getActiveChar().isDead()) {
			if (Config.DEBUG) {
				log.debug("HP/MP regen started");
			}

			// Get the Regeneration periode
			int period = Formulas.getRegeneratePeriod(getActiveChar());

			// Create the HP/MP/CP Regeneration task
			regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RegenTask(), period, period);
		}
	}

	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 */
	public final synchronized void stopHpMpRegeneration() {
		if (regTask != null) {
			if (Config.DEBUG) {
				log.debug("HP/MP regen stop");
			}

			// Stop the HP/MP/CP Regeneration task
			regTask.cancel(false);
			regTask = null;

			// Set the RegenActive flag to false
			flagsRegenActive = 0;
		}
	}

	// place holder, only PcStatus has CP
	public double getCurrentCp() {
		return 0;
	}

	// place holder, only PcStatus has CP
	public void setCurrentCp(double newCp) {
	}

	public final double getCurrentHp() {
		return currentHp;
	}

	public final void setCurrentHp(double newHp) {
		setCurrentHp(newHp, false);
	}

	public void setCurrentHp(double newHp, boolean broadcastPacket) {
		setCurrentHp(newHp, broadcastPacket, null, StatusUpdateDisplay.NONE);
	}

	public void setCurrentHp(double newHp, boolean broadcastPacket, Creature causer, StatusUpdateDisplay display) {
		// Get the Max HP of the Creature
		final double maxHp = getActiveChar().getStat().getMaxHp();

		synchronized (this) {
			if (getActiveChar().isDead()) {
				return;
			}

			if (newHp >= maxHp) {
				// Set the RegenActive flag to false
				currentHp = maxHp;
				flagsRegenActive &= ~REGEN_FLAG_HP;

				// Stop the HP/MP/CP Regeneration task
				if (flagsRegenActive == 0) {
					stopHpMpRegeneration();
				}
			} else {
				// Set the RegenActive flag to true
				currentHp = newHp;
				flagsRegenActive |= REGEN_FLAG_HP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		if (broadcastPacket) {
			getActiveChar().broadcastStatusUpdate(causer, display);
		}
	}

	public final void setCurrentHpMp(double newHp, double newMp) {
		setCurrentHp(newHp, false);
		setCurrentMp(newMp, true); //send the StatusUpdate only once
	}

	public final double getCurrentMp() {
		return currentMp;
	}

	public final void setCurrentMp(double newMp) {
		setCurrentMp(newMp, true);
	}

	public final void setCurrentMp(double newMp, boolean broadcastPacket) {
		// Get the Max MP of the Creature
		final int maxMp = getActiveChar().getStat().getMaxMp();

		synchronized (this) {
			if (getActiveChar().isDead()) {
				return;
			}

			if (newMp >= maxMp) {
				// Set the RegenActive flag to false
				currentMp = maxMp;
				flagsRegenActive &= ~REGEN_FLAG_MP;

				// Stop the HP/MP/CP Regeneration task
				if (flagsRegenActive == 0) {
					stopHpMpRegeneration();
				}
			} else {
				// Set the RegenActive flag to true
				currentMp = newMp;
				flagsRegenActive |= REGEN_FLAG_MP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		if (broadcastPacket) {
			getActiveChar().broadcastStatusUpdate();
		}
	}

	protected void doRegeneration() {
		final CharStat charstat = getActiveChar().getStat();

		// Modify the current HP of the Creature and broadcast Server->Client packet StatusUpdate
		if (getCurrentHp() < charstat.getMaxHp()) {
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		}

		// Modify the current MP of the Creature and broadcast Server->Client packet StatusUpdate
		if (getCurrentMp() < charstat.getMaxMp()) {
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		}

		if (!getActiveChar().isInActiveRegion()) {
			// no broadcast necessary for characters that are in inactive regions.
			// stop regeneration for characters who are filled up and in an inactive region.
			if (getCurrentHp() == charstat.getMaxHp() && getCurrentMp() == charstat.getMaxMp()) {
				stopHpMpRegeneration();
			}
		} else {
			getActiveChar().broadcastStatusUpdate(); //send the StatusUpdate packet
		}
	}

	/**
	 * Task of HP/MP regeneration
	 */
	class RegenTask implements Runnable {
		@Override
		public void run() {
			try {
				doRegeneration();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	public Creature getActiveChar() {
		return activeChar;
	}
}
