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

package handlers.itemhandlers;

import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.Dice;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Broadcast;
import l2server.util.Rnd;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:30:07 $
 */

public class RollingDice implements IItemHandler {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		Player activeChar = (Player) playable;
		int itemId = item.getItemId();

		if (activeChar.isInOlympiadMode()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		if (itemId == 4625 || itemId == 4626 || itemId == 4627 || itemId == 4628) {
			int number = rollDice(activeChar);
			if (number == 0) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER));
				return;
			}

			Broadcast.toSelfAndKnownPlayers(activeChar,
					new Dice(activeChar.getObjectId(), item.getItemId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ()));

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ROLLED_S2);
			sm.addString(activeChar.getName());
			sm.addNumber(number);

			activeChar.sendPacket(sm);
			if (activeChar.isInsideZone(Creature.ZONE_PEACE)) {
				Broadcast.toKnownPlayers(activeChar, sm);
			} else if (activeChar.isInParty()) {
				activeChar.getParty().broadcastToPartyMembers(activeChar, sm);
			}
		}
	}

	private int rollDice(Player player) {
		// Check if the dice is ready
		if (!player.getFloodProtectors().getRollDice().tryPerformAction("roll dice")) {
			return 0;
		}
		return Rnd.get(1, 6);
	}
}
