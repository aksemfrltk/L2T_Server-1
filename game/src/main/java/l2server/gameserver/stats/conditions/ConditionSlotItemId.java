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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionSlotItemId.
 *
 * @author mkizub
 */
public final class ConditionSlotItemId extends ConditionInventory {

	private final int itemId;
	private final int enchantLevel;

	/**
	 * Instantiates a new condition slot item id.
	 *
	 * @param slot         the slot
	 * @param itemId       the item id
	 * @param enchantLevel the enchant level
	 */
	public ConditionSlotItemId(int slot, int itemId, int enchantLevel) {
		super(slot);
		this.itemId = itemId;
		this.enchantLevel = enchantLevel;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.ConditionInventory#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env) {
		if (!(env.player instanceof Player)) {
			return false;
		}
		Inventory inv = ((Player) env.player).getInventory();
		Item item = inv.getPaperdollItem(slot);
		if (item == null) {
			return itemId == 0;
		}
		return item.getItemId() == itemId && item.getEnchantLevel() >= enchantLevel;
	}
}
