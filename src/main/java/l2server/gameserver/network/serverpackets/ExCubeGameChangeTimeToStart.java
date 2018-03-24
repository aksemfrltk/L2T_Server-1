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

package l2server.gameserver.network.serverpackets;

/**
 * Format: (chd) d
 * d: Seconds Left
 *
 * @author mrTJO
 */
public class ExCubeGameChangeTimeToStart extends L2GameServerPacket
{
	int _seconds;

	/**
	 * Update Minigame Waiting List Time to Start
	 *
	 * @param seconds
	 */
	public ExCubeGameChangeTimeToStart(int seconds)
	{
		_seconds = seconds;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_seconds);
	}
}