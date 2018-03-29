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

import l2server.gameserver.model.L2Object;

/**
 * format  cdd
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public final class DeleteObject extends L2GameServerPacket
{
	private final int objectId;

	public DeleteObject(L2Object obj)
	{
		objectId = obj.getObjectId();
	}

	public DeleteObject(int objectId)
	{
		this.objectId = objectId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(objectId);
		writeC(0x00); //c2
	}
}
