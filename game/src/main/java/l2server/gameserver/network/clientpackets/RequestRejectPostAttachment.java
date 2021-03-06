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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExChangePostState;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;

import static l2server.gameserver.model.actor.Creature.ZONE_PEACE;

/**
 * @author Pere, DS
 */
public final class RequestRejectPostAttachment extends L2GameClientPacket {
	
	private int msgId;
	
	@Override
	protected void readImpl() {
		msgId = readD();
	}
	
	@Override
	public void runImpl() {
		if (!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS) {
			return;
		}
		
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("rejectattach")) {
			return;
		}
		
		if (!activeChar.isInsideZone(ZONE_PEACE)) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE));
			return;
		}
		
		Message msg = MailManager.getInstance().getMessage(msgId);
		if (msg == null) {
			return;
		}
		
		if (msg.getReceiverId() != activeChar.getObjectId()) {
			Util.handleIllegalPlayerAction(activeChar,
					"Player " + activeChar.getName() + " tried to reject not own attachment!",
					Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!msg.hasAttachments() || msg.getSendBySystem() != 0) {
			return;
		}
		
		MailManager.getInstance().sendMessage(new Message(msg));
		
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MAIL_SUCCESSFULLY_RETURNED));
		activeChar.sendPacket(new ExChangePostState(true, msgId, Message.REJECTED));
		
		final Player sender = World.getInstance().getPlayer(msg.getSenderId());
		if (sender != null) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RETURNED_MAIL);
			sm.addCharName(activeChar);
			sender.sendPacket(sm);
		}
	}
}
