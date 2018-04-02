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

package ai.group_template;

import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;

import java.util.Collection;

public class GiantScouts extends L2AttackableAIScript {
	private static final int scouts[] = {22668, 22669};

	public GiantScouts(int questId, String name, String descr) {
		super(questId, name, descr);
		for (int id : scouts) {
			addAggroRangeEnterId(id);
		}
	}

	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet) {
		Creature target = isPet ? player.getPet() : player;

		if (GeoData.getInstance().canSeeTarget(npc, target)) {
			if (!npc.isInCombat() && npc.getTarget() == null) {
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.SHOUT, npc.getName(), "Oh Giants, an intruder has been discovered."));
			}

			npc.setTarget(target);
			npc.setRunning();
			((Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

			// Notify clan
			Collection<WorldObject> objs = npc.getKnownList().getKnownObjects().values();
			for (WorldObject obj : objs) {
				if (obj != null) {
					if (obj instanceof MonsterInstance) {
						MonsterInstance monster = (MonsterInstance) obj;
						if (npc.getClan() != null && monster.getClan() != null && monster.getClan().equals(npc.getClan()) &&
								GeoData.getInstance().canSeeTarget(npc, monster)) {
							monster.setTarget(target);
							monster.setRunning();
							monster.addDamageHate(target, 0, 999);
							monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						}
					}
				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	public static void main(String[] args) {
		new GiantScouts(-1, "GiantScouts", "ai");
	}
}
