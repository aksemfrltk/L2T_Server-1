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

import java.util.List;

/**
 * Format: (ch) d[d]
 *
 * @author -Wooden-
 */
public class ExCursedWeaponList extends L2GameServerPacket
{
	private List<Integer> cursedWeaponIds;

	public ExCursedWeaponList(List<Integer> cursedWeaponIds)
	{
		this.cursedWeaponIds = cursedWeaponIds;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(cursedWeaponIds.size());
		for (int i : cursedWeaponIds)
		{
			writeD(i);
		}
	}
}
