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

import l2server.gameserver.model.actor.Npc;

/**
 * sample
 * 06 8f19904b 2522d04b 00000000 80 950c0000 4af50000 08f2ffff 0000	- 0 damage (missed 0x80)
 * 06 85071048 bc0e504b 32000000 10 fc41ffff fd240200 a6f5ffff 0100 bc0e504b 33000000 10									 3....
 * <p>
 * format
 * dddc dddh (ddc)
 *
 * @version $Revision: 1.1.6.2 $ $Date: 2005/03/27 15:29:39 $
 */
public class MonRaceInfo extends L2GameServerPacket {
	private int unknown1;
	private int unknown2;
	private Npc[] monsters;
	private int[][] speeds;
	
	public MonRaceInfo(int unknown1, int unknown2, Npc[] monsters, int[][] speeds) {
		/*
		 * -1 0 to initial the race
		 * 0 15322 to start race
		 * 13765 -1 in middle of race
		 * -1 0 to end the race
		 */
		this.unknown1 = unknown1;
		this.unknown2 = unknown2;
		this.monsters = monsters;
		this.speeds = speeds;
	}
	
	//  0xf3;EtcStatusUpdatePacket;ddddd
	
	@Override
	protected final void writeImpl() {
		writeD(unknown1);
		writeD(unknown2);
		writeD(8);
		
		for (int i = 0; i < 8; i++) {
			//Logozo.info("MOnster "+(i+1)+" npcid "+monsters[i].getNpcTemplate().getNpcId());
			writeD(monsters[i].getObjectId()); //npcObjectID
			writeD(monsters[i].getTemplate().NpcId + 1000000); //npcID
			writeD(14107); //origin X
			writeD(181875 + 58 * (7 - i)); //origin Y
			writeD(-3566); //origin Z
			writeD(12080); //end X
			writeD(181875 + 58 * (7 - i)); //end Y
			writeD(-3566); //end Z
			writeF(monsters[i].getTemplate().fCollisionHeight); //coll. height
			writeF(monsters[i].getTemplate().fCollisionRadius); //coll. radius
			writeD(120); // ?? unknown
			for (int j = 0; j < 20; j++) {
				if (unknown1 == 0) {
					writeC(speeds[i][j]);
				} else {
					writeC(0);
				}
			}
			writeD(0);
			writeD(0); // CT2.3 special effect
		}
	}
}
