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

package l2server.gameserver.stats.skills;

import l2server.gameserver.Ranked1v1;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;

public class SkillAgathion extends Skill {
	private int npcId;

	public SkillAgathion(StatsSet set) {
		super(set);
		npcId = set.getInteger("npcId", 0);
	}

	@Override
	public void useSkill(Creature caster, WorldObject[] targets) {
		if (caster.isAlikeDead() || !(caster instanceof Player)) {
			return;
		}

		Player activeChar = (Player) caster;

		if (activeChar.isInOlympiadMode()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		// Ranked 1v1 Restriction Agathions
		if (Ranked1v1.fighters.containsKey(activeChar)) {
			activeChar.sendMessage("You can't use this in 1v1!");
			return;
		}

		activeChar.setAgathionId(npcId);
		activeChar.broadcastUserInfo();
	}
}
