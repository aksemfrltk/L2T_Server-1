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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.NpcInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

public class FairyTrees extends L2AttackableAIScript {
	private static final int[] mobs = {27185, 27186, 27187, 27188};

	public FairyTrees(int questId, String name, String descr) {
		super(questId, name, descr);
		this.registerMobs(mobs, QuestEventType.ON_KILL);
		super.addSpawnId(27189);
	}

	public String onKill(NpcInstance npc, Player killer, boolean isPet) {
		int npcId = npc.getNpcId();
		if (Util.contains(mobs, npcId)) {
			for (int i = 0; i < 20; i++) {
				Attackable newNpc = (Attackable) addSpawn(27189, npc.getX(), npc.getY(), npc.getZ(), 0, false, 30000);
				Creature originalKiller = isPet ? killer.getPet() : killer;
				newNpc.setRunning();
				newNpc.addDamageHate(originalKiller, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalKiller);
				if (Rnd.get(1, 2) == 1) {
					Skill skill = SkillTable.getInstance().getInfo(4243, 1);
					if (skill != null && originalKiller != null) {
						skill.getEffects(newNpc, originalKiller);
					}
				}
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args) {
		new FairyTrees(-1, "fairy_trees", "ai");
	}
}
