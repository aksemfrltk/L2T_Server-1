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

import l2server.Config;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.PetInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class PetItemList extends L2ItemListPacket {
	private static Logger log = LoggerFactory.getLogger(PetItemList.class.getName());


	
	private PetInstance activeChar;
	
	public PetItemList(PetInstance character) {
		activeChar = character;
		if (Config.DEBUG) {
			Item[] items = activeChar.getInventory().getItems();
			for (Item temp : items) {
				log.debug("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
			}
		}
	}
	
	@Override
	protected final void writeImpl() {
		Item[] items = activeChar.getInventory().getItems();
		int count = items.length;
		writeH(count);
		
		for (Item item : items) {
			writeItem(item);
		}
	}
}
