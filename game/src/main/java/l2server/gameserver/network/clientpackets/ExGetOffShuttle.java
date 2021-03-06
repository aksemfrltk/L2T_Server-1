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

import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.ShuttleInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;

/**
 * @author Pere
 */
public class ExGetOffShuttle extends L2GameClientPacket {
	private int shuttleId;
	private int posX;
	private int posY;
	private int posZ;

	@Override
	protected void readImpl() {
		shuttleId = readD();
		posX = readD();
		posY = readD();
		posZ = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player == null || !player.canGetOnOffShuttle()) {
			return;
		}

		WorldObject obj = World.getInstance().findObject(shuttleId);
		if (!(obj instanceof ShuttleInstance)) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		ShuttleInstance shuttle = (ShuttleInstance) obj;
		if (shuttle.isClosed()) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		shuttle.oustPlayer(player, posX, posY, posZ);
	}
}
