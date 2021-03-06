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

import l2server.gameserver.handler.IUserCommandHandler;
import l2server.gameserver.handler.UserCommandHandler;
import l2server.gameserver.model.actor.instance.Player;

import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class BypassUserCmd extends L2GameClientPacket {
	static Logger log = Logger.getLogger(BypassUserCmd.class.getName());

	private int command;

	@Override
	protected void readImpl() {
		command = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}

		IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(command);

		if (handler == null) {
			if (player.isGM()) {
				player.sendMessage("User commandID " + command + " not implemented yet.");
			}
		} else {
			handler.useUserCommand(command, getClient().getActiveChar());
		}
	}
}
